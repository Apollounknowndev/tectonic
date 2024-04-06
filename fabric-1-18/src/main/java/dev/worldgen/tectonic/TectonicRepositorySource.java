package dev.worldgen.tectonic;

import dev.worldgen.tectonic.config.ConfigHandler;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TectonicRepositorySource implements RepositorySource {

    @Override
    public void loadPacks(@NotNull Consumer<Pack> consumer, @NotNull Pack.PackConstructor constructor) {
        List<String> tectonicBuiltInPackNames = new ArrayList<>();
        ConfigHandler.getConfig().enablePacks(FabricLoader.getInstance().isModLoaded("terralith"), tectonicBuiltInPackNames::add);
        Collections.reverse(tectonicBuiltInPackNames);

        for (String packName : tectonicBuiltInPackNames) {
            Pack tectonicPack = Pack.create(
                "tectonic/"+packName.toLowerCase(),
                true,
                () -> new TectonicBuiltInResourcePack(
                    PackType.SERVER_DATA,
                    List.of(ModNioResourcePack.create(packName, FabricLoader.getInstance().getModContainer("tectonic").get(), "resourcepacks/"+packName.toLowerCase(), PackType.SERVER_DATA, ResourcePackActivationType.ALWAYS_ENABLED)),
                    packName,
                    "",
                    "resourcepacks/"+packName.toLowerCase()+"/pack.png"
                ),
                constructor,
                Pack.Position.TOP,
                PackSource.BUILT_IN
            );

            consumer.accept(tectonicPack);
        }
    }
}