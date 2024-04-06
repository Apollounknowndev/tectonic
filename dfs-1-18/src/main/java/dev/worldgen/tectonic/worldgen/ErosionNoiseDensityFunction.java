package dev.worldgen.tectonic.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public record ErosionNoiseDensityFunction(DensityFunction delegatedDensityFunction, @Nullable Holder<NormalNoise.NoiseParameters> noiseData, @Nullable DensityFunction shiftX, @Nullable DensityFunction shiftZ) implements DensityFunction {
    public static MapCodec<ErosionNoiseDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(ErosionNoiseDensityFunction::noiseData),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(ErosionNoiseDensityFunction::shiftX),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(ErosionNoiseDensityFunction::shiftZ)
    ).apply(instance, ErosionNoiseDensityFunction::createUnseeded));

    public static Codec<ErosionNoiseDensityFunction> CODEC = DATA_CODEC.codec();

    public static ErosionNoiseDensityFunction createUnseeded(Holder<NormalNoise.NoiseParameters> parameters, DensityFunction shiftX, DensityFunction shiftZ) {
        return new ErosionNoiseDensityFunction(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, ConfigHandler.getConfig().getValue("horizontal_mountain_scale"), parameters), null, null, null);
    }

    @Override
    public double compute(@NotNull FunctionContext context) {
        return this.delegatedDensityFunction().compute(context);
    }

    @Override
    public void fillArray(double @NotNull [] doubles, @NotNull ContextProvider contextProvider) {
        this.delegatedDensityFunction().fillArray(doubles, contextProvider);
    }

    @Override
    @NotNull
    public DensityFunction mapAll(Visitor visitor) {
        return this.delegatedDensityFunction().mapAll(visitor);
    }

    @Override
    public double minValue() {
        return this.delegatedDensityFunction().minValue();
    }

    @Override
    public double maxValue() {
        return this.delegatedDensityFunction().maxValue();
    }

    @Override
    @NotNull
    public Codec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
