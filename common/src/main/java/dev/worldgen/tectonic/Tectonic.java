package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Tectonic {
    public static final String MOD_ID = "tectonic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static void init(Path path) {
        ConfigHandler.load(path);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    /// If the chunk's blending version doesn't match this, then chunk blending will be enabled.
    /// @return 0 when disabled.
    public static int blendingVersion() {
        return ConfigHandler.getConfig().enabled() ? 1000 /* Change this number when making breaking changes to world generation */ : 0;
    }
}