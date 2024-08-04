package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.mixin.BuiltInPackSourceAccessor;
import dev.worldgen.tectonic.worldgen.ConfigDensityFunction;
import dev.worldgen.tectonic.worldgen.DynamicReferenceDensityFunction;
import dev.worldgen.tectonic.worldgen.ErosionNoiseDensityFunction;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.worldgen.tectonic.Tectonic.idOf;

public class TectonicFabric implements ModInitializer {
    public static final List<Pack> bonusPacks = new ArrayList<>();
    public static Pack basePack = null;

    @Override
    public void onInitialize() {
        Tectonic.init(RegistryCodecs.homogeneousList(Registries.DENSITY_FUNCTION));

        ConfigHandler.load(FabricLoader.getInstance().getConfigDir().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(FabricLoader.getInstance().isModLoaded("terralith"), TectonicFabric::registerPack);

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, idOf("config"), ConfigDensityFunction.CODEC_HOLDER.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, idOf("dynamic_reference"), DynamicReferenceDensityFunction.CODEC_HOLDER.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, idOf("erosion_noise"), ErosionNoiseDensityFunction.CODEC_HOLDER.codec());
    }

    private static void registerPack(String packName) {
        Path resourcePath = FabricLoader.getInstance().getModContainer("tectonic").get().findPath("resourcepacks/"+packName).get();

        PackLocationInfo locationInfo = new PackLocationInfo(
            resourcePath.getFileName().toString(),
            Component.translatable("pack_name.tectonic."+packName),
            PackSource.BUILT_IN,
            Optional.empty()
        );

        PackSelectionConfig selectionConfig = new PackSelectionConfig(
            true,
            Pack.Position.TOP,
            false
        );

        Pack dataPack = Pack.readMetaAndCreate(
            locationInfo,
            BuiltInPackSourceAccessor.createSupplier(new PathPackResources(locationInfo, resourcePath)),
            PackType.SERVER_DATA,
            selectionConfig
        );

        if (packName.endsWith("tonic")) {
            basePack = dataPack;
        } else {
            bonusPacks.add(dataPack);
        }
    }
}
