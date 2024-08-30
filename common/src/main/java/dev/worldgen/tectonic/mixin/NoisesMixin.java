package dev.worldgen.tectonic.mixin;

import net.minecraft.resources.ResourceLocation;
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
            target = "Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;fromHashOf(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/util/RandomSource;"
        )
    )
    private static ResourceLocation tectonic$fixTectonicNoiseSeeds(ResourceLocation name) {
        if (name.getNamespace().equals("tectonic")) {
            String path = name.getPath();
            if (path.startsWith("parameter/")) {
                return new ResourceLocation(path.substring(10));
            }
        }
        return name;
    }
}
