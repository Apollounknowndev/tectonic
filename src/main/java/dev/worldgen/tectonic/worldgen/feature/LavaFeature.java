package dev.worldgen.tectonic.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class LavaFeature extends Feature<NoneFeatureConfiguration> {
	public static final LavaFeature INSTANCE = new LavaFeature();
	
	public LavaFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}
	
	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		for (int x = 0; x < 4; x++) {
			for (int z = 0; z < 4; z++) {
				BlockPos pos = context.origin().offset(x, 0, z);
				for (int y = pos.getY(); y >= context.level().getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()); y--) {
					context.level().setBlock(pos.atY(y), Blocks.LAVA.defaultBlockState(), 2);
					context.level().scheduleTick(pos.atY(y), Fluids.LAVA, 0);
				}
			}
		}
		return true;
	}
}
