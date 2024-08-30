package dev.worldgen.tectonic.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

// __X fields are for comments. It's a disgusting hack, but I don't particularly care
public record ConfigCodec(boolean modEnabled, Legacy legacyModule, Features featuresModule, Experimental experimentalModule) {
    public static final Codec<ConfigCodec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("mod_enabled").orElse(true).forGetter(ConfigCodec::modEnabled),
        Legacy.CODEC.fieldOf("legacy").orElse(Legacy.DEFAULT).forGetter(ConfigCodec::legacyModule),
        Features.CODEC.fieldOf("features").orElse(Features.DEFAULT).forGetter(ConfigCodec::featuresModule),
        Experimental.CODEC.fieldOf("experimental").orElse(Experimental.DEFAULT).forGetter(ConfigCodec::experimentalModule)
    ).apply(instance, ConfigCodec::new));

    private List<String> getEnabledPacks(boolean terralithEnabled) {
        List<String> enabledPacks = new ArrayList<>();
        if (this.modEnabled()) {
            if (this.legacyModule().enabled()) {
                enabledPacks.add("legacy");
            }
            if (this.experimentalModule().increasedHeight()) {
                enabledPacks.add(terralithEnabled ? "increased_height_terratonic": "increased_height");
            }
            enabledPacks.add(terralithEnabled ? "terratonic": "tectonic");
        }

        return enabledPacks;
    }

    public void enablePacks(boolean terralithEnabled, Consumer<String> registerPack) {
        for (String packName : ConfigHandler.getConfig().getEnabledPacks(terralithEnabled)) {
            registerPack.accept(packName);
        }
    }

    public double getValue(String option) {
        return switch (option) {
            case "terrain_scale" -> this.experimentalModule().terrainScale();
            case "horizontal_mountain_scale" -> this.experimentalModule().horizontalMountainScale();

            case "deeper_oceans" -> this.featuresModule().deeperOceans() ? 1 : 0;
            case "desert_dunes" -> this.featuresModule().desertDunes() ? 1 : 0;
            case "lava_rivers" -> this.featuresModule().lavaRivers() ? 1 : 0;
            case "underground_rivers" -> this.featuresModule().undergroundRivers() ? 1 : 0;
            default -> 0;
        };
    }

    public record Legacy(String commentA, String commentB, String commentC, boolean enabled) {
        private static final String COMMENT_A = "Tectonic v1 worlds have old biome data preventing them from being opened in Tectonic v2.1+.";
        private static final String COMMENT_B = "Enabling legacy mode will add back the biomes and upgrade worlds to the new format upon opening them.";
        private static final String COMMENT_C = "Once a world is upgraded by opening it, turn off legacy mode.";
        public static final Codec<Legacy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("__A").orElse(COMMENT_A).forGetter(Legacy::commentA),
            Codec.STRING.fieldOf("__B").orElse(COMMENT_B).forGetter(Legacy::commentB),
            Codec.STRING.fieldOf("__C").orElse(COMMENT_C).forGetter(Legacy::commentC),
            Codec.BOOL.fieldOf("enabled").orElse(false).forGetter(Legacy::enabled)
        ).apply(instance, Legacy::new));
        public static final Legacy DEFAULT = new Legacy(COMMENT_A, COMMENT_B, COMMENT_C, false);
    }



    public record Features(String commentA, String commentB, boolean deeperOceans, boolean desertDunes, boolean lavaRivers, int snowStartOffset, boolean undergroundRivers) {
        private static final String COMMENT_A = "Enabling deeper oceans will lower vanilla ocean monuments to compensate for lower depth.";
        private static final String COMMENT_B = "Snow start offset moves where snow starts, preventing biomes like Taigas looking weird next to mountain ranges.";
        public static final Codec<Features> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("__A").orElse(COMMENT_A).forGetter(Features::commentA),
                Codec.STRING.fieldOf("__B").orElse(COMMENT_B).forGetter(Features::commentB),
                Codec.BOOL.fieldOf("deeper_oceans").orElse(true).forGetter(Features::deeperOceans),
                Codec.BOOL.fieldOf("desert_dunes").orElse(true).forGetter(Features::desertDunes),
                Codec.BOOL.fieldOf("lava_rivers").orElse(true).forGetter(Features::lavaRivers),
                Codec.INT.fieldOf("snow_start_offset").orElse(128).forGetter(Features::snowStartOffset),
                Codec.BOOL.fieldOf("underground_rivers").orElse(true).forGetter(Features::undergroundRivers)
        ).apply(instance, Features::new));
        public static final Features DEFAULT = new Features(COMMENT_A, COMMENT_B, true, true, true, 128, true);
    }



    public record Experimental(String commentA, String commentB, String commentC, String commentD, boolean increasedHeight, double horizontalMountainScale, double terrainScale) {
        private static final String COMMENT_A = "The increased height setting will change the max Overworld build and generation height to y640.";
        private static final String COMMENT_B = "The horizontal mountain scale setting will change the thickness of mountain ranges and the spacing between them.";
        private static final String COMMENT_C = "Lower values = thicker mountain ranges and more space between ranges. 0.15-0.25 is the sweet spot.";
        private static final String COMMENT_D = "The terrain scale setting will vertically stretch/compress terrain. Higher values = more extreme terrain heights.";

        public static final Codec<Experimental> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("__A").orElse(COMMENT_A).forGetter(Experimental::commentA),
            Codec.STRING.fieldOf("__B").orElse(COMMENT_B).forGetter(Experimental::commentB),
            Codec.STRING.fieldOf("__C").orElse(COMMENT_C).forGetter(Experimental::commentC),
            Codec.STRING.fieldOf("__D").orElse(COMMENT_D).forGetter(Experimental::commentD),
            Codec.BOOL.fieldOf("increased_height").orElse(false).forGetter(Experimental::increasedHeight),
            Codec.DOUBLE.fieldOf("horizontal_mountain_scale").orElse(0.25).forGetter(Experimental::horizontalMountainScale),
            Codec.DOUBLE.fieldOf("terrain_scale").orElse(1.125).forGetter(Experimental::terrainScale)
        ).apply(instance, Experimental::new));
        public static final Experimental DEFAULT = new Experimental(COMMENT_A, COMMENT_B, COMMENT_C, COMMENT_D, false, 0.25, 1.125);
    }
}
