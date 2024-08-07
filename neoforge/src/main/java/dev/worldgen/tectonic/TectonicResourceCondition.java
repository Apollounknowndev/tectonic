package dev.worldgen.tectonic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public record TectonicResourceCondition(String key) implements ICondition {
    public static final MapCodec<TectonicResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(TectonicResourceCondition::key)
    ).apply(instance, TectonicResourceCondition::new));

    @Override
    public boolean test(@NotNull IContext context) {
        return switch (this.key) {
            case "increased_height" -> ConfigHandler.getConfig().experimentalModule().increasedHeight();
            case "legacy" -> ConfigHandler.getConfig().legacyModule().enabled();
            default -> false;
        };
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
