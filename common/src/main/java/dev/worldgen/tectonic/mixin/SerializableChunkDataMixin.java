package dev.worldgen.tectonic.mixin;

import dev.worldgen.tectonic.Tectonic;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(SerializableChunkData.class)
public abstract class SerializableChunkDataMixin {
	@Unique
	// Taken from the BlendingDataFix, don't blend this or the terrain will drop all the way to bedrock level
	private static final Set<String> STATUSES_TO_SKIP_BLENDING = Set.of(
			"minecraft:empty", "minecraft:structure_starts", "minecraft:structure_references", "minecraft:biomes"
	);
	@Inject(method = "parse", at = @At("HEAD"))
	private static void tectonic$parse(LevelHeightAccessor levelHeightAccessor, RegistryAccess registryAccess, CompoundTag compoundTag, CallbackInfoReturnable<SerializableChunkData> cir) {
		if (STATUSES_TO_SKIP_BLENDING.contains(ChunkStatus.byName(compoundTag.getString("Status")).toString())) return;
		if (compoundTag.getInt("tectonic_blending_version") != Tectonic.blendingVersion()) {
			CompoundTag data = getOrMakeBlendingTag(compoundTag);
			data.remove("Heightmaps");
			data.remove("isLightOn");
		}
	}

	@Unique
	private static CompoundTag getOrMakeBlendingTag(CompoundTag compoundTag) {
		int min = 0, max = 0;
		ListTag sections = compoundTag.getList("sections", ListTag.TAG_COMPOUND);
		for (Tag section : sections) {
			int y = section instanceof IntTag tag ? tag.getAsInt() : 0;
			min = Math.min(y, min);
			max = Math.max(y, max);
		}
		min = Math.min(min, -4);
		max = Math.max(max, 20);
		CompoundTag blendingData = new CompoundTag();
		blendingData.putInt("min_section", min);
		blendingData.putInt("max_section", max);
		compoundTag.put("blending_data", blendingData);
		return compoundTag;
	}

	@Inject(method = "write", at = @At("RETURN"), cancellable = true)
	private void tectonic$write(CallbackInfoReturnable<CompoundTag> cir) {
		int blendingVersion = Tectonic.blendingVersion();
		if (blendingVersion == 0) return; // don't save it when it's 0 since that's basically vanilla world generation
		CompoundTag data = cir.getReturnValue();
		data.putInt("tectonic_blending_version", blendingVersion);
		cir.setReturnValue(data);
	}
}
