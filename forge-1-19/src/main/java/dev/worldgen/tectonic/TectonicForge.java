package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigDensityFunction;
import dev.worldgen.tectonic.worldgen.DynamicReferenceDensityFunction;
import dev.worldgen.tectonic.worldgen.ErosionNoiseDensityFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.resource.PathPackResources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod(Tectonic.MOD_ID)
public class TectonicForge {
    private static final List<String> enabledPacks = new ArrayList<>();
    
    public TectonicForge() {
        Tectonic.init(RegistryCodecs.homogeneousList(Registry.DENSITY_FUNCTION_REGISTRY));

        ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(ModList.get().isLoaded("terralith"), enabledPacks::add);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerDensityFunctionTypes);
        modEventBus.addListener(this::registerEnabledPacks);
    }

    private void registerDensityFunctionTypes(final RegisterEvent event) {
        event.register(Registry.DENSITY_FUNCTION_TYPE_REGISTRY, helper -> helper.register("config", ConfigDensityFunction.CODEC));
        event.register(Registry.DENSITY_FUNCTION_TYPE_REGISTRY, helper -> helper.register("dynamic_reference", DynamicReferenceDensityFunction.CODEC));
        event.register(Registry.DENSITY_FUNCTION_TYPE_REGISTRY, helper -> helper.register("erosion_noise", ErosionNoiseDensityFunction.CODEC));
    }

    private void registerEnabledPacks(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            for (String packName : enabledPacks) {
                registerDatapack(event, packName);
            }
        }
    }

    /**
     * Explanation of the Bullshit Fix™️
     * Forge loads built-in packs by their ID. tectonic/increased_height must load above tectonic/tectonic or tectonic/terratonic, but it comes alphabetically before them
     * The "fix" tacks a z on the ID, making the increased height pack's ID tectonic/zincreased_height
     * I hate Forge.
     */
    private void registerDatapack(final AddPackFindersEvent event, String packName) {
        boolean enableBullshitFix = packName.equals("increased_height");
        try {
            Path datapackPath = ModList.get().getModFileById("tectonic").getFile().findResource("resourcepacks/" + packName.toLowerCase());
            PathPackResources datapack = new PathPackResources(ModList.get().getModFileById("tectonic").getFile().getFileName() + ":" + datapackPath, datapackPath);
            PackMetadataSection mcmeta = datapack.getMetadataSection(PackMetadataSection.SERIALIZER);
            if (mcmeta != null) {
                event.addRepositorySource((consumer, constructor) -> consumer.accept(constructor.create("tectonic/" + (enableBullshitFix ? "z" : "") + packName.toLowerCase(), Component.literal(packName), true, () -> datapack, mcmeta, Pack.Position.TOP, PackSource.BUILT_IN)));
            }

        } catch (IOException var6) {
            throw new RuntimeException(var6);
        }
    }
}