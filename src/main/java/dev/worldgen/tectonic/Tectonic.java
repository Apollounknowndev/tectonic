package dev.worldgen.tectonic;

import dev.worldgen.apollib.Apollib;
import dev.worldgen.apollib.config.ApollibConfigHolder;
import dev.worldgen.apollib.registry.ApollibRegistrar;
import dev.worldgen.lithostitched.api.event.AddWorldgenModifiersEvent;
import dev.worldgen.lithostitched.api.worldgen.densityfunction.LithostitchedDensityFunctions;
import dev.worldgen.lithostitched.api.worldgen.densityfunction.cellular.ReturnType;
import dev.worldgen.lithostitched.api.worldgen.modifier.WorldgenModifier;
import dev.worldgen.tectonic.config.ConfigState;
import dev.worldgen.tectonic.registry.TectonicRegistrations;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tectonic {
    public static final String MOD_ID = "tectonic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /**
     * Value saved in chunks used for blending between Tectonic versions.
     * <ol>
     *     <li>3.0.0 - 3.0.8, 3.0.10+</li>
     *     <li>3.0.9</li>
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
	    
	    AddWorldgenModifiersEvent.EVENT.register((registries, consumer) -> {
			var dfs = registries.lookupOrThrow(Registries.DENSITY_FUNCTION);
			var rawCenterDistance = dfs.get(id("noise/island/raw_center_distance"));
			var rawCenterValue = dfs.get(id("noise/island/raw_center_value"));
			var condition = dfs.get(id("noise/island/condition"));
			if (rawCenterDistance.isEmpty() || rawCenterValue.isEmpty() || condition.isEmpty()) return;
			
			int gridSize = CONFIG.getState().islands.averageDistance;
			int jitter = 1 - (640 / gridSize);
			
			consumer.accept(
				id("apply_island_config_distance"),
				WorldgenModifier.builder().wrapDensityFunction(
					rawCenterDistance.get(),
					DensityFunctions.cache(
						LithostitchedDensityFunctions.cellular2d(
							condition.get().value(), 1, 1, 16384,
							gridSize, jitter, ReturnType.DISTANCE_1, 896234
						)
					)
				)
			);
		    
		    consumer.accept(
			    id("apply_island_config_value"),
			    WorldgenModifier.builder().wrapDensityFunction(
				    rawCenterValue.get(),
				    LithostitchedDensityFunctions.cellular2d(
					    condition.get().value(), 1, 1, -64,
						gridSize, jitter, ReturnType.CELL_VALUE, 896234
				    )
			    )
		    );
	    });
		
        if (Apollib.isModLoaded("terralith")) {
            throw new IllegalStateException("This Tectonic beta doesn't have compat with Terralith, come back later.");
        }
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
