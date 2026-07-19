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
    @Inject(
        method = "isOldChunk",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tectonic$needsBlending(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir) {
        //? if >=26.1 {
        int version = nbt.getIntOr(Tectonic.BLENDING_KEY, 0);
        //? } else {
        /*int version = nbt.getInt(Tectonic.BLENDING_KEY);
         *///? }
        
        if (version != Tectonic.BLENDING_VERSION) {
            cir.setReturnValue(true);
        }
    }
}
