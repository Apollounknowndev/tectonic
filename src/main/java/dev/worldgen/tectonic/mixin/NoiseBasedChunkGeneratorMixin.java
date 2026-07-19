package dev.worldgen.tectonic.mixin;

import com.google.common.base.Suppliers;
import dev.worldgen.tectonic.Tectonic;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Shadow @Final @Mutable
    private Supplier<Aquifer.FluidPicker> globalFluidPicker;

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void tectonic$fixLavaLevel(BiomeSource source, Holder<NoiseGeneratorSettings> settings, CallbackInfo ci) {
        if (Tectonic.CONFIG.getState().enabled() && settings.unwrapKey().map(key -> key.identifier().getPath().equals("overworld")).orElse(false)) {
            this.globalFluidPicker = Suppliers.memoize(() -> {
                int lavaLevel = Tectonic.CONFIG.getState().globalTerrain.heightLimits.minY + 10;
                Aquifer.FluidStatus lavaStatus = new Aquifer.FluidStatus(lavaLevel, Blocks.LAVA.defaultBlockState());
                int seaLevel = settings.value().seaLevel();
                Aquifer.FluidStatus seaStatus = new Aquifer.FluidStatus(seaLevel, settings.value().defaultFluid());
                Aquifer.FluidStatus disabledStatus = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
                return (x, y, z) -> {
                    if (SharedConstants.DEBUG_DISABLE_FLUID_GENERATION) {
                        return disabledStatus;
                    }
                    if (y < Math.min(lavaLevel, seaLevel)) {
                        return lavaStatus;
                    }
                    return seaStatus;
                };
            });
        }
    }
}
