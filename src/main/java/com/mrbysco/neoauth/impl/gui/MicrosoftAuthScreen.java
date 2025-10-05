package com.mrbysco.neoauth.impl.gui;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mrbysco.neoauth.NeoAuth;
import com.mrbysco.neoauth.api.gui.AuthScreen;
import com.mrbysco.neoauth.util.MicrosoftUtils;
import com.mrbysco.neoauth.util.SessionUtils;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.apache.http.conn.ConnectTimeoutException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A screen for handling user authentication via Microsoft.
 */
public class MicrosoftAuthScreen extends AuthScreen {

	public static final WidgetSprites COPY_BUTTON_TEXTURES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/copy_button"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/copy_button_disabled"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/copy_button_focused")
	);

	// The executor to run the login task on
	private ExecutorService executor = null;
	// The completable future for all Microsoft login tasks
	private CompletableFuture<Void> task = null;
	// The current progress/status of the login task
	private StringWidget statusWidget = null;
	// The uri used for authentication
	private URI authUri = null;
	// True if Microsoft should prompt to select an account
	private final boolean selectAccount;

	/**
	 * Constructs a new authentication via Microsoft screen.
	 *
	 * @param parentScreen  parent (or last) screen that opened this screen
	 * @param successScreen screen to be returned to after a successful login
	 * @param selectAccount true if Microsoft should prompt to select an account
	 */
	public MicrosoftAuthScreen(Screen parentScreen, Screen successScreen, boolean selectAccount) {
		super(Component.translatable("gui.neo_auth.microsoft.title"), parentScreen, successScreen);
		this.selectAccount = selectAccount;
		this.closeOnSuccess = true;
	}

	@Override
	protected void init() {
		super.init();
		assert minecraft != null;

		// Add a title
		StringWidget titleWidget = new StringWidget(width, height, title, font);
		titleWidget.setColor(0xffffff);
		titleWidget.setPosition(width / 2 - titleWidget.getWidth() / 2, height / 2 - titleWidget.getHeight() / 2 - 27);
		addRenderableWidget(titleWidget);

		// Add a status message
		var previousMessage = statusWidget != null ? statusWidget.getMessage() : Component.empty();
		statusWidget = new StringWidget(width, height, title, font);
		statusWidget.setColor(0xdddddd);
		statusWidget.setPosition(width / 2 - statusWidget.getWidth() / 2, height / 2 - statusWidget.getHeight() / 2 - 1);
		statusWidget.setMessage(previousMessage);
		addRenderableWidget(statusWidget);

		// Add a cancel button to abort the task
		final Button cancelBtn;
		addRenderableWidget(cancelBtn = Button.builder(Component.translatable("gui.cancel"), button -> onClose()).bounds(width / 2 - 50, height / 2 + 22, 75, 20).build());

		final Button copyLinkBtn;
		addRenderableWidget(copyLinkBtn = new ImageButton(width / 2 + 30, height / 2 + 22, 20, 20, COPY_BUTTON_TEXTURES, button -> copyLinkToClipboard(), Component.translatable("chat.copy")));
		copyLinkBtn.setTooltip(Tooltip.create(
				Component.translatable("gui.neo_auth.microsoft.button.copyLink")
						.append("\n")
						.append(
								Component.translatable("gui.neo_auth.microsoft.button.copyLink.tooltip").withStyle(ChatFormatting.GRAY)
						)
		));
		copyLinkBtn.active = authUri != null;


		// Prevent the task from starting several times
		if (task != null) return;

		// Set the initial progress/status of the login task
		statusWidget.setMessage(Component.translatable("gui.neo_auth.microsoft.status.checkBrowser"));

		// Prepare a new executor thread to run the login task on
		executor = Executors.newSingleThreadExecutor();

		// Start the login task
		task = MicrosoftUtils
				// Acquire a Microsoft auth code
				.acquireMSAuthCode(
						uri -> {
							authUri = uri;
							copyLinkBtn.active = true;
							Util.getPlatform().openUri(uri);
						},
						success -> Component.translatable("gui.neo_auth.microsoft.browser").getString(),
						executor,
						selectAccount ? MicrosoftUtils.MicrosoftPrompt.SELECT_ACCOUNT : null
				)

				// Exchange the Microsoft auth code for an access token
				.thenComposeAsync(msAuthCode -> {
					statusWidget.setMessage(Component.translatable("gui.neo_auth.microsoft.status.msAccessToken"));
					return MicrosoftUtils.acquireMSAccessToken(msAuthCode, executor);
				})

				// Exchange the Microsoft access token for an Xbox access token
				.thenComposeAsync(msAccessToken -> {
					statusWidget.setMessage(Component.translatable("gui.neo_auth.microsoft.status.xboxAccessToken"));
					return MicrosoftUtils.acquireXboxAccessToken(msAccessToken, executor);
				})

				// Exchange the Xbox access token for an XSTS token
				.thenComposeAsync(xboxAccessToken -> {
					statusWidget.setMessage(Component.translatable("gui.neo_auth.microsoft.status.xboxXstsToken"));
					return MicrosoftUtils.acquireXboxXstsToken(xboxAccessToken, executor);
				})

				// Exchange the Xbox XSTS token for a Minecraft access token
				.thenComposeAsync(xboxXstsData -> {
					statusWidget.setMessage(Component.translatable("gui.neo_auth.microsoft.status.mcAccessToken"));
					return MicrosoftUtils.acquireMCAccessToken(xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor);
				})

				// Build a new Minecraft session with the Minecraft access token
				.thenComposeAsync(mcToken -> {
					statusWidget.setMessage(Component.translatable("gui.neo_auth.microsoft.status.mcProfile"));
					return MicrosoftUtils.login(mcToken, executor);
				})

				// Update the game session and greet the player
				.thenAccept(session -> {
					// Apply the new session
					SessionUtils.setSession(session);
					// Add a toast that greets the player
					SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("gui.neo_auth.toast.greeting", Component.literal(session.getName())), null);
					// Mark the task as successful, in turn closing the screen
					NeoAuth.LOGGER.info("Successfully logged in via Microsoft!");
					success = true;
				})

				// On any exception, update the status and cancel button
				.exceptionally(error -> {
					final String key;
					if (error.getCause() instanceof ConnectTimeoutException) {
						key = "gui.neo_auth.error.timeout";
					} else if ("NOT_FOUND: Not Found".equals(error.getCause().getMessage())) {
						key = "gui.neo_auth.error.notPurchased";
					} else {
						key = "gui.neo_auth.error.generic";
					}
					statusWidget.setMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
					cancelBtn.setMessage(Component.translatable("gui.back"));
					return null; // return a default value
				});
	}

	public void copyLinkToClipboard() {
		if (authUri != null && minecraft != null) {
			minecraft.keyboardHandler.setClipboard(authUri.toString());
			if (statusWidget != null) {
				statusWidget.setMessage(Component.translatable("gui.neo_auth.microsoft.status.linkCopied"));
			}
		}
	}

	@Override
	public void onClose() {
		// Cancel the login task if still running
		if (task != null && !task.isDone()) {
			task.cancel(true);
			executor.shutdownNow();
		}

		// Cascade the closing
		super.onClose();
	}
}
