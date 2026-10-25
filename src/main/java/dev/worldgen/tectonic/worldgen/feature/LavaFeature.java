package dev.worldgen.tectonic.worldgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;

public class LavaFeature implements Feature {
	public static final MapCodec<LavaFeature> CODEC = MapCodec.unit(LavaFeature::new);
	
	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		for (int x = 0; x < 4; x++) {
			for (int z = 0; z < 4; z++) {
				BlockPos pos = origin.offset(x, 0, z);
				for (int y = pos.getY(); y >= level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()); y--) {
					level.setBlock(pos.atY(y), Blocks.LAVA.defaultBlockState(), 2);
					level.scheduleTick(pos.atY(y), Fluids.LAVA, 0);
				}
			}
		}
		return true;
	}
	
	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}
}
