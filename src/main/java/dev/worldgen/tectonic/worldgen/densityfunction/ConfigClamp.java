package dev.worldgen.tectonic.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.op.ClampFunction;

// TODO: Remove this
public record ConfigClamp(DensityFunction input, DensityFunction min, DensityFunction max) implements DensityFunction {
    public static final MapCodec<ConfigClamp> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DensityFunction.CODEC.fieldOf("input").forGetter(ConfigClamp::input),
        DensityFunction.CODEC.fieldOf("min").forGetter(ConfigClamp::min),
        DensityFunction.CODEC.fieldOf("max").forGetter(ConfigClamp::max)
    ).apply(instance, ConfigClamp::new));
    
    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new ClampFunction.Sampler(this.input.compileSampler(context), this.min.range().max(), this.max.range().min());
    }
    
    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this.input.clamp(this.min.range().max(), this.max.range().min()).rewriteChildren(rule);
    }
    
    @Override
    public Interval range() {
        return Interval.clamp(this.input.range(), this.min.range().max(), this.max.range().min());
    }
    
    @Override
    public @Axes int domainAxes() {
        return this.input.domainAxes();
    }
    
    @Override
    public MapCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
