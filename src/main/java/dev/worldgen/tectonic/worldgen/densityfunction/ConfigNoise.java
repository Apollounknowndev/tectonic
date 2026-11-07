package dev.worldgen.tectonic.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.Tectonic;
import dev.worldgen.tectonic.config.object.NoiseState;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record ConfigNoise(Holder<NormalNoise> noise, DensityFunction shiftX, DensityFunction shiftZ, float scale, float multiplier, float offset, boolean smootherScaling) implements DensityFunction {
    public static MapCodec<ConfigNoise> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(df -> ""),
        NormalNoise.CODEC.fieldOf("noise").forGetter(ConfigNoise::noise),
        DensityFunction.CODEC.fieldOf("shift_x").forGetter(ConfigNoise::shiftX),
        DensityFunction.CODEC.fieldOf("shift_z").forGetter(ConfigNoise::shiftZ)
    ).apply(instance, ConfigNoise::create));
    
    public static ConfigNoise create(String key, Holder<NormalNoise> noise, DensityFunction shiftX, DensityFunction shiftZ) {
        NoiseState state = Tectonic.CONFIG.getState().getNoiseState(key);
        return new ConfigNoise(noise, shiftX, shiftZ, state.scale, state.multiplier, state.offset, state.smootherScaling);
    }
    
    @Override
    public DensitySampler compileSampler(CompileContext context) {
        Noise noise = context.createNoiseSampler(this.noise);
        DensitySampler shiftX = this.shiftX.compileSampler(context);
        DensitySampler shiftZ = this.shiftZ.compileSampler(context);
        if (this.smootherScaling) {
            return new SmoothSampler(noise, shiftX, shiftZ, scale, multiplier, offset);
        }
        return new NormalSampler(noise, shiftX, shiftZ, scale, multiplier, offset);
    }
    
    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction shiftX = rule.rewrite(this.shiftX);
        DensityFunction shiftZ = rule.rewrite(this.shiftZ);
        return shiftX == this.shiftX && shiftZ == this.shiftZ ? this : new ConfigNoise(
            this.noise, shiftX, shiftZ, this.scale, this.multiplier, this.offset, this.smootherScaling
        );
    }
    
    @Override
    public Interval range() {
        return this.noise.value().range();
    }
    
    @Override
    public @Axes int domainAxes() {
        return 5 | this.shiftX.domainAxes() | this.shiftZ.domainAxes();
    }
    
    @Override
    public MapCodec<? extends DensityFunction> codec() {
        return ConfigNoise.CODEC;
    }
    
    public record NormalSampler(Noise noise, DensitySampler shiftX, DensitySampler shiftZ, float scale, float multiplier, float offset) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.shiftX.sampleVolume(context, outputBuffer, volume);
            
            try (ScopedDensityBuffer shiftZBuffer = context.acquireBuffer(volume)) {
                this.shiftZ.sampleVolume(context, shiftZBuffer, volume);
                int index = 0;
                
                for(int z = 0; z < volume.sizeZ(); ++z) {
                    double baseNoiseZ = (double)volume.blockZ(z) * this.scale;
                    
                    for(int x = 0; x < volume.sizeX(); ++x) {
                        double baseNoiseX = (double)volume.blockX(x) * this.scale;
                        
                        for(int y = 0; y < volume.sizeY(); ++y) {
                            double noiseX = baseNoiseX + (double)outputBuffer.get(index);
                            double noiseZ = baseNoiseZ + (double)shiftZBuffer.get(index);
                            outputBuffer.set(index, this.noise.get(noiseX, 0, noiseZ) * this.multiplier + this.offset);
                            ++index;
                        }
                    }
                }
            }
        }
        
        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            double x = blockX * this.scale + this.shiftX.sampleValue(context, blockX, blockY, blockZ);
            double z = blockZ * this.scale + this.shiftZ.sampleValue(context, blockX, blockY, blockZ);
            return this.noise.get(x, 0, z) * this.multiplier + this.offset;
        }
    }
    
    public record SmoothSampler(Noise noise, DensitySampler shiftX, DensitySampler shiftZ, float scale, float multiplier, float offset) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.shiftX.sampleVolume(context, outputBuffer, volume);
            
            try (ScopedDensityBuffer shiftZBuffer = context.acquireBuffer(volume)) {
                this.shiftZ.sampleVolume(context, shiftZBuffer, volume);
                int index = 0;
                
                for(int z = 0; z < volume.sizeZ(); ++z) {
                    double baseNoiseZ = volume.blockZ(z);
                    
                    for(int x = 0; x < volume.sizeX(); ++x) {
                        double baseNoiseX = volume.blockX(x);
                        
                        for(int y = 0; y < volume.sizeY(); ++y) {
                            double noiseX = (baseNoiseX + (double)outputBuffer.get(index)) * this.scale;
                            double noiseZ = (baseNoiseZ + (double)shiftZBuffer.get(index)) * this.scale;
                            outputBuffer.set(index, this.noise.get(noiseX, 0, noiseZ) * this.multiplier + this.offset);
                            ++index;
                        }
                    }
                }
            }
        }
        
        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            double x = blockX * this.scale + this.shiftX.sampleValue(context, blockX, blockY, blockZ);
            double z = blockZ * this.scale + this.shiftZ.sampleValue(context, blockX, blockY, blockZ);
            return this.noise.get(x, 0, z) * this.multiplier + this.offset;
        }
    }
}
