package dev.worldgen.tectonic.platform.neoforge;

//? if neoforge {
/*import dev.worldgen.tectonic.Tectonic;
import dev.worldgen.tectonic.command.TectonicCommand;
import dev.worldgen.tectonic.registry.TectonicRegistrations;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
//? if >= 26.1 {
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforge.resource.JarContentsPackResources;
//? }
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Mod(Tectonic.MOD_ID)
public class TectonicNeoforge {
    public TectonicNeoforge(IEventBus bus) {
        TectonicRegistrations.register(NeoForgeRegistries.CONDITION_SERIALIZERS, "config", ConfigResourceCondition.CODEC);
        Tectonic.init();
        
        bus.addListener(this::registerEnabledPacks);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerEnabledPacks(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA && Tectonic.CONFIG.getState().enabled()) {
            //? if < 26.1 {
            /^Path resourcePath = ModList.get().getModFileById("tectonic").getFile().findResource("resourcepacks/tectonic");
            
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
	        ^///? } else {
            IModInfo modInfo = ModList.get().getModContainerById(Tectonic.MOD_ID).orElseThrow().getModInfo();
            BiFunction<PackLocationInfo, String, PackResources> resourceGetter = (info, prefix) -> {
                JarContents contents = modInfo.getOwningFile().getFile().getContents();
                return new JarContentsPackResources(info, contents, prefix);
            };
            String path = "resourcepacks/tectonic";
            
            Pack pack = Pack.readMetaAndCreate(
                new PackLocationInfo("mod/tectonic:tectonic", Component.literal("Tectonic"), PackSource.BUILT_IN, Optional.empty()),
                new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openPrimary(PackLocationInfo info) {
                        return resourceGetter.apply(info, path);
                    }
                    
                    @Override
                    public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                        PackResources baseResources = resourceGetter.apply(info, path);
                        List<String> overlays = metadata.overlays();
                        if (overlays.isEmpty()) {
                            return baseResources;
                        } else {
                            List<PackResources> effectiveOverlays = new ArrayList<>(overlays.size());
                            
                            for(String s : overlays) {
                                effectiveOverlays.add(resourceGetter.apply(info, path + "/" + s));
                            }
                            
                            return new CompositePackResources(baseResources, effectiveOverlays);
                        }
                    }
                },
                PackType.SERVER_DATA,
                new PackSelectionConfig(true, Pack.Position.TOP, false)
            );
            event.addRepositorySource(consumer -> consumer.accept(pack));
            //? }
        }
    }

    private void registerCommands(final RegisterCommandsEvent event) {
        TectonicCommand.register(event.getDispatcher());
    }
}
*///? }