package fr.quentin.phosg;

import fr.quentin.phosg.util.ScreenConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

/**
 * The main client mod class for Phosg.
 */
public class PhosgClient implements ClientModInitializer {
    private static ScreenConfig config;

    @Override
    public void onInitializeClient() {
        ConfigHolder<ScreenConfig> configHolder = AutoConfig.register(ScreenConfig.class, Toml4jConfigSerializer::new);
        PhosgClient.config = configHolder.getConfig();
    }

    /**
     * Gets the current configuration for the mod.
     *
     * @return The current ScreenConfig instance.
     */
    public static ScreenConfig getConfig() {
        return PhosgClient.config;
    }
}