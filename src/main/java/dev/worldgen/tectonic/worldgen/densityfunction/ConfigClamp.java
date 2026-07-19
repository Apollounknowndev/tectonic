package dev.worldgen.tectonic.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ConfigClamp(DensityFunction input, DensityFunction min, DensityFunction max) implements DensityFunction {
    public static final MapCodec<ConfigClamp> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DensityFunction.CODEC.fieldOf("input").forGetter(ConfigClamp::input),
        DensityFunction.CODEC.fieldOf("min").forGetter(ConfigClamp::min),
        DensityFunction.CODEC.fieldOf("max").forGetter(ConfigClamp::max)
    ).apply(instance, ConfigClamp::new));
    public static KeyDispatchDataCodec<ConfigClamp> CODEC_HOLDER = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        return Math.min(Math.max(this.input.compute(context), this.min.compute(context)), this.max.compute(context));
    }

    @Override
    public void fillArray(double[] densities, ContextProvider context) {
        context.fillAllDirectly(densities, this);
    }
    
    //? if >=26.2 {
    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        return visitor.apply(input.clamp(min.maxValue(), max.minValue()));
    }
    //? } else {
    
    /*@Override
    public DensityFunction mapAll(Visitor visitor) {
        return input.clamp(min.maxValue(), max.minValue()).mapAll(visitor);
    }
    *///? }

    @Override
    public double minValue() {
        return this.min.minValue();
    }

    @Override
    public double maxValue() {
        return this.max.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
