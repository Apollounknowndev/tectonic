package dev.worldgen.tectonic;

import com.mojang.serialization.MapCodec;
import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigDensityFunction;
import dev.worldgen.tectonic.worldgen.ErosionNoiseDensityFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.Optional;

import static dev.worldgen.tectonic.Tectonic.id;

@Mod(Tectonic.MOD_ID)
public class TectonicNeoforge {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Tectonic.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TectonicResourceCondition>> TECTONIC = CONDITION_TYPES.register("config", () -> TectonicResourceCondition.CODEC);

    public TectonicNeoforge(IEventBus modEventBus) {
        Tectonic.init(FMLPaths.CONFIGDIR.get().resolve("tectonic.json"));

        CONDITION_TYPES.register(modEventBus);

        modEventBus.addListener(this::registerDensityFunctionTypes);
        modEventBus.addListener(this::registerEnabledPacks);
    }

    private void registerDensityFunctionTypes(final RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> {
            helper.register(id("config"), ConfigDensityFunction.CODEC_HOLDER.codec());
            helper.register(id("erosion_noise"), ErosionNoiseDensityFunction.CODEC_HOLDER.codec());
        });
    }

    private void registerEnabledPacks(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA && ConfigHandler.getConfig().modEnabled()) {
            Path resourcePath = ModList.get().getModFileById("tectonic").getFile().findResource("resourcepacks/tectonic");

            Pack dataPack = Pack.readMetaAndCreate(
                new PackLocationInfo(
                    resourcePath.getFileName().toString(),
                    Component.literal("Tectonic"),
                    PackSource.BUILT_IN,
                    Optional.empty()
                ),
                new PathPackResources.PathResourcesSupplier(resourcePath),
                PackType.SERVER_DATA,
                new PackSelectionConfig(
                    true,
                    Pack.Position.TOP,
                    false
                )
            );

            event.addRepositorySource((packConsumer) -> packConsumer.accept(dataPack));
        }
    }
}