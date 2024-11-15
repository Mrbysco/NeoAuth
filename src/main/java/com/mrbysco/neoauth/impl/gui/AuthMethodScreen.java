package com.mrbysco.neoauth.impl.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrbysco.neoauth.NeoAuth;
import com.mrbysco.neoauth.NeoAuthConfig;
import com.mrbysco.neoauth.util.SessionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A screen for choosing a user authentication method.
 */
public class AuthMethodScreen extends Screen {
	// The parent (or last) screen that opened this screen
	private final Screen parentScreen;

	// The 'Microsoft' authentication method button textures
	public static final WidgetSprites MICROSOFT_BUTTON_TEXTURES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/microsoft_button"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/microsoft_button_disabled"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/microsoft_button_focused")
	);
	// The 'Mojang (or legacy)' authentication method button textures
	public static final WidgetSprites MOJANG_BUTTON_TEXTURES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/mojang_button"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/mojang_button_disabled"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/mojang_button_focused")
	);
	// The 'Offline' authentication method button textures
	public static final WidgetSprites OFFLINE_BUTTON_TEXTURES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/offline_button"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/offline_button_disabled"),
			ResourceLocation.fromNamespaceAndPath("neo_auth", "widget/offline_button_focused")
	);

	/**
	 * Constructs a new authentication method choice screen.
	 *
	 * @param parentScreen parent (or last) screen that opened this screen
	 */
	public AuthMethodScreen(Screen parentScreen) {
		super(Component.translatable("gui.neo_auth.method.title"));
		this.parentScreen = parentScreen;
	}

	@Override
	protected void init() {
		super.init();
		assert minecraft != null;

		// Add a title
		StringWidget titleWidget = new StringWidget(width, height, title, font);
		titleWidget.setColor(0xffffff);
		titleWidget.setPosition(width / 2 - titleWidget.getWidth() / 2, height / 2 - titleWidget.getHeight() / 2 - 22);
		addRenderableWidget(titleWidget);

		// Add a greeting message
		StringWidget greetingWidget = new StringWidget(
				width, height,
				Component.translatable(
						"gui.neo_auth.method.greeting",
						Component.literal(SessionUtils.getSession().getName()).withStyle(ChatFormatting.YELLOW)
				),
				font
		);
		greetingWidget.setColor(0xa0a0a0);
		greetingWidget.setPosition(
				width / 2 - greetingWidget.getWidth() / 2, height / 2 - greetingWidget.getHeight() / 2 - 42
		);
		addRenderableWidget(greetingWidget);

		// Add a button for the 'Microsoft' authentication method
		ImageButton msButton = new ImageButton(
				width / 2 - 10 - 10 - 4, height / 2 - 5, 20, 20,
				MICROSOFT_BUTTON_TEXTURES,
				button -> {
					// If 'Left Control' is being held, enforce user interaction
					final boolean selectAccount = InputConstants.isKeyDown(
							minecraft.getWindow().getWindow(), InputConstants.KEY_LCONTROL
					);
					if (NeoAuthConfig.isDefaults()) {
						minecraft.setScreen(new MicrosoftAuthScreen(this, parentScreen, selectAccount));
					} else {
						NeoAuth.LOGGER.warn("Non-default Microsoft authentication URLs are in use!");
						ConfirmScreen confirmScreen = new ConfirmScreen(
								a -> minecraft.setScreen(a ? new MicrosoftAuthScreen(this, parentScreen, selectAccount) : this),
								Component.translatable("gui.neo_auth.microsoft.warning.title"),
								Component.translatable("gui.neo_auth.microsoft.warning.body"),
								Component.translatable("gui.neo_auth.microsoft.warning.accept"),
								Component.translatable("gui.neo_auth.microsoft.warning.cancel")
						);
						minecraft.setScreen(confirmScreen);
						confirmScreen.setDelay(40);
					}
				},
				Component.translatable("gui.neo_auth.method.button.microsoft")
		);
		msButton.setTooltip(Tooltip.create(
				Component.translatable("gui.neo_auth.method.button.microsoft")
						.append("\n")
						.append(
								Component.translatable("gui.neo_auth.method.button.microsoft.selectAccount").withStyle(ChatFormatting.GRAY)
						)
		));
		addRenderableWidget(msButton);

		// Add a button for the 'Mojang (or legacy)' authentication method
		ImageButton mojangButton = new ImageButton(
				width / 2, height / 2 - 5, 20, 20,
				MOJANG_BUTTON_TEXTURES,
				ConfirmLinkScreen.confirmLink(this, NeoAuth.MOJANG_ACCOUNT_MIGRATION_FAQ_URL),
				Component.translatable("gui.neo_auth.method.button.mojang")
		);
		mojangButton.setTooltip(Tooltip.create(
				Component.translatable("gui.neo_auth.method.button.mojang")
						.append("\n")
						.append(Component.translatable("gui.neo_auth.method.button.mojang.unavailable").withStyle(ChatFormatting.RED))
		));
		addRenderableWidget(mojangButton);

		// Add a button to go back
		addRenderableWidget(
				Button.builder(Component.translatable("gui.back"), button -> onClose())
						.bounds(width / 2 - 50, height / 2 + 27, 100, 20)
						.build()
		);
	}

	@Override
	public void onClose() {
		if (minecraft != null) minecraft.setScreen(parentScreen);
	}
}
