package dev.worldgen.tectonic.mixin;

import dev.worldgen.tectonic.Tectonic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureStart.class)
public class StructureStartMixin {

    @Inject(method = "loadStaticStart", at = @At("HEAD"))
    private static void tectonic$loadStaticStartHead(StructurePieceSerializationContext context, CompoundTag tag, long seed, CallbackInfoReturnable<StructureStart> cir) {
        Tectonic.setMonumentLoadOffset(null);
        if (Tectonic.isMonumentTag(tag)) {
            Tectonic.setMonumentLoadOffset(Tectonic.computeMonumentOffset(tag));
        }
    }

    @Inject(method = "loadStaticStart", at = @At("RETURN"))
    private static void tectonic$loadStaticStartReturn(StructurePieceSerializationContext context, CompoundTag tag, long seed, CallbackInfoReturnable<StructureStart> cir) {
        Tectonic.setMonumentLoadOffset(null);
    }
}
