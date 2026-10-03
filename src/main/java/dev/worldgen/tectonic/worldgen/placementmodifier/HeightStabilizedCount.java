package dev.worldgen.tectonic.worldgen.placementmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record HeightStabilizedCount(VerticalAnchor maxY, float countPerSection, boolean biasedToBottom) implements PlacementModifier {
	public static final MapCodec<HeightStabilizedCount> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		VerticalAnchor.CODEC.fieldOf("max_y").forGetter(HeightStabilizedCount::maxY),
		ExtraCodecs.POSITIVE_FLOAT.fieldOf("count_per_section").forGetter(HeightStabilizedCount::countPerSection),
		Codec.BOOL.fieldOf("biased_to_bottom").forGetter(HeightStabilizedCount::biasedToBottom)
	).apply(i, HeightStabilizedCount::new));
	
	@Override
	public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
		int maxY = this.maxY.resolveY(context);
		int minY = context.getMinGenY();
		int spanY = maxY - minY;
		int count = (int) Math.ceil((spanY / 16f) * this.countPerSection);
		List<BlockPos> positions = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int y = this.biasedToBottom ? minY + random.nextInt(random.nextIntBetweenInclusive(spanY / 2, spanY)) : random.nextIntBetweenInclusive(minY, maxY);
			positions.add(origin.atY(y));
		}
		
		positions.forEach(output);
	}
	
	@Override
	public MapCodec<? extends PlacementModifier> codec() {
		return CODEC;
	}
}
