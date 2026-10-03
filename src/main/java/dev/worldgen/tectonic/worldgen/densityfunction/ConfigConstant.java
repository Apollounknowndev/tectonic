package dev.worldgen.tectonic.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.Tectonic;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;

public record ConfigConstant(float value) implements DensityFunction {
    public static MapCodec<ConfigConstant> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(df -> "")
    ).apply(instance, ConfigConstant::create));

    public static ConfigConstant create(String key) {
        return new ConfigConstant(Tectonic.CONFIG.getState().getValue(key));
    }
    
    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new ConstantFunction.Sampler(this.value);
    }
    
    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return DensityFunctions.constant(this.value);
    }
    
    @Override
    public Interval range() {
        return Interval.ofExact(this.value);
    }
    
    @Override
    public @Axes int domainAxes() {
        return 0;
    }
    
    @Override
    public MapCodec<? extends DensityFunction> codec() {
        return ConfigConstant.CODEC;
    }
}
