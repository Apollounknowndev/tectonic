package dev.worldgen.tectonic.registry;

import dev.worldgen.tectonic.Tectonic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;

public interface TectonicDensityFunctions {
	ResourceKey<DensityFunction> ISLAND_CENTER_DISTANCE = key("noise/island/center_distance");
	ResourceKey<DensityFunction> ISLAND_SELECTOR = key("noise/island/selector");
	
	private static ResourceKey<DensityFunction> key(String name) {
		return ResourceKey.create(Registries.DENSITY_FUNCTION, Tectonic.id(name));
	}
}
