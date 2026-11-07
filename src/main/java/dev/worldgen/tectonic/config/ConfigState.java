package dev.worldgen.tectonic.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.apollib.config.ApollibCopyable;
import dev.worldgen.tectonic.config.object.HeightLimits;
import dev.worldgen.tectonic.config.object.NoiseState;

import static dev.worldgen.apollib.codec.ApollibCodecs.commented;

public class ConfigState implements ApollibCopyable<ConfigState> {
    public static ConfigState DEFAULT_STATE = new ConfigState(
        ConfigState.MINOR_VERSION,
        General.DEFAULT,
        GlobalTerrain.DEFAULT,
        Continents.DEFAULT,
        Islands.DEFAULT,
        Oceans.DEFAULT,
        Biomes.DEFAULT,
        Caves.DEFAULT,
        Experimental.DEFAULT
    );
    public static final int MINOR_VERSION = 1;
    public static final Codec<ConfigState> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("minor_version").orElse(0).forGetter(state -> MINOR_VERSION),
        General.CODEC.fieldOf("general").forGetter(state -> state.general),
        GlobalTerrain.CODEC.fieldOf("global_terrain").orElse(GlobalTerrain.DEFAULT).forGetter(state -> state.globalTerrain),
        Continents.CODEC.fieldOf("continents").orElse(Continents.DEFAULT).forGetter(state -> state.continents),
        Islands.CODEC.fieldOf("islands").orElse(Islands.DEFAULT).forGetter(state -> state.islands),
        Oceans.CODEC.fieldOf("oceans").orElse(Oceans.DEFAULT).forGetter(state -> state.oceans),
        Biomes.CODEC.fieldOf("biomes").orElse(Biomes.DEFAULT).forGetter(state -> state.biomes),
        Caves.CODEC.fieldOf("caves").orElse(Caves.DEFAULT).forGetter(state -> state.caves),
        Experimental.CODEC.fieldOf("experimental").orElse(Experimental.DEFAULT).forGetter(state -> state.experimental)
    ).apply(instance, ConfigState::create));
    public static final Codec<ConfigState> CODEC = Codec.withAlternative(BASE_CODEC, V2ConfigState.CODEC, V2ConfigState::upgrade);

    public General general;
    public GlobalTerrain globalTerrain;
    public Continents continents;
    public Islands islands;
    public Oceans oceans;
    public Biomes biomes;
    public Caves caves;
    public Experimental experimental;
    
    public static ConfigState create(int minorVersion, General general, GlobalTerrain globalTerrain, Continents continents, Islands islands, Oceans oceans, Biomes biomes, Caves caves, Experimental experimental) {
        return new ConfigState(minorVersion, general, globalTerrain, continents, islands, oceans, biomes, caves, experimental);
    }

    public ConfigState(int minorVersion, General general, GlobalTerrain globalTerrain, Continents continents, Islands islands, Oceans oceans, Biomes biomes, Caves caves, Experimental experimental) {
        this.general = general;
        this.globalTerrain = globalTerrain;
        this.continents = continents;
        this.islands = islands;
        this.oceans = oceans;
        this.biomes = biomes;
        this.caves = caves;
        this.experimental = experimental;

        if (minorVersion < 1 && this.globalTerrain.ultrasmooth) {
            this.globalTerrain.heightLimits = HeightLimits.INCREASED_HEIGHT;
        }
    }
    
    public boolean enabled() {
        return this.general.modEnabled;
    }

    public float getValue(String option) {
        return switch (option) {
            case "vertical_scale" -> this.globalTerrain.verticalScale;
            case "elevation_boost" -> this.globalTerrain.elevationBoost;
            case "min_y" -> this.globalTerrain.heightLimits.minY;
            case "max_y" -> this.globalTerrain.heightLimits.maxY;
            case "lava_tunnels" -> this.globalTerrain.lavaTunnels ? 1 : 0;

            case "ocean_offset" -> this.continents.oceanOffset;
            case "alternate_erosion_scale" -> this.continents.erosionScale * 4;
            case "underground_rivers" -> this.continents.undergroundRivers ? -1 : 0;
            case "flat_terrain_skew" -> this.continents.flatTerrainSkew;
            case "rolling_hills" -> this.continents.rollingHills ? 1 : 0;
            case "jungle_pillars" -> this.continents.junglePillars ? 1 : 0;

            case "ocean_depth" -> this.oceans.oceanDepth;
            case "deep_ocean_depth" -> this.oceans.deepOceanDepth;

            case "depth_cutoff_start" -> this.caves.depthCutoffStart;
            case "depth_cutoff_size" -> this.caves.depthCutoffSize;
            case "cheese_enabled" -> this.caves.cheeseEnabled ? 1 : 0;
            case "cheese_additive" -> this.caves.cheeseAdditive;

            case "noodle_enabled" -> this.caves.noodleEnabled ? 1 : 0;
            case "noodle_additive" -> this.caves.noodleAdditive;

            default -> 0;
        };
    }

    public NoiseState getNoiseState(String option) {
        return switch (option) {
            case "continents" -> new NoiseState(continents.continentsScale, 1, 0, this.experimental.alternateErosionScaling);
            case "island" -> NoiseState.create();
            case "erosion" -> new NoiseState(continents.erosionScale, 1, 0, this.experimental.alternateErosionScaling);
            case "ridge" -> new NoiseState(continents.ridgeScale, 1, 0);
            case "temperature" -> this.biomes.temperature;
            case "vegetation" -> this.biomes.vegetation;
            default -> throw new IllegalArgumentException("Unknown noise state option");
        };
    }

    public boolean test(String key) {
        return switch (key) {
            case "disable_islands" -> !this.islands.enabled && this.continents.oceanOffset < -0.49;
            case "increased_height" -> this.globalTerrain.heightLimits.maxY > 320;
            case "ultrasmooth" -> this.globalTerrain.ultrasmooth;
            case "remove_frozen_ocean_ice" -> this.oceans.removeFrozenOceanIce;
            case "river_lanterns" -> this.continents.riverLanterns;
            case "river_ice" -> this.continents.riverIce;
            case "ore_fix" -> this.caves.oreFix;
            case "no_carvers" -> !this.caves.carversEnabled;
            default -> false;
        };
    }
    
    @Override
    public ConfigState copy() {
        return new ConfigState(
            ConfigState.MINOR_VERSION,
            new General(
                this.general.modEnabled,
                this.general.snowStartOffset
            ),
            new GlobalTerrain(
                this.globalTerrain.verticalScale,
                this.globalTerrain.elevationBoost,
                this.globalTerrain.heightLimits.copy(),
                this.globalTerrain.lavaTunnels,
                this.globalTerrain.ultrasmooth
            ),
            new Continents(
                this.continents.oceanOffset,
                this.continents.continentsScale,
                this.continents.erosionScale,
                this.continents.ridgeScale,
                this.continents.undergroundRivers,
                this.continents.riverLanterns,
                this.continents.riverIce,
                this.continents.flatTerrainSkew,
                this.continents.rollingHills,
                this.continents.junglePillars
            ),
            new Islands(
                this.islands.enabled,
                this.islands.averageDistance
            ),
            new Oceans(
                this.oceans.oceanDepth,
                this.oceans.deepOceanDepth,
                this.oceans.monumentOffset,
                this.oceans.removeFrozenOceanIce
            ),
            new Biomes(
                this.biomes.temperature.copy(),
                this.biomes.vegetation.copy()
            ),
            new Caves(
                this.caves.depthCutoffStart,
                this.caves.depthCutoffSize,
                this.caves.cheeseEnabled,
                this.caves.cheeseAdditive,
                this.caves.noodleEnabled,
                this.caves.noodleAdditive,
                this.caves.spaghettiEnabled,
                this.caves.carversEnabled,
                this.caves.oreFix
            ),
            new Experimental(
                this.experimental.alternateErosionScaling,
                this.experimental.alternateContinentsScaling
            )
        );
    }
    
    
    public static class General {
        public static final boolean MOD_ENABLED = true;
        public static final int SNOW_START_OFFSET = 128;

        public static final General DEFAULT = new General(MOD_ENABLED, SNOW_START_OFFSET);
        public static final Codec<General> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            commented(Codec.BOOL, "mod_enabled", "When disabled, the mod lays dormant and does nothing. Game restart required!").forGetter(general -> general.modEnabled),
            commented(Codec.INT, "snow_start_offset", "Offsets the y-level snow in cold biomes like Taigas starts at.").orElse(SNOW_START_OFFSET).forGetter(general -> general.snowStartOffset)
        ).apply(instance, General::new));

        public boolean modEnabled;
        public int snowStartOffset;

        public General(boolean modEnabled, int snowStartOffset) {
            this.modEnabled = modEnabled;
            this.snowStartOffset = snowStartOffset;
        }
    }

    public static class GlobalTerrain {
        public static final float VERTICAL_SCALE = 1.125f;
        public static final float ELEVATION_BOOST = 0;
        public static final HeightLimits HEIGHT_LIMITS = HeightLimits.DEFAULT;
        public static final boolean LAVA_TUNNELS = true;
        public static final boolean ULTRASMOOTH = false;

        public static final GlobalTerrain DEFAULT = new GlobalTerrain(VERTICAL_SCALE, ELEVATION_BOOST, HEIGHT_LIMITS, LAVA_TUNNELS, ULTRASMOOTH);
        public static final Codec<GlobalTerrain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            commented(Codec.FLOAT, "vertical_scale", "Vertically stretches terrain above sea level. Doubling this value will double the surface's height at a given point from sea level.").orElse(VERTICAL_SCALE).forGetter(globalTerrain -> globalTerrain.verticalScale),
            commented(Codec.FLOAT, "elevation_boost", "Increases the effects of Vertical Scale on mountainous terrain. This will increase the scale of mountains and plateaus faster than lowlands.").orElse(ELEVATION_BOOST).forGetter(globalTerrain -> globalTerrain.elevationBoost),
            HeightLimits.FULL_CODEC.orElse(HEIGHT_LIMITS).forGetter(globalTerrain -> globalTerrain.heightLimits),
            commented(Codec.BOOL, "lava_tunnels", "Rare, deep underground tunnels of lava sometimes generate at the bottom of the world.").orElse(LAVA_TUNNELS).forGetter(globalTerrain -> globalTerrain.lavaTunnels),
            commented(Codec.BOOL, "ultrasmooth", "Greatly smoothens out the terrain like Tectonic v2, removing staircasing artifacts at the cost of strange/broken generation in deep oceans and windswept biomes.").orElse(ULTRASMOOTH).forGetter(globalTerrain -> globalTerrain.ultrasmooth)
        ).apply(instance, GlobalTerrain::new));

        public float verticalScale;
        public float elevationBoost;
        public HeightLimits heightLimits;
        public boolean lavaTunnels;
        public boolean ultrasmooth;

        public GlobalTerrain(float verticalScale, float elevationBoost, HeightLimits heightLimits, boolean lavaTunnels, boolean ultrasmooth) {
            this.verticalScale = verticalScale;
            this.elevationBoost = elevationBoost;
            this.heightLimits = heightLimits;
            this.lavaTunnels = lavaTunnels;
            this.ultrasmooth = ultrasmooth;
        }
    }

    public static class Continents {
        public static final float OCEAN_OFFSET = -0.8f;
        public static final float CONTINENTS_SCALE = 0.13f;
        public static final float EROSION_SCALE = 0.25f;
        public static final float RIDGE_SCALE = 0.25f;
        public static final boolean UNDERGROUND_RIVERS = true;
        public static final boolean RIVER_LANTERNS = true;
        public static final boolean RIVER_ICE = false;
        public static final float FLAT_TERRAIN_SKEW = 0.1f;
        public static final boolean ROLLING_HILLS = true;
        public static final boolean JUNGLE_PILLARS = true;

        public static final Continents DEFAULT = new Continents(OCEAN_OFFSET, CONTINENTS_SCALE, EROSION_SCALE, RIDGE_SCALE, UNDERGROUND_RIVERS, RIVER_LANTERNS, RIVER_ICE, FLAT_TERRAIN_SKEW, ROLLING_HILLS, JUNGLE_PILLARS);
        public static final Codec<Continents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            commented(Codec.FLOAT, "ocean_offset", "Skews terrain towards or away from oceans. Lower values = more oceans. Above -0.45, deep oceans will not spawn! Above -0.2, oceans will not spawn!").orElse(OCEAN_OFFSET).forGetter(continents -> continents.oceanOffset),
            commented(Codec.FLOAT, "continents_scale", "Horizontally scales continents. Lower values = larger continents and oceans between them.").orElse(CONTINENTS_SCALE).forGetter(continents -> continents.continentsScale),
            commented(Codec.FLOAT, "erosion_scale", "Horizontally scales erosion, primarily impacting mountains. Lower values = thicker mountain ranges and terrain between them.").orElse(EROSION_SCALE).forGetter(continents -> continents.erosionScale),
            commented(Codec.FLOAT, "ridge_scale", "Horizontally scales the ridge noise, primarily impacting rivers and terrain around them like plateaus. Lower values = wider rivers and terrain between them.").orElse(RIDGE_SCALE).forGetter(continents -> continents.ridgeScale),
            commented(Codec.BOOL, "underground_rivers", "Rivers that reach mountain ranges continue underground, allowing smoother boat exploration.").orElse(UNDERGROUND_RIVERS).forGetter(continents -> continents.undergroundRivers),
            commented(Codec.BOOL, "river_lanterns", "Underground rivers will rarely generate with hanging lanterns.").orElse(RIVER_LANTERNS).forGetter(continents -> continents.riverLanterns),
            commented(Codec.BOOL, "river_ice", "Underground rivers under snowy biomes will generate with ice.").orElse(RIVER_ICE).forGetter(continents -> continents.riverIce),
            commented(Codec.FLOAT, "flat_terrain_skew", "The threshold where plateau terrain becomes flatter terrain. Positive numbers favor flat terrain and negative numbers favor plateau terrain.").orElse(FLAT_TERRAIN_SKEW).forGetter(continents -> continents.flatTerrainSkew),
            commented(Codec.BOOL, "rolling_hills", "Some Plains biomes generate very smooth, hilly terrain.").orElse(ROLLING_HILLS).forGetter(continents -> continents.rollingHills),
            commented(Codec.BOOL, "jungle_pillars", "Some jungles generate massive pillars that can extend upwards of a hundred blocks tall.").orElse(JUNGLE_PILLARS).forGetter(continents -> continents.junglePillars)
        ).apply(instance, Continents::new));

        public float oceanOffset;
        public float continentsScale;
        public float erosionScale;
        public float ridgeScale;
        public boolean undergroundRivers;
        public boolean riverLanterns;
        public boolean riverIce;
        public float flatTerrainSkew;
        public boolean rollingHills;
        public boolean junglePillars;

        public Continents(float oceanOffset, float continentsScale, float erosionScale, float ridgeScale, boolean undergroundRivers, boolean riverLanterns, boolean riverIce, float flatTerrainSkew, boolean rollingHills, boolean junglePillars) {
            this.oceanOffset = oceanOffset;
            this.continentsScale = continentsScale;
            this.erosionScale = erosionScale;
            this.ridgeScale = ridgeScale;
            this.undergroundRivers = undergroundRivers;
            this.riverLanterns = riverLanterns;
            this.riverIce = riverIce;
            this.flatTerrainSkew = flatTerrainSkew;
            this.rollingHills = rollingHills;
            this.junglePillars = junglePillars;
        }
    }

    public static class Islands {
        public static final boolean ENABLED = true;
        public static final int AVERAGE_DISTANCE = 1536;

        public static final Islands DEFAULT = new Islands(ENABLED, AVERAGE_DISTANCE);
        public static final Codec<Islands> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").orElse(ENABLED).forGetter(islands -> islands.enabled),
            commented(Codec.intRange(640, Integer.MAX_VALUE), "average_distance", "The average distance between two islands, measured in blocks. The minimum distance is always 640 blocks.").orElse(AVERAGE_DISTANCE).forGetter(islands -> islands.averageDistance)
        ).apply(instance, Islands::new));

        public boolean enabled;
        public int averageDistance;

        public Islands(boolean enabled, int averageDistance) {
            this.enabled = enabled;
            this.averageDistance = averageDistance;
        }
    }

    public static class Oceans {
        public static final float OCEAN_DEPTH = -0.22f;
        public static final float DEEP_OCEAN_DEPTH = -0.45f;
        public static final int MONUMENT_OFFSET = -30;
        public static final boolean REMOVE_FROZEN_OCEAN_ICE = false;

        public static final Oceans DEFAULT = new Oceans(OCEAN_DEPTH, DEEP_OCEAN_DEPTH, MONUMENT_OFFSET, REMOVE_FROZEN_OCEAN_ICE);
        public static final Codec<Oceans> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            commented(Codec.FLOAT, "ocean_depth", "0 is at sea level, -0.5 is 64 blocks below sea level.").orElse(OCEAN_DEPTH).forGetter(oceans -> oceans.oceanDepth),
            commented(Codec.FLOAT, "deep_ocean_depth", "0 is at sea level, -0.5 is 64 blocks below sea level.").orElse(DEEP_OCEAN_DEPTH).forGetter(oceans -> oceans.deepOceanDepth),
            commented(Codec.INT, "monument_offset", "Offsets (vanilla) ocean monuments to adjust for deeper oceans.").orElse(MONUMENT_OFFSET).forGetter(oceans -> oceans.monumentOffset),
            commented(Codec.BOOL, "remove_frozen_ocean_ice", "Removes the surface ice from Frozen Oceans, making them as navigable as Deep Frozen Oceans.").orElse(REMOVE_FROZEN_OCEAN_ICE).forGetter(oceans -> oceans.removeFrozenOceanIce)
        ).apply(instance, Oceans::new));

        public float oceanDepth;
        public float deepOceanDepth;
        public int monumentOffset;
        public boolean removeFrozenOceanIce;

        public Oceans(float oceanDepth, float deepOceanDepth, int monumentOffset, boolean removeFrozenOceanIce) {
            this.oceanDepth = oceanDepth;
            this.deepOceanDepth = deepOceanDepth;
            this.monumentOffset = monumentOffset;
            this.removeFrozenOceanIce = removeFrozenOceanIce;
        }
    }

    public static class Biomes {
        public static final Biomes DEFAULT = new Biomes(NoiseState.create(), NoiseState.create());
        public static final Codec<Biomes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NoiseState.legacyCodec("temperature").orElse(NoiseState.create()).forGetter(biomes -> biomes.temperature),
            NoiseState.legacyCodec("vegetation").orElse(NoiseState.create()).forGetter(biomes -> biomes.vegetation)
        ).apply(instance, Biomes::new));

        public NoiseState temperature;
        public NoiseState vegetation;

        public Biomes(NoiseState temperature, NoiseState vegetation) {
            this.temperature = temperature.copy();
            this.vegetation = vegetation.copy();
        }
    }

    public static class Caves {
        public static final float DEPTH_CUTOFF_START = 0.1f;
        public static final float DEPTH_CUTOFF_SIZE = 0.1f;
        public static final boolean CHEESE_ENABLED = true;
        public static final float CHEESE_ADDITIVE = 0.27f;
        public static final boolean NOODLE_ENABLED = true;
        public static final float NOODLE_ADDITIVE = -0.075f;
        public static final boolean SPAGHETTI_ENABLED = true;
        public static final boolean CARVERS_ENABLED = true;
        public static final boolean ORE_FIX = false;

        public static final Caves DEFAULT = new Caves(DEPTH_CUTOFF_START, DEPTH_CUTOFF_SIZE, CHEESE_ENABLED, CHEESE_ADDITIVE, NOODLE_ENABLED, NOODLE_ADDITIVE, SPAGHETTI_ENABLED, CARVERS_ENABLED, ORE_FIX);
        public static final Codec<Caves> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            commented(Codec.FLOAT, "depth_cutoff_start", "The depth noise value that caves stop generating. 0.1 cuts off caves ~12 blocks below the surface, 0.5 cuts off caves ~64 blocks below the surface.").orElse(DEPTH_CUTOFF_START).forGetter(caves -> caves.depthCutoffStart),
            commented(Codec.FLOAT, "depth_cutoff_size", "The distance in depth between the start and end of the depth cutoff. 0.1 means caves close over ~12 blocks, 0.0 means caves cut off at the `depth_cutoff_start` value immediately.").orElse(DEPTH_CUTOFF_SIZE).forGetter(caves -> caves.depthCutoffSize),
            commented(Codec.BOOL, "cheese_enabled", "The standard expansive, large cheese-shaped caves.").orElse(CHEESE_ENABLED).forGetter(caves -> caves.cheeseEnabled),
            commented(Codec.FLOAT, "cheese_additive", "Density added to cheese caves. Lower values = bigger cheese caves.").orElse(CHEESE_ADDITIVE).forGetter(caves -> caves.cheeseAdditive),
            commented(Codec.BOOL, "noodle_enabled", "The narrow, claustrophobic noodle-shaped caves.").orElse(NOODLE_ENABLED).forGetter(caves -> caves.noodleEnabled),
            commented(Codec.FLOAT, "noodle_additive", "Density added to noodle caves. Lower values = bigger noodle caves.").orElse(NOODLE_ADDITIVE).forGetter(caves -> caves.noodleAdditive),
            commented(Codec.BOOL, "spaghetti_enabled", "The tunneling, medium sized spaghetti-shaped caves.").orElse(SPAGHETTI_ENABLED).forGetter(caves -> caves.spaghettiEnabled),
            commented(Codec.BOOL, "carvers_enabled", "Whether old caves/ravines should generate. Game restart required!").orElse(CARVERS_ENABLED).forGetter(caves -> caves.carversEnabled),
            commented(Codec.BOOL, "ore_fix", "Alters ore generation to generate at more acceptable levels when min_y is lowered.").orElse(ORE_FIX).forGetter(caves -> caves.oreFix)
        ).apply(instance, Caves::new));

        public float depthCutoffStart;
        public float depthCutoffSize;
        public boolean cheeseEnabled;
        public float cheeseAdditive;
        public boolean noodleEnabled;
        public float noodleAdditive;
        public boolean spaghettiEnabled;
        public boolean carversEnabled;
        public boolean oreFix;

        public Caves(float depthCutoffStart, float depthCutoffSize, boolean cheeseEnabled, float cheeseAdditive, boolean noodleEnabled, float noodleAdditive, boolean spaghettiEnabled, boolean carversEnabled, boolean oreFix) {
            this.depthCutoffStart = depthCutoffStart;
            this.depthCutoffSize = depthCutoffSize;
            this.cheeseEnabled = cheeseEnabled;
            this.cheeseAdditive = cheeseAdditive;
            this.noodleEnabled = noodleEnabled;
            this.noodleAdditive = noodleAdditive;
            this.spaghettiEnabled = spaghettiEnabled;
            this.carversEnabled = carversEnabled;
            this.oreFix = oreFix;
        }
    }
    
    public static class Experimental {
        public static final boolean ALTERNATE_EROSION_SCALING = false;
        public static final boolean ALTERNATE_CONTINENTS_SCALING = false;
        
        public static final Experimental DEFAULT = new Experimental(ALTERNATE_EROSION_SCALING, ALTERNATE_CONTINENTS_SCALING);
        public static final Codec<Experimental> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("alternate_erosion_scaling").orElse(ALTERNATE_EROSION_SCALING).forGetter(e -> e.alternateErosionScaling),
            Codec.BOOL.fieldOf("alternate_continents_scaling").orElse(ALTERNATE_CONTINENTS_SCALING).forGetter(e -> e.alternateContinentsScaling)
        ).apply(i, Experimental::new));
        
        public boolean alternateErosionScaling;
        public boolean alternateContinentsScaling;
        
        public Experimental(boolean alternateErosionScaling, boolean alternateContinentsScaling) {
            this.alternateErosionScaling = alternateErosionScaling;
            this.alternateContinentsScaling = alternateContinentsScaling;
        }
    }
}
