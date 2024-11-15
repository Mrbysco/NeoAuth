package com.mrbysco.neoauth;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(NeoAuth.MOD_ID)
public class NeoAuth {
	public static final String MOD_ID = "neo_auth";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MOJANG_ACCOUNT_MIGRATION_FAQ_URL = "https://aka.ms/MinecraftPostMigrationFAQ";

	public NeoAuth(IEventBus eventBus, Dist dist, ModContainer container) {
		if (dist.isClient()) {
			container.registerConfig(ModConfig.Type.CLIENT, NeoAuthConfig.clientSpec, "neo-auth.toml");
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}
}
