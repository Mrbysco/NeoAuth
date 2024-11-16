package com.mrbysco.neoauth.impl.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrbysco.neoauth.NeoAuth;
import com.mrbysco.neoauth.NeoAuthConfig;
import com.mrbysco.neoauth.util.SessionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A screen for choosing a user authentication method.
 */
public class AuthMethodScreen extends Screen {
	// The parent (or last) screen that opened this screen
	private final Screen parentScreen;

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
				0, 0, 20, NeoAuth.WIDGETS_TEXTURE, 128, 128,
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
				20, 0, 20, NeoAuth.WIDGETS_TEXTURE, 128, 128,
				ConfirmLinkScreen.confirmLink(NeoAuth.MOJANG_ACCOUNT_MIGRATION_FAQ_URL, this, true),
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		assert minecraft != null;

		// Render the background before any widgets
		renderBackground(guiGraphics);

		// Cascade the rendering
		super.render(guiGraphics, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		if (minecraft != null) minecraft.setScreen(parentScreen);
	}
}
