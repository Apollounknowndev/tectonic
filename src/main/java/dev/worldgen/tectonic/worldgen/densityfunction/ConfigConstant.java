package dev.worldgen.tectonic.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.Tectonic;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.Arrays;

public record ConfigConstant(double value) implements DensityFunction {
    public static MapCodec<ConfigConstant> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(df -> "")
    ).apply(instance, ConfigConstant::create));

    public static KeyDispatchDataCodec<ConfigConstant> CODEC_HOLDER = KeyDispatchDataCodec.of(DATA_CODEC);

    public static ConfigConstant create(String key) {
        return new ConfigConstant(Tectonic.CONFIG.getState().getValue(key));
    }

    @Override
    public double compute(FunctionContext context) {
        return value;
    }

    @Override
    public void fillArray(double[] doubles, ContextProvider contextProvider) {
        Arrays.fill(doubles, value);
    }
    
    //? if >=26.2 {
    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        return DensityFunctions.constant(value);
    }
    //? } else {
    
    /*@Override
    public DensityFunction mapAll(Visitor visitor) {
        return DensityFunctions.constant(value);
    }
    *///? }
    
    @Override
    public double minValue() {
        return value;
    }

    @Override
    public double maxValue() {
        return value;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
