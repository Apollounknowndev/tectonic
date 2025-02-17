package dev.worldgen.tectonic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.config.ConfigHandler;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record TectonicResourceCondition(String key) implements ICondition {
    public static final MapCodec<TectonicResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(TectonicResourceCondition::key)
    ).apply(instance, TectonicResourceCondition::new));

    @Override
    public boolean test(@NotNull IContext context) {
        if (this.key.equals("increased_height")) {
            return ConfigHandler.getConfig().toggles().increasedHeight();
        }
        return false;
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
