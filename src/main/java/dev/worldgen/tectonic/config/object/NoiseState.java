package dev.worldgen.tectonic.config.object;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseState {
    public double scale;
    public double multiplier;
    public double offset;
    public boolean smootherScaling;

    public NoiseState(double scale, double multiplier, double offset) {
        this.scale = scale;
        this.multiplier = multiplier;
        this.offset = offset;
        this.smootherScaling = false;
    }
    
    public NoiseState(double scale, double multiplier, double offset, boolean smootherScaling) {
        this.scale = scale;
        this.multiplier = multiplier;
        this.offset = offset;
        this.smootherScaling = smootherScaling;
    }
    
    public static NoiseState create() {
        return new NoiseState(0.25, 1, 0);
    }
    
    public static NoiseState create(double scale) {
        return new NoiseState(scale, 1, 0);
    }
    
    public NoiseState copy() {
        return new NoiseState(this.scale, this.multiplier, this.offset, this.smootherScaling);
    }

    public static MapCodec<NoiseState> legacyCodec(String name) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf(name + "_scale").forGetter(state -> state.scale),
            Codec.DOUBLE.fieldOf(name + "_multiplier").forGetter(state -> state.multiplier),
            Codec.DOUBLE.fieldOf(name + "_offset").forGetter(state -> state.offset)
        ).apply(instance, NoiseState::new));
    }
    
    public static MapCodec<NoiseState> codec(String name, NoiseState initialState) {
        return RecordCodecBuilder.<NoiseState>create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("scale").orElse(initialState.scale).forGetter(state -> state.scale),
            Codec.DOUBLE.fieldOf("multiplier").orElse(initialState.multiplier).forGetter(state -> state.multiplier),
            Codec.DOUBLE.fieldOf("offset").orElse(initialState.offset).forGetter(state -> state.offset)
        ).apply(instance, NoiseState::new)).fieldOf(name).orElse(initialState);
    }
}
