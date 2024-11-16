package com.mrbysco.neoauth;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(NeoAuth.MOD_ID)
public class NeoAuth {
	public static final String MOD_ID = "neo_auth";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MOJANG_ACCOUNT_MIGRATION_FAQ_URL = "https://aka.ms/MinecraftPostMigrationFAQ";
	public static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("neo_auth", "textures/gui/widgets.png");

	public NeoAuth() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, NeoAuthConfig.clientSpec);
		});
	}
}
