package com.mrbysco.neoauth.mixin;

import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mrbysco.neoauth.NeoAuth;
import com.mrbysco.neoauth.impl.gui.AuthMethodScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.realms.RealmsScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects a button into the Realms' error screen to open the authentication screen.
 */
@Mixin(RealmsGenericErrorScreen.class)
public abstract class RealmsGenericErrorScreenMixin extends RealmsScreen {
	@Shadow
	@Final
	private Screen nextScreen;

	@Shadow
	@Final
	private RealmsGenericErrorScreen.ErrorMessage lines;

	private RealmsGenericErrorScreenMixin(Component title) {
		super(title);
	}

	/**
	 * Injects into the creation of the screen and adds the authentication button.
	 *
	 * @param ci injection callback info
	 */
	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		// Determine if the disconnection reason is user or session related
		if (isUserRelated(lines.detail())) {
			NeoAuth.LOGGER.info("Adding auth button to the Realms error screen");
			assert minecraft != null;

			// Create and add the button to the screen above the back button
			final Button backButton = (Button) children().get(0);
			addRenderableWidget(
					Button.builder(
							Component.translatable("gui.neo_auth.button.relogin"),
							btn -> minecraft.setScreen(new AuthMethodScreen(nextScreen))
					).bounds(
							backButton.getX(),
							backButton.getY() - backButton.getHeight() - 4,
							backButton.getWidth(),
							backButton.getHeight()
					).build()
			);
		}
	}

	/**
	 * Determines if a Realms' disconnection reason is user or session related.
	 *
	 * @param reason disconnect reason text
	 * @return true if the disconnection reason is user or session related
	 */
	@Unique
	private static boolean isUserRelated(final @Nullable Component reason) {
		if (reason != null && reason.getContents() instanceof TranslatableContents content) {
			final String key = content.getKey();
			return key != null && key.startsWith("mco.error.invalid.session");
		}
		return false;
	}
}
