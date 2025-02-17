package dev.worldgen.tectonic.config.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ConfigCodec(boolean enabled, Toggles toggles, Scales scales, int snowOffset) {
    public static final Codec<ConfigCodec> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ConfigCodec::enabled),
        Toggles.CODEC.fieldOf("feature_toggles").orElse(Toggles.DEFAULT).forGetter(ConfigCodec::toggles),
        Scales.CODEC.fieldOf("terrain_scales").orElse(Scales.DEFAULT).forGetter(ConfigCodec::scales),
        Codec.INT.fieldOf("snow_start_offset").orElse(128).forGetter(ConfigCodec::snowOffset)
    ).apply(instance, ConfigCodec::new));

    public static final Codec<ConfigCodec> CODEC = Codec.withAlternative(BASE_CODEC, LegacyCodec.CODEC, LegacyCodec::upgrade);

    public double getValue(String option) {
        return switch (option) {
            case "final_multiplier" -> this.scales.finalMultiplier;
            case "erosion_scale" -> this.scales.erosionScale;
            case "ocean_depth" -> this.scales.ocean;
            case "deep_ocean_depth" -> this.scales.deepOcean;

            case "desert_dunes" -> this.toggles.dunes ? 1 : 0;
            case "underground_rivers" -> this.toggles.undergroundRivers ? 1 : 0;
            case "lava_rivers" -> this.toggles.lavaRivers ? 1 : 0;
            default -> 0;
        };
    }

    public record Toggles(String c1, String c2, boolean dunes, boolean undergroundRivers, boolean lavaRivers, boolean monumentOffset, boolean increasedHeight) {
        public static final String C1 = "Moves max height to y640";
        public static final String C2 = "Offsets ocean monuments 30 blocks down";
        public static final Codec<Toggles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("i__").orElse(C1).forGetter(Toggles::c1),
            Codec.STRING.fieldOf("m__").orElse(C2).forGetter(Toggles::c2),
            Codec.BOOL.fieldOf("desert_dunes").orElse(true).forGetter(Toggles::dunes),
            Codec.BOOL.fieldOf("underground_rivers").orElse(true).forGetter(Toggles::undergroundRivers),
            Codec.BOOL.fieldOf("lava_rivers").orElse(true).forGetter(Toggles::lavaRivers),
            Codec.BOOL.fieldOf("monument_offset").orElse(true).forGetter(Toggles::monumentOffset),
            Codec.BOOL.fieldOf("increased_height").orElse(false).forGetter(Toggles::increasedHeight)
        ).apply(instance, Toggles::new));
        public static final Toggles DEFAULT = new Toggles(true, true, true, true, false);

        public Toggles(boolean dunes, boolean undergroundRivers, boolean lavaRivers, boolean monumentOffset, boolean increasedHeight) {
            this(C1, C2, dunes, undergroundRivers, lavaRivers, monumentOffset, increasedHeight);
        }
    }

    public record Scales(String c1, String c2, String c3, String c4, double finalMultiplier, double erosionScale, double ocean, double deepOcean) {
        public static final String C1 = "Lower values = thicker mountain ranges and more space between ranges";
        public static final String C2 = "Vanilla value is -0.12";
        public static final String C3 = "Vanilla value is -0.22";
        public static final String C4 = "Higher values = vertically stretched terrain";
        public static final Codec<Scales> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("e__").orElse(C1).forGetter(Scales::c1),
            Codec.STRING.fieldOf("o__").orElse(C2).forGetter(Scales::c2),
            Codec.STRING.fieldOf("d__").orElse(C3).forGetter(Scales::c3),
            Codec.STRING.fieldOf("v__").orElse(C4).forGetter(Scales::c4),
            Codec.DOUBLE.fieldOf("vertical_multiplier").orElse(1.125).forGetter(Scales::finalMultiplier),
            Codec.DOUBLE.fieldOf("erosion_scale").orElse(0.25).forGetter(Scales::erosionScale),
            Codec.DOUBLE.fieldOf("ocean_depth").orElse(-0.15).forGetter(Scales::ocean),
            Codec.DOUBLE.fieldOf("deep_ocean_depth").orElse(-0.45).forGetter(Scales::deepOcean)
        ).apply(instance, Scales::new));
        public static final Scales DEFAULT = new Scales(1.125, 0.25, -0.15, -0.45);

        public Scales(double finalMultiplier, double erosionScale, double ocean, double deepOcean) {
            this(C1, C2, C3, C4, finalMultiplier, erosionScale, ocean, deepOcean);
        }
    }
}
