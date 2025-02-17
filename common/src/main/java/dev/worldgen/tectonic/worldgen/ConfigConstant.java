package dev.worldgen.tectonic.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record ConfigConstant(double value) implements DensityFunction {
    public static MapCodec<ConfigConstant> DATA_CODEC = Codec.STRING.fieldOf("option").xmap(ConfigConstant::new, df -> "");

    public static KeyDispatchDataCodec<ConfigConstant> CODEC_HOLDER = KeyDispatchDataCodec.of(DATA_CODEC);

    public ConfigConstant(String option) {
        this(ConfigHandler.getConfig().getValue(option));
    }

    @Override
    public double compute(@NotNull FunctionContext context) {
        return value;
    }

    @Override
    public void fillArray(double @NotNull [] doubles, ContextProvider contextProvider) {
        Arrays.fill(doubles, value);
    }

    @Override
    @NotNull
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public double minValue() {
        return -10000000;
    }

    @Override
    public double maxValue() {
        return 10000000;
    }

    @Override
    @NotNull
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
