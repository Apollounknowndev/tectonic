package dev.worldgen.tectonic.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.object.NoiseState;

import static dev.worldgen.apollib.codec.ApollibCodecs.commented;
import static dev.worldgen.apollib.codec.ApollibCodecs.optionalCommented;

public class NewConfigState {
	public static final int VERSION = 4;
	
	public int version;
	
	public static class TerrainScales {
		public static final double VERTICAL_SCALE = 1;
		public static final double OCEAN_DEPTH = 2;
		public static final double DEEP_OCEAN_DEPTH = 2;
	}
	
	public static class Features {
		public static final boolean UNDERGROUND_RIVERS = true;
		public static final boolean RIVER_ICE = true;
		public static final boolean LAVA_TUNNELS = true;
		public static final boolean JUNGLE_PILLARS = true;
		
		public static final Codec<Features> CODEC = RecordCodecBuilder.create(i -> i.group(
			optionalCommented(Codec.BOOL, true, "underground_rivers", "Rivers that reach mountain ranges continue underground, allowing smoother boat exploration.").forGetter(t -> t.undergroundRivers),
			optionalCommented(Codec.BOOL, true, "river_ice", "Underground rivers under snowy biomes will generate with ice.").forGetter(t -> t.riverIce),
			optionalCommented(Codec.BOOL, true, "lava_tunnels", "Rare, deep underground tunnels of lava sometimes generate at the bottom of the world.").forGetter(t -> t.lavaTunnels),
			optionalCommented(Codec.BOOL, true, "jungle_pillars", "Some jungles generate massive pillars that can extend upwards of a hundred blocks tall.").forGetter(t -> t.junglePillars)
		).apply(i, Features::new));
		
		public boolean undergroundRivers;
		public boolean riverIce;
		public boolean lavaTunnels;
		public boolean junglePillars;
		
		public Features(boolean undergroundRivers, boolean riverIce, boolean lavaTunnels, boolean junglePillars) {
			this.undergroundRivers = undergroundRivers;
			this.riverIce = riverIce;
			this.lavaTunnels = lavaTunnels;
			this.junglePillars = junglePillars;
		}
	}
	
	public static class Noises {
		public static final NoiseState TEMPERATURE = NoiseState.create();
		public static final NoiseState VEGETATION = NoiseState.create();
		public static final NoiseState CONTINENTS = NoiseState.create(0.13);
		public static final NoiseState EROSION = NoiseState.create();
		public static final NoiseState RIDGES = NoiseState.create();
		public static final NoiseState ISLANDS = NoiseState.create(0.11);
		
		public static final Codec<Noises> CODEC = RecordCodecBuilder.create(i -> i.group(
			NoiseState.codec("temperature", TEMPERATURE).forGetter(n -> n.temperature),
			NoiseState.codec("vegetation", VEGETATION).forGetter(n -> n.vegetation),
			NoiseState.codec("continents", CONTINENTS).forGetter(n -> n.continents),
			NoiseState.codec("erosion", EROSION).forGetter(n -> n.erosion),
			NoiseState.codec("ridges", RIDGES).forGetter(n -> n.ridges),
			NoiseState.codec("islands", ISLANDS).forGetter(n -> n.islands)
		).apply(i, Noises::new));
		
		public NoiseState temperature;
		public NoiseState vegetation;
		public NoiseState continents;
		public NoiseState erosion;
		public NoiseState ridges;
		public NoiseState islands;
		
		public Noises(NoiseState temperature, NoiseState vegetation, NoiseState continents, NoiseState erosion, NoiseState ridges, NoiseState islands) {
			this.temperature = temperature.copy();
			this.vegetation = vegetation.copy();
			this.continents = continents.copy();
			this.erosion = erosion.copy();
			this.ridges = ridges.copy();
			this.islands = islands.copy();
		}
	}
}
