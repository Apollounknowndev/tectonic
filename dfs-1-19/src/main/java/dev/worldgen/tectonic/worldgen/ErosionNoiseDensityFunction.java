package dev.worldgen.tectonic.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.jetbrains.annotations.NotNull;

public record ErosionNoiseDensityFunction(DensityFunction.NoiseHolder noise, DensityFunction shiftX, DensityFunction shiftZ) implements DensityFunction {
    public static Codec<ErosionNoiseDensityFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        NoiseHolder.CODEC.fieldOf("noise").forGetter(ErosionNoiseDensityFunction::noise),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(ErosionNoiseDensityFunction::shiftX),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(ErosionNoiseDensityFunction::shiftZ)
    ).apply(instance, ErosionNoiseDensityFunction::new));

    public static KeyDispatchDataCodec<ErosionNoiseDensityFunction> CODEC_HOLDER = KeyDispatchDataCodec.of(CODEC);

    @Override
    public double compute(@NotNull FunctionContext context) {
        double xzScale = ConfigHandler.getConfig().getValue("horizontal_mountain_scale");
        return this.noise.getValue(context.blockX() * xzScale + this.shiftX().compute(context), 0, context.blockZ() * xzScale + this.shiftZ().compute(context));
    }

    @Override
    public void fillArray(double @NotNull [] doubles, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(doubles, this);
    }

    @Override
    @NotNull
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ErosionNoiseDensityFunction(visitor.visitNoise(this.noise), this.shiftX, this.shiftZ));
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return this.noise.maxValue();
    }

    @Override
    @NotNull
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
