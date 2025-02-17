package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigConstant;
import dev.worldgen.tectonic.worldgen.ConfigNoise;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static dev.worldgen.tectonic.Tectonic.id;

public class TectonicFabric implements ModInitializer {
    public static final List<Pack> bonusPacks = new ArrayList<>();
    public static Pack basePack = null;

    @Override
    public void onInitialize() {
        Tectonic.init(FabricLoader.getInstance().getConfigDir().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(FabricLoader.getInstance().isModLoaded("terralith"), TectonicFabric::registerPack);

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("config"), ConfigConstant.CODEC_HOLDER.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("config_noise"), ConfigNoise.CODEC_HOLDER.codec());
    }

    private static void registerPack(String packName) {
        Path resourcePath = FabricLoader.getInstance().getModContainer("tectonic").get().findPath("resourcepacks/"+packName).get();
        Pack dataPack = Pack.readMetaAndCreate("tectonic/" + packName.toLowerCase(), Component.translatable("pack_name.tectonic."+packName), false, string -> new PathPackResources(resourcePath.getFileName().toString(), resourcePath, false), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);

        if (packName.equals("terratonic") || packName.equals("tectonic")) {
            basePack = dataPack;
        } else {
            bonusPacks.add(dataPack);
        }
    }
}
