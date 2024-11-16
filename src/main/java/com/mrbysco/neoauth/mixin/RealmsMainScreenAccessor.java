package com.mrbysco.neoauth.mixin;

import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Provides the means to access protected members of the Realms Main Screen.
 */
@Mixin(RealmsMainScreen.class)
public interface RealmsMainScreenAccessor {
	/**
	 * Sets the 'checked client compatibility' flag.
	 *
	 * @param checked true if checked
	 */
	@Accessor
	@Mutable
	static void setCheckedClientCompatability(boolean checked) {
	}

	/**
	 * Sets the 'Realms Generic Error' screen.
	 *
	 * @param screen error screen
	 */
	@Accessor
	@Mutable
	static void setRealmsGenericErrorScreen(Screen screen) {
	}
}
