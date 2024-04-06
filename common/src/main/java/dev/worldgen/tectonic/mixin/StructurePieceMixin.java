package dev.worldgen.tectonic.mixin;

import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StructurePiece.class)
public class StructurePieceMixin {
    
    @ModifyVariable(
        method = "<init>(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceType;ILnet/minecraft/world/level/levelgen/structure/BoundingBox;)V",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private static BoundingBox tectonic$lowerOceanMonuments(BoundingBox boundingBox, StructurePieceType type) {
        if (type == StructurePieceType.OCEAN_MONUMENT_BUILDING && ConfigHandler.getConfig().modEnabled() && ConfigHandler.getConfig().featuresModule().deeperOceans()) {
            return boundingBox.moved(0, -30, 0);
        }
        return boundingBox;
    }
}