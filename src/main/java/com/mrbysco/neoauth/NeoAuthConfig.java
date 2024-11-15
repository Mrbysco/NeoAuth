package com.mrbysco.neoauth;

import com.mrbysco.neoauth.util.MicrosoftUtils;
import com.mrbysco.neoauth.util.MicrosoftUtils.MicrosoftPrompt;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

public class NeoAuthConfig {
	public static class Client {
		public final EnumValue<MicrosoftPrompt> prompt;
		public final IntValue port;
		public final ConfigValue<? extends String> clientId;
		public final ConfigValue<? extends String> authorizeUrl;
		public final ConfigValue<? extends String> tokenUrl;
		public final ConfigValue<? extends String> xboxAuthUrl;
		public final ConfigValue<? extends String> xboxXstsUrl;
		public final ConfigValue<? extends String> mcAuthUrl;
		public final ConfigValue<? extends String> mcProfileUrl;

		public final IntValue xPos;
		public final IntValue yPos;
		public final BooleanValue draggable;

		Client(ModConfigSpec.Builder builder) {
			builder.comment("Auth settings")
					.push("microsoft");

			prompt = builder
					.comment("Indicates the type of user interaction that is required")
					.defineEnum("prompt", MicrosoftPrompt.DEFAULT);

			port = builder
					.comment("The port from which to listen for OAuth2 callbacks")
					.defineInRange("port", 25585, 0, 65535);

			clientId = builder
					.comment("OAuth2 client id")
					.define("clientId", MicrosoftUtils.CLIENT_ID);

			authorizeUrl = builder
					.comment("OAuth2 authorization url")
					.define("authorizeUrl", MicrosoftUtils.AUTHORIZE_URL);

			tokenUrl = builder
					.comment("OAuth2 access token url")
					.define("tokenUrl", MicrosoftUtils.TOKEN_URL);

			xboxAuthUrl = builder
					.comment("Xbox authentication url")
					.define("xboxAuthUrl", MicrosoftUtils.XBOX_AUTH_URL);

			xboxXstsUrl = builder
					.comment("Xbox XSTS authorization url")
					.define("xboxXstsUrl", MicrosoftUtils.XBOX_XSTS_URL);

			mcAuthUrl = builder
					.comment("Minecraft authentication url")
					.define("mcAuthUrl", MicrosoftUtils.MC_AUTH_URL);

			mcProfileUrl = builder
					.comment("Minecraft profile url")
					.define("mcProfileUrl", MicrosoftUtils.MC_PROFILE_URL);

			builder.pop();
			builder.comment("Button settings")
					.push("buttons");

			xPos = builder
					.comment("X Position of the button on the multiplayer screen")
					.defineInRange("xPos", 6, 0, Integer.MAX_VALUE);

			yPos = builder
					.comment("Y Position of the button on the multiplayer screen")
					.defineInRange("yPos", 6, 0, Integer.MAX_VALUE);

			draggable = builder
					.comment("True if the button can be dragged to a new position")
					.define("draggable", true);

			builder.pop();
		}
	}

	public static boolean isDefaults() {
		return Objects.equals(CLIENT.authorizeUrl.get(), MicrosoftUtils.AUTHORIZE_URL)
				&& Objects.equals(CLIENT.tokenUrl.get(), MicrosoftUtils.TOKEN_URL)
				&& Objects.equals(CLIENT.xboxAuthUrl.get(), MicrosoftUtils.XBOX_AUTH_URL)
				&& Objects.equals(CLIENT.xboxXstsUrl.get(), MicrosoftUtils.XBOX_XSTS_URL)
				&& Objects.equals(CLIENT.mcAuthUrl.get(), MicrosoftUtils.MC_AUTH_URL)
				&& Objects.equals(CLIENT.mcProfileUrl.get(), MicrosoftUtils.MC_PROFILE_URL);
	}


	public static final ModConfigSpec clientSpec;
	public static final Client CLIENT;

	static {
		final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
		clientSpec = specPair.getRight();
		CLIENT = specPair.getLeft();
	}
}
