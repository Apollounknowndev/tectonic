package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
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
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static dev.worldgen.tectonic.Tectonic.idOf;

public class TectonicFabric implements ModInitializer {
    public static final List<Pack> bonusPacks = new ArrayList<>();
    public static Pack basePack = null;

    @Override
    public void onInitialize() {
        Tectonic.init(RegistryCodecs.homogeneousList(Registries.DENSITY_FUNCTION));

        ConfigHandler.load(FabricLoader.getInstance().getConfigDir().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(FabricLoader.getInstance().isModLoaded("terralith"), TectonicFabric::registerPack);

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, idOf("config"), ConfigDensityFunction.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, idOf("dynamic_reference"), DynamicReferenceDensityFunction.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, idOf("erosion_noise"), ErosionNoiseDensityFunction.CODEC);
    }

    private static void registerPack(String packName) {
        Path resourcePath = FabricLoader.getInstance().getModContainer("tectonic").get().findPath("resourcepacks/"+packName).get();
        Pack dataPack = Pack.readMetaAndCreate("tectonic/" + packName.toLowerCase(), Component.translatable("pack_name.tectonic."+packName), false, createSupplier(new PathPackResources(resourcePath.getFileName().toString(), resourcePath, false)), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);

        if (packName.endsWith("tonic")) {
            basePack = dataPack;
        } else {
            bonusPacks.add(dataPack);
        }
    }

    protected static Pack.ResourcesSupplier createSupplier(final PackResources packResources) {
        return new Pack.ResourcesSupplier() {
            public PackResources open(@NotNull String string) {
                return packResources;
            }

            public @NotNull PackResources openPrimary(@NotNull String string) {
                return packResources;
            }

            public @NotNull PackResources openFull(@NotNull String string, @NotNull Pack.Info info) {
                return packResources;
            }
        };
    }
}
