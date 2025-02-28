package dev.worldgen.tectonic.mixin;

import dev.worldgen.tectonic.Tectonic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IOWorker.class)
public class IOWorkerMixin {
	@Inject(method = "isOldChunk", at = @At("HEAD"), cancellable = true)
	void tectonic$isOldChunk(CompoundTag compoundTag, CallbackInfoReturnable<Boolean> cir) {
		int blendingVersion = compoundTag.getInt("tectonic_blending_version");
		if (blendingVersion != Tectonic.blendingVersion()) cir.setReturnValue(true);
	}
}
