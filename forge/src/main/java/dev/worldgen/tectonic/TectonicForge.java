package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigDensityFunction;
import dev.worldgen.tectonic.worldgen.ErosionNoiseDensityFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod(Tectonic.MOD_ID)
public class TectonicForge {
    private static final List<String> enabledPacks = new ArrayList<>();
    
    public TectonicForge() {
        Tectonic.init(FMLPaths.CONFIGDIR.get().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(ModList.get().isLoaded("terralith"), enabledPacks::add);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerDensityFunctionTypes);
        modEventBus.addListener(this::registerEnabledPacks);
    }

    private void registerDensityFunctionTypes(final RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> helper.register("config", ConfigDensityFunction.CODEC_HOLDER.codec()));
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> helper.register("erosion_noise", ErosionNoiseDensityFunction.CODEC_HOLDER.codec()));
    }

    private void registerEnabledPacks(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            for (String packName : enabledPacks) {
                registerDatapack(event, packName);
            }
        }
    }

    private void registerDatapack(final AddPackFindersEvent event, String packName) {
        boolean increasedHeight = packName.startsWith("increased_height");
        Path resourcePath = ModList.get().getModFileById("tectonic").getFile().findResource("resourcepacks/" + packName.toLowerCase());
        Pack dataPack = Pack.readMetaAndCreate("tectonic/" + (increasedHeight ? "z" : "") + packName.toLowerCase(), Component.translatable("pack_name.tectonic."+packName), false, string -> new PathPackResources(resourcePath.getFileName().toString(), resourcePath, false), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
        event.addRepositorySource((packConsumer) -> packConsumer.accept(dataPack));
    }
}