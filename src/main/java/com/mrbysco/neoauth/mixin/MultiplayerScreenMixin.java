package com.mrbysco.neoauth.mixin;

import com.mrbysco.neoauth.NeoAuth;
import com.mrbysco.neoauth.NeoAuthConfig;
import com.mrbysco.neoauth.api.gui.widget.AuthButtonWidget;
import com.mrbysco.neoauth.impl.gui.AuthMethodScreen;
import com.mrbysco.neoauth.util.SessionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects a button into the multiplayer screen to open the authentication screen.
 */
@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
	private MultiplayerScreenMixin(Component title) {
		super(title);
	}

	/**
	 * Injects into the creation of the screen and adds the authentication button.
	 *
	 * @param ci injection callback info
	 */
	@Inject(method = "init", at = @At("HEAD"))
	private void init(CallbackInfo ci) {
		NeoAuth.LOGGER.info("Adding auth button to the multiplayer screen");
		assert minecraft != null;

		// Create and add the button to the screen
		addRenderableWidget(
				new AuthButtonWidget(
						this,
						NeoAuthConfig.CLIENT.xPos.get(),
						NeoAuthConfig.CLIENT.yPos.get(),
						btn -> minecraft.setScreen(new AuthMethodScreen(this)),
						// Optionally, enable button dragging
						NeoAuthConfig.CLIENT.draggable.get() ? btn -> {
							// Sync configuration with the updated button position
							NeoAuth.LOGGER.info("Moved the auth button to {}, {}", btn.getX(), btn.getY());
							NeoAuthConfig.CLIENT.xPos.set(btn.getX());
							NeoAuthConfig.CLIENT.xPos.save();
							NeoAuthConfig.CLIENT.yPos.set(btn.getY());
							NeoAuthConfig.CLIENT.yPos.save();
						} : null,
						// Add a tooltip to greet the player
						Tooltip.create(Component.translatable(
								"gui.neo_auth.button.auth.tooltip",
								Component.literal(SessionUtils.getSession().getName()).withStyle(ChatFormatting.YELLOW)
						)),
						// Non-visible text, useful for screen narrator
						Component.translatable("gui.neo_auth.button.auth")
				)
		);
	}
}
