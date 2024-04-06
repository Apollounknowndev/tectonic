package dev.worldgen.tectonic;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TectonicRepositorySource implements RepositorySource {

    @Override
    public void loadPacks(@NotNull Consumer<Pack> consumer, @NotNull Pack.PackConstructor constructor) {
        TectonicFabric.getPacks().forEach(modNio -> {
            Pack pack = Pack.create(
                modNio.getName(), true, () -> modNio, constructor, Pack.Position.TOP, PackSource.BUILT_IN
            );
            consumer.accept(pack);
        });
    }
}