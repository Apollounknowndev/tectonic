package dev.worldgen.tectonic.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Noises;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Noises.class)
public abstract class NoisesMixin {

    @ModifyArg(
        method = "instantiate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;fromHashOf(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/util/RandomSource;"
        )
    )
    private static Identifier tectonic$fixTectonicNoiseSeeds(Identifier name) {
        if (name.getNamespace().equals("tectonic")) {
            String path = name.getPath();
            if (path.startsWith("parameter/")) {
                return Identifier.withDefaultNamespace(path.substring(10));
            }
        }
        return name;
    }
}
