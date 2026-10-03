package dev.worldgen.tectonic.lithostitched;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.apollib.Apollib;
import dev.worldgen.lithostitched.api.predicate.LoadPredicate;
import dev.worldgen.lithostitched.api.worldgen.modifier.WorldgenModifier;
import dev.worldgen.tectonic.Tectonic;
import dev.worldgen.tectonic.config.object.HeightLimits;
import dev.worldgen.tectonic.mixin.DimensionTypeAccessor;
import dev.worldgen.tectonic.mixin.NoiseSettingsAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Optional;


public record SetHeightLimitsModifier(Optional<LoadPredicate> predicate, int priority, Holder<DimensionType> dimensionType, Holder<NoiseGeneratorSettings> noiseSettings) implements WorldgenModifier {
    public static final MapCodec<SetHeightLimitsModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        LoadPredicate.CODEC.optionalFieldOf("predicate").forGetter(SetHeightLimitsModifier::predicate),
        WorldgenModifier.PRIORITY_DEFAULT_CODEC.forGetter(SetHeightLimitsModifier::priority),
        DimensionType.CODEC.fieldOf("dimension_type").forGetter(SetHeightLimitsModifier::dimensionType),
        NoiseGeneratorSettings.CODEC.fieldOf("noise_settings").forGetter(SetHeightLimitsModifier::noiseSettings)
    ).apply(instance, SetHeightLimitsModifier::new));

    @Override
    public void apply(RegistryAccess registries) {
        if (!Tectonic.CONFIG.getState().enabled()) return;

        HeightLimits limits = Tectonic.CONFIG.getState().globalTerrain.heightLimits;
        if (limits.isVanilla()) return;

        // Set heights on dimension type
        DimensionTypeAccessor typeAccessor = (DimensionTypeAccessor) (Object) this.dimensionType.value();
        typeAccessor.setMinY(limits.minY);
        typeAccessor.setHeight(limits.getHeight());
        typeAccessor.setLogicalHeight(limits.getHeight());

        // Set heights on noise settings
        NoiseSettingsAccessor settingsAccessor = (NoiseSettingsAccessor) (Object) this.noiseSettings.value().noiseSettings();
        settingsAccessor.setMinY(limits.minY);
        settingsAccessor.setHeight(limits.getHeight());

        // Ensure dimension type is synced
        WorldgenModifier.resetRegistrationInfo(Apollib.registry(registries, Registries.DIMENSION_TYPE), this.dimensionType);
    }

    @Override
    public int priority() {
        return this.priority;
    }

    @Override
    public MapCodec<SetHeightLimitsModifier> codec() {
        return CODEC;
    }
}
