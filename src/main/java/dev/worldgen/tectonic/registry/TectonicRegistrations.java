package dev.worldgen.tectonic.registry;

import dev.worldgen.lithostitched.api.registry.LithostitchedBuiltInRegistries;
import dev.worldgen.tectonic.lithostitched.ConfigLoadPredicate;
import dev.worldgen.tectonic.lithostitched.SetHeightLimitsModifier;
import dev.worldgen.tectonic.worldgen.densityfunction.*;
import dev.worldgen.tectonic.worldgen.feature.LavaFeature;
import dev.worldgen.tectonic.worldgen.placementmodifier.HeightStabilizedCount;
import dev.worldgen.tectonic.worldgen.placementmodifier.TileAcrossChunk;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import static dev.worldgen.tectonic.Tectonic.REGISTRAR;

public class TectonicRegistrations {
	public static void init() {
		register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, "config_clamp", ConfigClamp.DATA_CODEC);
		register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, "config_constant", ConfigConstant.DATA_CODEC);
		register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, "config_noise", ConfigNoise.DATA_CODEC);
		
		register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, "height_stabilized_count", HeightStabilizedCount.TYPE);
		register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, "tile_across_chunk", TileAcrossChunk.TYPE);
		
		register(BuiltInRegistries.FEATURE, "lava", LavaFeature.INSTANCE);
		
		register(LithostitchedBuiltInRegistries.MODIFIER_TYPE, "set_height_limits", SetHeightLimitsModifier.CODEC);
		
		register(LithostitchedBuiltInRegistries.LOAD_PREDICATE_TYPE, "config", ConfigLoadPredicate.CODEC);
		
		REGISTRAR.registerAll();
	}
	
	public static <T> void register(Registry<? super T> registry, String string, T value) {
		REGISTRAR.register(registry, string, value);
	}
}
