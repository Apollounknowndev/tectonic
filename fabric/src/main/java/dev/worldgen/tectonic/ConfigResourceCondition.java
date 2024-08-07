package dev.worldgen.tectonic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import static dev.worldgen.tectonic.Tectonic.id;

public record ConfigResourceCondition(String key) implements ResourceCondition {
    public static final MapCodec<ConfigResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(ConfigResourceCondition::key)
    ).apply(instance, ConfigResourceCondition::new));
    public static final ResourceConditionType<ConfigResourceCondition> TYPE = ResourceConditionType.create(id("config"), CODEC);

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable HolderLookup.Provider registryLookup) {
        return switch (this.key) {
            case "increased_height" -> ConfigHandler.getConfig().experimentalModule().increasedHeight();
            case "legacy" -> ConfigHandler.getConfig().legacyModule().enabled();
            default -> false;
        };
    }
}
