package dev.worldgen.tectonic.worldgen.placementmodifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.function.Consumer;

public record TileAcrossChunk() implements PlacementModifier {
	public static final MapCodec<TileAcrossChunk> CODEC = MapCodec.unit(TileAcrossChunk::new);
	
	@Override
	public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
		for (int x = 0; x < 4; x++) {
			for (int z = 0; z < 4; z++) {
				output.accept(origin.offset(x * 4, 0, z * 4));
			}
		}
	}
	
	@Override
	public MapCodec<? extends PlacementModifier> codec() {
		return CODEC;
	}
}
