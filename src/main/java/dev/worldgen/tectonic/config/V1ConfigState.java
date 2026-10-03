package dev.worldgen.tectonic.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record V1ConfigState(boolean modEnabled, Features features, Experimental experimental) {
    public static final Codec<V1ConfigState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("mod_enabled").orElse(true).forGetter(V1ConfigState::modEnabled),
        Features.CODEC.fieldOf("features").orElse(Features.DEFAULT).forGetter(V1ConfigState::features),
        Experimental.CODEC.fieldOf("experimental").orElse(Experimental.DEFAULT).forGetter(V1ConfigState::experimental)
    ).apply(instance, V1ConfigState::new));

    public V2ConfigState upgrade() {
        return new V2ConfigState(
            modEnabled,
            new V2ConfigState.Toggles(
                features.desertDunes,
                features.undergroundRivers,
                features.lavaRivers,
                features.deeperOceans,
                experimental.increasedHeight
            ),
            new V2ConfigState.Scales(
                experimental.terrainScale,
                experimental.horizontalMountainScale,
                features.deeperOceans ? -0.15f : -0.12f,
                features.deeperOceans ? -0.45f : -0.22f
            ),
            features.snowStartOffset
        );
    }

    public record Features(boolean deeperOceans, boolean desertDunes, boolean lavaRivers, int snowStartOffset, boolean undergroundRivers) {
        public static final Codec<Features> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("deeper_oceans").orElse(true).forGetter(Features::deeperOceans),
            Codec.BOOL.fieldOf("desert_dunes").orElse(true).forGetter(Features::desertDunes),
            Codec.BOOL.fieldOf("lava_rivers").orElse(true).forGetter(Features::lavaRivers),
            Codec.INT.fieldOf("snow_start_offset").orElse(128).forGetter(Features::snowStartOffset),
            Codec.BOOL.fieldOf("underground_rivers").orElse(true).forGetter(Features::undergroundRivers)
        ).apply(instance, Features::new));

        public static final Features DEFAULT = new Features(true, true, true, 128, true);
    }



    private record Experimental(float horizontalMountainScale, boolean increasedHeight, float terrainScale) {
        public static final Codec<Experimental> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("horizontal_mountain_scale").orElse(0.25f).forGetter(Experimental::horizontalMountainScale),
            Codec.BOOL.fieldOf("increased_height").orElse(false).forGetter(Experimental::increasedHeight),
            Codec.FLOAT.fieldOf("terrain_scale").orElse(1.125f).forGetter(Experimental::terrainScale)
        ).apply(instance, Experimental::new));

        public static final Experimental DEFAULT = new Experimental(0.25f, false, 1.125f);
    }
}
