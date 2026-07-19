package dev.worldgen.tectonic.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.Tectonic;
import dev.worldgen.tectonic.config.object.NoiseState;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record ConfigNoise(NoiseHolder noise, DensityFunction shiftX, DensityFunction shiftZ, double scale, double multiplier, double offset, boolean smootherScaling) implements DensityFunction {
    public static MapCodec<ConfigNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(df -> ""),
        NoiseHolder.CODEC.fieldOf("noise").forGetter(ConfigNoise::noise),
        DensityFunction.CODEC.fieldOf("shift_x").forGetter(ConfigNoise::shiftX),
        DensityFunction.CODEC.fieldOf("shift_z").forGetter(ConfigNoise::shiftZ)
    ).apply(instance, ConfigNoise::create));

    public static KeyDispatchDataCodec<ConfigNoise> CODEC_HOLDER = KeyDispatchDataCodec.of(DATA_CODEC);

    public static ConfigNoise create(String key, NoiseHolder noise, DensityFunction shiftX, DensityFunction shiftZ) {
        NoiseState state = Tectonic.CONFIG.getState().getNoiseState(key);
        return new ConfigNoise(noise, shiftX, shiftZ, state.scale, state.multiplier, state.offset, state.smootherScaling);
    }

    @Override
    public double compute(FunctionContext context) {
        double x;
        double z;
        if (smootherScaling) {
            x = (context.blockX() + shiftX.compute(context)) * scale;
            z = (context.blockZ() + shiftZ.compute(context)) * scale;
        } else {
            x = context.blockX() * scale + shiftX.compute(context);
            z = context.blockZ() * scale + shiftZ.compute(context);
        }
        return noise.getValue(x, 0, z) * multiplier + offset;
    }

    @Override
    public void fillArray(double[] doubles, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(doubles, this);
    }
    
    //? if >=26.2 {
    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        if (this.smootherScaling) {
            return new ConfigNoise(visitor.visitNoise(noise), visitor.apply(shiftX), visitor.apply(shiftZ), scale, multiplier, offset, true);
        }
        return DensityFunctions.add(
            DensityFunctions.mul(
                DensityFunctions.shiftedNoise2d(this.shiftX, this.shiftZ, this.scale, this.noise.noiseData()),
                DensityFunctions.constant(this.multiplier)
            ),
            DensityFunctions.constant(this.offset)
        ).mapChildren(visitor);
    }
    //? } else {
    
    /*@Override
    public DensityFunction mapAll(Visitor visitor) {
        if (this.smootherScaling) {
            return new ConfigNoise(visitor.visitNoise(noise), shiftX.mapAll(visitor), shiftZ.mapAll(visitor), scale, multiplier, offset, smootherScaling);
        }
        return DensityFunctions.add(
            DensityFunctions.mul(
                DensityFunctions.shiftedNoise2d(this.shiftX, this.shiftZ, this.scale, this.noise.noiseData()),
                DensityFunctions.constant(this.multiplier)
            ),
            DensityFunctions.constant(this.offset)
        ).mapAll(visitor);
    }
    *///? }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return noise.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
