package dev.worldgen.tectonic.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.object.HeightLimits;
import dev.worldgen.tectonic.config.object.NoiseState;

public class V2ConfigState {
    public static final Codec<V2ConfigState> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(config -> config.enabled),
        Toggles.CODEC.fieldOf("feature_toggles").orElse(Toggles.DEFAULT).forGetter(config -> config.toggles),
        Scales.CODEC.fieldOf("terrain_scales").orElse(Scales.DEFAULT).forGetter(config -> config.scales),
        Codec.INT.fieldOf("snow_start_offset").orElse(128).forGetter(config -> config.snowOffset)
    ).apply(instance, V2ConfigState::new));
    public static final Codec<V2ConfigState> CODEC = Codec.withAlternative(BASE_CODEC, V1ConfigState.CODEC, V1ConfigState::upgrade);

    public boolean enabled;
    public Toggles toggles;
    public Scales scales;
    public int snowOffset;

    public V2ConfigState(boolean enabled, Toggles toggles, Scales scales, int snowOffset) {
        this.enabled = enabled;
        this.toggles = toggles;
        this.scales = scales;
        this.snowOffset = snowOffset;
    }

    public ConfigState upgrade() {
        return new ConfigState(
            ConfigState.MINOR_VERSION,
            new ConfigState.General(
                this.enabled,
                this.snowOffset
            ),
            new ConfigState.GlobalTerrain(
                this.scales.finalMultiplier,
                0,
                HeightLimits.defaultLimits(this.toggles.increasedHeight),
                this.toggles.lavaRivers,
                false
            ),
            new ConfigState.Continents(
                -0.8,
                0.13,
                this.scales.erosionScale,
                0.25,
                this.toggles.undergroundRivers,
                true,
                false,
                0.1,
                true,
                true
            ),
            ConfigState.Islands.DEFAULT,
            new ConfigState.Oceans(
                this.scales.ocean,
                this.scales.deepOcean,
                this.toggles.monumentOffset ? -30 : 0, // Monument offset now int
                false
            ),
            new ConfigState.Biomes(
                NoiseState.create(),
                NoiseState.create()
            ),
            ConfigState.Caves.DEFAULT,
            ConfigState.Experimental.DEFAULT
        );
    }


    public static class Toggles {
        public static final Codec<Toggles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("desert_dunes").orElse(true).forGetter(toggles -> toggles.dunes),
            Codec.BOOL.fieldOf("underground_rivers").orElse(true).forGetter(toggles -> toggles.undergroundRivers),
            Codec.BOOL.fieldOf("lava_rivers").orElse(true).forGetter(toggles -> toggles.lavaRivers),
            Codec.BOOL.fieldOf("monument_offset").orElse(true).forGetter(toggles -> toggles.monumentOffset),
            Codec.BOOL.fieldOf("increased_height").orElse(false).forGetter(toggles -> toggles.increasedHeight)
        ).apply(instance, Toggles::new));
        public static final Toggles DEFAULT = new Toggles(true, true, true, true, false);

        public boolean dunes;
        public boolean undergroundRivers;
        public boolean lavaRivers;
        public boolean monumentOffset;
        public boolean increasedHeight;

        public Toggles(boolean dunes, boolean undergroundRivers, boolean lavaRivers, boolean monumentOffset, boolean increasedHeight) {
            this.dunes = dunes;
            this.undergroundRivers = undergroundRivers;
            this.lavaRivers = lavaRivers;
            this.monumentOffset = monumentOffset;
            this.increasedHeight = increasedHeight;
        }
    }

    public static class Scales {
        public static final Codec<Scales> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("vertical_multiplier").orElse(1.125).forGetter(scales -> scales.finalMultiplier),
            Codec.DOUBLE.fieldOf("erosion_scale").orElse(0.25).forGetter(scales -> scales.erosionScale),
            Codec.DOUBLE.fieldOf("ocean_depth").orElse(-0.15).forGetter(scales -> scales.ocean),
            Codec.DOUBLE.fieldOf("deep_ocean_depth").orElse(-0.45).forGetter(scales -> scales.deepOcean)
        ).apply(instance, Scales::new));
        public static final Scales DEFAULT = new Scales(1.125, 0.25, -0.15, -0.45);

        public double finalMultiplier;
        public double erosionScale;
        public double ocean;
        public double deepOcean;

        public Scales(double finalMultiplier, double erosionScale, double ocean, double deepOcean) {
            this.finalMultiplier = finalMultiplier;
            this.erosionScale = erosionScale;
            this.ocean = ocean;
            this.deepOcean = deepOcean;
        }
    }
}
