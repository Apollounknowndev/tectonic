package dev.worldgen.tectonic.config.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LegacyCodec(boolean modEnabled, Features features, Experimental experimental) {
    public static final Codec<LegacyCodec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("mod_enabled").orElse(true).forGetter(LegacyCodec::modEnabled),
        Features.CODEC.fieldOf("features").orElse(Features.DEFAULT).forGetter(LegacyCodec::features),
        Experimental.CODEC.fieldOf("experimental").orElse(Experimental.DEFAULT).forGetter(LegacyCodec::experimental)
    ).apply(instance, LegacyCodec::new));

    public ConfigCodec upgrade() {
        return new ConfigCodec(
            modEnabled,
            new ConfigCodec.Toggles(
                features.desertDunes,
                features.undergroundRivers,
                features.lavaRivers,
                features.deeperOceans,
                experimental.increasedHeight
            ),
            new ConfigCodec.Scales(
                experimental.terrainScale,
                experimental.horizontalMountainScale,
                features.deeperOceans ? -0.15 : -0.12,
                features.deeperOceans ? -0.45 : -0.22
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



    public record Experimental(double horizontalMountainScale, boolean increasedHeight, double terrainScale) {
        public static final Codec<Experimental> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("horizontal_mountain_scale").orElse(0.25).forGetter(Experimental::horizontalMountainScale),
            Codec.BOOL.fieldOf("increased_height").orElse(false).forGetter(Experimental::increasedHeight),
            Codec.DOUBLE.fieldOf("terrain_scale").orElse(1.125).forGetter(Experimental::terrainScale)
        ).apply(instance, Experimental::new));

        public static final Experimental DEFAULT = new Experimental(0.25, false, 1.125);
    }
}
