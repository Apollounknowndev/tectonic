package dev.worldgen.tectonic.mixin;

import dev.worldgen.tectonic.Tectonic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
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
    private static void tectonic$parse(LevelHeightAccessor heightAccessor, PalettedContainerFactory factory, CompoundTag nbt, CallbackInfoReturnable<SerializableChunkData> cir) {
        // Safe cast unless some mod does weird bs
        if (!((ServerLevel)heightAccessor).dimension().equals(Level.OVERWORLD)) return;

        if (STATUSES_TO_SKIP_BLENDING.contains(ChunkStatus.byName(nbt.getStringOr("Status", "unknown")).toString())) return;
        if (nbt.getIntOr(Tectonic.BLENDING_KEY, 0) != Tectonic.BLENDING_VERSION) {
            int min = 0, max = 0;
            ListTag sections = nbt.getListOrEmpty("sections");
            for (Tag section : sections) {
                int y = section instanceof IntTag tag ? tag.intValue() : 0;
                min = Math.min(y, min);
                max = Math.max(y, max);
            }
            min = Math.min(min, -4);
            max = Math.max(max, 20);
            CompoundTag blendingData = new CompoundTag();
            blendingData.putInt("min_section", min);
            blendingData.putInt("max_section", max);
            nbt.put("blending_data", blendingData);
            nbt.remove("Heightmaps");
            nbt.remove("isLightOn");
        }
    }

    @Inject(method = "write", at = @At("RETURN"), cancellable = true)
    private void tectonic$write(CallbackInfoReturnable<CompoundTag> cir) {
        if (Tectonic.BLENDING_VERSION == 0) return;
        CompoundTag data = cir.getReturnValue();
        data.putInt(Tectonic.BLENDING_KEY, Tectonic.BLENDING_VERSION);
        cir.setReturnValue(data);
    }
}