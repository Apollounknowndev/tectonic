package dev.worldgen.tectonic;

import dev.worldgen.apollib.Apollib;
import dev.worldgen.apollib.config.ApollibConfigHolder;
import dev.worldgen.apollib.registry.ApollibRegistrar;
import dev.worldgen.tectonic.config.ConfigState;
import dev.worldgen.tectonic.registry.TectonicRegistrations;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tectonic {
    public static final String MOD_ID = "tectonic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /**
     * Value saved in chunks used for blending between Tectonic versions.
     * <ol>
     *     <li>3.0.0 - 3.0.8, 3.0.10-3.0.27</li>
     *     <li>3.0.9</li>
     *     <li>3.1.0+</li>
     * </ol>
     */
    public static int BLENDING_VERSION = 1;
    public static String BLENDING_KEY = "tectonic:blending_version";
    
    public static final ApollibConfigHolder<ConfigState> CONFIG = Apollib.createConfigHolder(
        Tectonic.id("tectonic"),
        ApollibConfigHolder.CONFIG_DIRECTORY.resolve("tectonic.json"),
        ConfigState.CODEC,
        ConfigState.DEFAULT_STATE
    );
    public static final ApollibRegistrar REGISTRAR = Apollib.createRegistrar(MOD_ID);

    public static void init() {
        CONFIG.load();
        TectonicRegistrations.init();
        
        Apollib.CONDITIONS_BY_ID.put(id("experimental/volcanoes"), () -> true);
    }
    
    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
