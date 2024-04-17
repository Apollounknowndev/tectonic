package dev.worldgen.tectonic;


import dev.worldgen.tectonic.config.ConfigHandler;
import dev.worldgen.tectonic.worldgen.ConfigDensityFunction;
import dev.worldgen.tectonic.worldgen.DynamicReferenceDensityFunction;
import dev.worldgen.tectonic.worldgen.ErosionNoiseDensityFunction;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod(Tectonic.MOD_ID)
public class TectonicNeoforge {
    private static final List<String> enabledPacks = new ArrayList<>();

    public TectonicNeoforge(IEventBus modEventBus) {
        Tectonic.init(RegistryCodecs.homogeneousList(Registries.DENSITY_FUNCTION));

        ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("tectonic.json"));

        ConfigHandler.getConfig().enablePacks(ModList.get().isLoaded("terralith"), enabledPacks::add);

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

    /**
     * Explanation of the Bullshit Fix™️
     * Neoforge loads built-in packs by their ID. tectonic/increased_height must load above tectonic/tectonic or tectonic/terratonic, but it comes alphabetically before them
     * The "fix" tacks a z on the ID, making the increased height pack's ID tectonic/zincreased_height
     * Neoforge is better than Forge, but I still hate it.
     */
    private void registerDatapack(final AddPackFindersEvent event, String packName) {
        boolean enableBullshitFix = packName.equals("increased_height");
        Path resourcePath = ModList.get().getModFileById("tectonic").getFile().findResource("resourcepacks/" + packName.toLowerCase());
        Pack dataPack = Pack.readMetaAndCreate("tectonic/" + (enableBullshitFix ? "z" : "") + packName.toLowerCase(), Component.translatable("pack_name.tectonic."+packName), false, createSupplier(new PathPackResources(resourcePath.getFileName().toString(), resourcePath, false)), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
        event.addRepositorySource((packConsumer) -> packConsumer.accept(dataPack));
    }

    protected static Pack.ResourcesSupplier createSupplier(final PackResources packResources) {
        return new Pack.ResourcesSupplier() {
            public @NotNull PackResources openPrimary(@NotNull String string) {
                return packResources;
            }

            public @NotNull PackResources openFull(@NotNull String string, Pack.@NotNull Info info) {
                return packResources;
            }
        };
    }
}