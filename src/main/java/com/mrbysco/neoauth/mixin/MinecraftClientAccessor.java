package com.mrbysco.neoauth.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Provides the means to access protected members of the Minecraft client.
 */
@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
	/**
	 * Sets the Minecraft user.
	 *
	 * @param user new Minecraft user
	 */
	@Accessor
	@Mutable
	void setUser(User user);

	/**
	 * Sets the Minecraft user API service.
	 *
	 * @param userApiService new Minecraft user API service
	 */
	@Accessor
	@Mutable
	void setUserApiService(UserApiService userApiService);

	/**
	 * Sets the Minecraft social interactions manager.
	 *
	 * @param playerSocialManager new Minecraft social interactions manager
	 */
	@Accessor
	@Mutable
	void setPlayerSocialManager(PlayerSocialManager playerSocialManager);

	/**
	 * Sets the Minecraft profile keys.
	 *
	 * @param profileKeyPairManager new Minecraft profile keys
	 */
	@Accessor
	@Mutable
	void setProfileKeyPairManager(ProfileKeyPairManager profileKeyPairManager);

	/**
	 * Sets the Minecraft abuse report context.
	 *
	 * @param reportingContext new Minecraft abuse report context
	 */
	@Accessor
	@Mutable
	void setReportingContext(ReportingContext reportingContext);

	/**
	 * Sets the Minecraft Realms periodic checkers.
	 *
	 * @param realmsDataFetcher new Minecraft Realms periodic checkers
	 */
	@Accessor
	@Mutable
	void setRealmsDataFetcher(RealmsDataFetcher realmsDataFetcher);
}
