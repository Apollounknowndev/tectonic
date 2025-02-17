package dev.worldgen.tectonic.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;

public record ConfigNoise(NoiseHolder noise, DensityFunction shiftX, DensityFunction shiftZ, double scale) implements DensityFunction {
    public static MapCodec<ConfigNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("option").forGetter(df -> ""),
        NoiseHolder.CODEC.fieldOf("noise").forGetter(ConfigNoise::noise),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(ConfigNoise::shiftX),
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(ConfigNoise::shiftZ)
    ).apply(instance, ConfigNoise::new));

    public static KeyDispatchDataCodec<ConfigNoise> CODEC_HOLDER = KeyDispatchDataCodec.of(DATA_CODEC);

    public ConfigNoise(String option, NoiseHolder noise, DensityFunction shiftX, DensityFunction shiftZ) {
        this(noise, shiftX, shiftZ, ConfigHandler.getConfig().getValue(option));
    }

    @Override
    public double compute(@NotNull FunctionContext context) {
        return noise.getValue(context.blockX() * scale + shiftX.compute(context), 0, context.blockZ() * scale + shiftZ.compute(context));
    }

    @Override
    public void fillArray(double @NotNull [] doubles, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(doubles, this);
    }

    @Override
    @NotNull
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ConfigNoise(visitor.visitNoise(noise), shiftX, shiftZ, scale));
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return noise.maxValue();
    }

    @Override
    @NotNull
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
