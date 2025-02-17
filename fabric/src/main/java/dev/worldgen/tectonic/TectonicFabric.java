package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigConstant;
import dev.worldgen.tectonic.worldgen.ConfigNoise;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import static dev.worldgen.tectonic.Tectonic.id;

public class TectonicFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Tectonic.init(FabricLoader.getInstance().getConfigDir().resolve("tectonic.json"));
        ResourceConditions.register(ConfigResourceCondition.TYPE);

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("config"), ConfigConstant.CODEC_HOLDER.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("config_noise"), ConfigNoise.CODEC_HOLDER.codec());

        if (ConfigHandler.getConfig().enabled()) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                id("tectonic"),
                FabricLoader.getInstance().getModContainer("tectonic").get(),
                Component.literal("Tectonic"),
                ResourcePackActivationType.ALWAYS_ENABLED
            );
        }
    }
}
