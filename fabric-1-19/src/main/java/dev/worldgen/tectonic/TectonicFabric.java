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
import java.util.Collections;
import java.util.List;

import static dev.worldgen.tectonic.Tectonic.idOf;

public class TectonicFabric implements ModInitializer {
    private static final List<ModNioResourcePack> packs = new ArrayList<>();

    @Override
    public void onInitialize() {
        Tectonic.init(RegistryCodecs.homogeneousList(Registry.DENSITY_FUNCTION_REGISTRY));

        ConfigHandler.load(FabricLoader.getInstance().getConfigDir().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(FabricLoader.getInstance().isModLoaded("terralith"), TectonicFabric::registerPack);
        Collections.reverse(packs);

        Registry.register(Registry.DENSITY_FUNCTION_TYPES, idOf("config"), ConfigDensityFunction.CODEC_HOLDER.codec());
        Registry.register(Registry.DENSITY_FUNCTION_TYPES, idOf("dynamic_reference"), DynamicReferenceDensityFunction.CODEC_HOLDER.codec());
        Registry.register(Registry.DENSITY_FUNCTION_TYPES, idOf("erosion_noise"), ErosionNoiseDensityFunction.CODEC_HOLDER.codec());
    }

    public static List<ModNioResourcePack> getPacks() {
        return packs;
    }

    private static void registerPack(String packName) {
        ModContainer container = FabricLoader.getInstance().getModContainer("tectonic").get();
        ResourceLocation packId = idOf(packName.toLowerCase());
        String separator = container.getRootPath().getFileSystem().getSeparator();
        packs.add(
            ModNioResourcePack.create(
                packId, packName, container, ("resourcepacks/"+packId.getPath()).replace("/", separator), PackType.SERVER_DATA, ResourcePackActivationType.DEFAULT_ENABLED
            )
        );
    }
}
