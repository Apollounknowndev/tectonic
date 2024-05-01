package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigDensityFunction;
import dev.worldgen.tectonic.worldgen.DynamicReferenceDensityFunction;
import dev.worldgen.tectonic.worldgen.ErosionNoiseDensityFunction;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mod(Tectonic.MOD_ID)
public class TectonicNeoforge {
    private static final List<String> enabledPacks = new ArrayList<>();

    public TectonicNeoforge(IEventBus modEventBus) {
        Tectonic.init(RegistryCodecs.homogeneousList(Registries.DENSITY_FUNCTION));

        ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(ModList.get().isLoaded("terralith"), enabledPacks::add);
        Collections.reverse(enabledPacks);

        modEventBus.addListener(this::registerDensityFunctionTypes);
        modEventBus.addListener(this::registerEnabledPacks);
    }

    private void registerDensityFunctionTypes(final RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> helper.register(Tectonic.idOf("config"), ConfigDensityFunction.CODEC_HOLDER.codec()));
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> helper.register(Tectonic.idOf("dynamic_reference"), DynamicReferenceDensityFunction.CODEC_HOLDER.codec()));
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> helper.register(Tectonic.idOf("erosion_noise"), ErosionNoiseDensityFunction.CODEC_HOLDER.codec()));
    }

    private void registerEnabledPacks(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            for (String packName : enabledPacks) {
                registerDatapack(event, packName);
            }
        }
    }

    private void registerDatapack(final AddPackFindersEvent event, String packName) {
        Path resourcePath = ModList.get().getModFileById("tectonic").getFile().findResource("resourcepacks/" + packName.toLowerCase());

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
            createSupplier(new PathPackResources(locationInfo, resourcePath)),
            PackType.SERVER_DATA,
            selectionConfig
        );

        event.addRepositorySource((packConsumer) -> packConsumer.accept(dataPack));
    }

    private static Pack.ResourcesSupplier createSupplier(final PackResources packResources) {
        return new Pack.ResourcesSupplier() {
            public PackResources openPrimary(PackLocationInfo locationInfo) {
                return packResources;
            }

            public PackResources openFull(PackLocationInfo locationInfo, Pack.Metadata metadata) {
                return packResources;
            }
        };
    }
}