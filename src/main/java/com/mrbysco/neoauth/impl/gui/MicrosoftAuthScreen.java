package com.mrbysco.neoauth.impl.gui;

import com.mrbysco.neoauth.NeoAuth;
import com.mrbysco.neoauth.api.gui.AuthScreen;
import com.mrbysco.neoauth.util.MicrosoftUtils;
import com.mrbysco.neoauth.util.SessionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.http.conn.ConnectTimeoutException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A screen for handling user authentication via Microsoft.
 */
public class MicrosoftAuthScreen extends AuthScreen {
	// The executor to run the login task on
	private ExecutorService executor = null;
	// The completable future for all Microsoft login tasks
	private CompletableFuture<Void> task = null;
	// The current progress/status of the login task
	private Component status = null;
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

		// Add a cancel button to abort the task
		final Button cancelBtn;
		addRenderableWidget(
				cancelBtn = Button.builder(
						Component.translatable("gui.cancel"),
						button -> onClose()
				).bounds(
						width / 2 - 50, height / 2 + 22, 100, 20
				).build()
		);

		// Prevent the task from starting several times
		if (task != null) return;

		// Set the initial progress/status of the login task
		status = Component.translatable("gui.neo_auth.microsoft.status.checkBrowser");

		// Prepare a new executor thread to run the login task on
		executor = Executors.newSingleThreadExecutor();

		// Start the login task
		task = MicrosoftUtils
				// Acquire a Microsoft auth code
				.acquireMSAuthCode(
						success -> Component.translatable("gui.neo_auth.microsoft.browser").getString(),
						executor,
						selectAccount ? MicrosoftUtils.MicrosoftPrompt.SELECT_ACCOUNT : null
				)

				// Exchange the Microsoft auth code for an access token
				.thenComposeAsync(msAuthCode -> {
					status = Component.translatable("gui.neo_auth.microsoft.status.msAccessToken");
					return MicrosoftUtils.acquireMSAccessToken(msAuthCode, executor);
				})

				// Exchange the Microsoft access token for an Xbox access token
				.thenComposeAsync(msAccessToken -> {
					status = Component.translatable("gui.neo_auth.microsoft.status.xboxAccessToken");
					return MicrosoftUtils.acquireXboxAccessToken(msAccessToken, executor);
				})

				// Exchange the Xbox access token for an XSTS token
				.thenComposeAsync(xboxAccessToken -> {
					status = Component.translatable("gui.neo_auth.microsoft.status.xboxXstsToken");
					return MicrosoftUtils.acquireXboxXstsToken(xboxAccessToken, executor);
				})

				// Exchange the Xbox XSTS token for a Minecraft access token
				.thenComposeAsync(xboxXstsData -> {
					status = Component.translatable("gui.neo_auth.microsoft.status.mcAccessToken");
					return MicrosoftUtils.acquireMCAccessToken(
							xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor
					);
				})

				// Build a new Minecraft session with the Minecraft access token
				.thenComposeAsync(mcToken -> {
					status = Component.translatable("gui.neo_auth.microsoft.status.mcProfile");
					return MicrosoftUtils.login(mcToken, executor);
				})

				// Update the game session and greet the player
				.thenAccept(session -> {
					// Apply the new session
					SessionUtils.setSession(session);
					// Add a toast that greets the player
					SystemToast.add(
							minecraft.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT,
							Component.translatable("gui.neo_auth.toast.greeting", Component.literal(session.getName())), null
					);
					// Mark the task as successful, in turn closing the screen
					NeoAuth.LOGGER.info("Successfully logged in via Microsoft!");
					success = true;
				})

				// On any exception, update the status and cancel button
				.exceptionally(error -> {
					status = Component.translatable(
							error.getCause() instanceof ConnectTimeoutException ? "gui.neo_auth.error.timeout"
									: "gui.neo_auth.error.generic"
					).withStyle(ChatFormatting.RED);
					cancelBtn.setMessage(Component.translatable("gui.back"));
					return null; // return a default value
				});
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		assert minecraft != null;

		// Render the background before any widgets
		renderBackground(graphics);

		// Render a title for the screen
		graphics.drawCenteredString(font, title, width / 2, height / 2 - 32, 0xffffff);

		// Render the current progress/status of the login, if present
		if (status != null) {
			graphics.drawCenteredString(font, status, width / 2, height / 2 - 6, 0xdddddd);
		}

		// Cascade the rendering
		super.render(graphics, mouseX, mouseY, delta);
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
