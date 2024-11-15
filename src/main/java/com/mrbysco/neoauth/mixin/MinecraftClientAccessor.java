package com.mrbysco.neoauth.mixin;

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.realmsclient.gui.RealmsDataFetcher;

/**
 * Provides the means to access protected members of the Minecraft client.
 */
@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
	/**
	 * Sets the Minecraft session.
	 *
	 * @param user new Minecraft session
	 */
	@Accessor
	@Mutable
	void setUser(User user);

	/**
	 * Sets the game profile.
	 *
	 * @param profileFuture the future of the new game profile
	 */
	@Accessor
	@Mutable
	void setProfileFuture(CompletableFuture<ProfileResult> profileFuture);

	/**
	 * Returns the Minecraft authentication service.
	 *
	 * @return the Minecraft authentication service
	 */
	@Accessor
	YggdrasilAuthenticationService getAuthenticationService();

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
