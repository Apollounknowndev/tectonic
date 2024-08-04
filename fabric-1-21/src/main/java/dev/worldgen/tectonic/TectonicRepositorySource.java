package dev.worldgen.tectonic;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TectonicRepositorySource implements RepositorySource {
    @Override
    public void loadPacks(@NotNull Consumer<Pack> consumer) {
        if (TectonicFabric.basePack != null) {
            consumer.accept(TectonicFabric.basePack);
        }

        TectonicFabric.bonusPacks.forEach(consumer);
    }
}