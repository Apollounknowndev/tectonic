package dev.worldgen.tectonic.worldgen.placementmodifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TileAcrossChunk extends PlacementModifier {
	public static final MapCodec<TileAcrossChunk> CODEC = MapCodec.unit(TileAcrossChunk::new);
	public static final PlacementModifierType<TileAcrossChunk> TYPE = () -> CODEC;
	
	@Override
	public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
		List<BlockPos> positions = new ArrayList<>();
		for (int x = 0; x < 4; x++) {
			for (int z = 0; z < 4; z++) {
				positions.add(pos.offset(x * 4, 0, z * 4));
			}
		}
		return positions.stream();
	}
	
	@Override
	public PlacementModifierType<?> type() {
		return TYPE;
	}
}
