package dev.worldgen.tectonic.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.Tectonic;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;

/**
 * I've written some very terrible mod code for Tectonic to make it function in the past.
 * This is probably the worst.
 */
public record DynamicReferenceDensityFunction(HolderSet<DensityFunction> arguments, Holder<DensityFunction> fallback) implements DensityFunction {
    public static Codec<DynamicReferenceDensityFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Tectonic.getHolderSetCodec().fieldOf("arguments").forGetter(DynamicReferenceDensityFunction::arguments),
        DensityFunction.CODEC.fieldOf("fallback").forGetter(DynamicReferenceDensityFunction::fallback)
    ).apply(instance, DynamicReferenceDensityFunction::new));

    @Override
    public double compute(@NotNull FunctionContext context) {
        Holder<DensityFunction> toCompute;
        if (this.arguments().size() == 1) {
            toCompute = this.arguments().get(0);
        } else {
            toCompute = this.fallback();
        }
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
    public Codec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
