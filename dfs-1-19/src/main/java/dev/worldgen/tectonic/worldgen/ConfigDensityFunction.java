package dev.worldgen.tectonic.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.jetbrains.annotations.NotNull;

public record ConfigDensityFunction(String option, Holder<DensityFunction> trueArgument, Holder<DensityFunction> falseArgument) implements DensityFunction {
    public static Codec<ConfigDensityFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("option").forGetter(ConfigDensityFunction::option),
        DensityFunction.CODEC.fieldOf("true_argument").orElse(Holder.direct(DensityFunctions.constant(1))).forGetter(ConfigDensityFunction::trueArgument),
        DensityFunction.CODEC.fieldOf("false_argument").orElse(Holder.direct(DensityFunctions.zero())).forGetter(ConfigDensityFunction::falseArgument)
    ).apply(instance, ConfigDensityFunction::new));

    public static KeyDispatchDataCodec<ConfigDensityFunction> CODEC_HOLDER = KeyDispatchDataCodec.of(CODEC);

    @Override
    public double compute(@NotNull FunctionContext context) {
        if (this.option().equals("terrain_scale")) return ConfigHandler.getConfig().getValue("terrain_scale");
        boolean enabled = ConfigHandler.getConfig().getValue(this.option()) == 1;
        Holder<DensityFunction> toCompute = enabled ? this.trueArgument() : this.falseArgument();
        return toCompute.value().compute(context);
    }

    @Override
    public void fillArray(double @NotNull [] doubles, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(doubles, this);
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
