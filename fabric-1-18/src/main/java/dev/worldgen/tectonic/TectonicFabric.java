package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigDensityFunction;
import dev.worldgen.tectonic.worldgen.DynamicReferenceDensityFunction;
import dev.worldgen.tectonic.worldgen.ErosionNoiseDensityFunction;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.ArrayList;
import java.util.List;

import static dev.worldgen.tectonic.Tectonic.idOf;

public class TectonicFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Tectonic.init(RegistryCodecs.homogeneousList(Registry.DENSITY_FUNCTION_REGISTRY));

        ConfigHandler.load(FabricLoader.getInstance().getConfigDir().resolve("tectonic.json"));

        Registry.register(Registry.DENSITY_FUNCTION_TYPES, idOf("config"), ConfigDensityFunction.CODEC);
        Registry.register(Registry.DENSITY_FUNCTION_TYPES, idOf("dynamic_reference"), DynamicReferenceDensityFunction.CODEC);
        Registry.register(Registry.DENSITY_FUNCTION_TYPES, idOf("erosion_noise"), ErosionNoiseDensityFunction.CODEC);
    }
}
