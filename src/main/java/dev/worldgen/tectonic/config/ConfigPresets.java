package dev.worldgen.tectonic.config;

import dev.worldgen.tectonic.config.object.HeightLimits;
import dev.worldgen.tectonic.config.object.NoiseState;
import org.apache.logging.log4j.util.TriConsumer;

public interface ConfigPresets {
    ConfigState DEFAULT = new ConfigState(
        ConfigState.MINOR_VERSION,
        ConfigState.General.DEFAULT,
        ConfigState.GlobalTerrain.DEFAULT,
        ConfigState.Continents.DEFAULT,
        ConfigState.Islands.DEFAULT,
        ConfigState.Oceans.DEFAULT,
        ConfigState.Biomes.DEFAULT,
        ConfigState.Caves.DEFAULT,
        ConfigState.Experimental.DEFAULT
    );

    ConfigState LARGE_BIOMES = new ConfigState(
        ConfigState.MINOR_VERSION,
        ConfigState.General.DEFAULT,
        ConfigState.GlobalTerrain.DEFAULT,
        ConfigState.Continents.DEFAULT,
        ConfigState.Islands.DEFAULT,
        ConfigState.Oceans.DEFAULT,
        new ConfigState.Biomes(
            new NoiseState(0.06f, 1.1f, 0),
            new NoiseState(0.06f, 1.1f, 0)
        ),
        ConfigState.Caves.DEFAULT,
        ConfigState.Experimental.DEFAULT
    );

    ConfigState DESERTED = new ConfigState(
        ConfigState.MINOR_VERSION,
        ConfigState.General.DEFAULT,
        ConfigState.GlobalTerrain.DEFAULT,
        ConfigState.Continents.DEFAULT,
        ConfigState.Islands.DEFAULT,
        ConfigState.Oceans.DEFAULT,
        new ConfigState.Biomes(
            new NoiseState(0, 0, 1),
            NoiseState.create()
        ),
        ConfigState.Caves.DEFAULT,
        ConfigState.Experimental.DEFAULT
    );

    ConfigState FROZEN_WASTELAND = new ConfigState(
        ConfigState.MINOR_VERSION,
        ConfigState.General.DEFAULT,
        new ConfigState.GlobalTerrain(
            1.2f,
            0.5f,
            new HeightLimits(-64, 448),
            true,
            false
        ),
        new ConfigState.Continents(
            -0.8f,
            0.13f,
            0.25f,
            0.25f,
            true,
            true,
            true,
            0.0f,
            true,
            false
        ),
        ConfigState.Islands.DEFAULT,
        ConfigState.Oceans.DEFAULT,
        new ConfigState.Biomes(
            new NoiseState(0, 0, -0.69f),
            new NoiseState(0.15f, 0.1f, -0.1f)
        ),
        ConfigState.Caves.DEFAULT,
        ConfigState.Experimental.DEFAULT
    );
    
    ConfigState OVERKILL = new ConfigState(
        ConfigState.MINOR_VERSION,
        new ConfigState.General(
            true,
            512
        ),
        new ConfigState.GlobalTerrain(
            2.5f,
            1.6f,
            new HeightLimits(-64, 768),
            false,
            true
        ),
        new ConfigState.Continents(
            -0.5f,
            0.1f,
            0.08f,
            0.2f,
            false,
            false,
            false,
            0.5f,
            false,
            false
        ),
        ConfigState.Islands.DEFAULT,
        ConfigState.Oceans.DEFAULT,
        new ConfigState.Biomes(
            new NoiseState(0.1f, 1.1f, 0),
            new NoiseState(0.1f, 1.1f, -0.2f)
        ),
        ConfigState.Caves.DEFAULT,
        new ConfigState.Experimental(true, true)
    );

    static void acceptPresets(TriConsumer<String, ConfigState, Integer> consumer) {
        consumer.accept("default", ConfigPresets.DEFAULT, 0xffffff);
        consumer.accept("large_biomes", ConfigPresets.LARGE_BIOMES, 0x88ff99);
        consumer.accept("deserted", ConfigPresets.DESERTED, 0xe2ca76);
        consumer.accept("frozen_wasteland", ConfigPresets.FROZEN_WASTELAND, 0xc1eaff);
        consumer.accept("overkill", ConfigPresets.OVERKILL, 0xEA6F6F);
    }
}
