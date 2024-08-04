package dev.worldgen.tectonic;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tectonic {
    public static final String MOD_ID = "tectonic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static Codec<HolderSet<DensityFunction>> holderSetCodec;
    public static void init(Codec<HolderSet<DensityFunction>> codec) {
        holderSetCodec = codec;
    }

    public static ResourceLocation idOf(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static Codec<HolderSet<DensityFunction>> getHolderSetCodec() {
        return holderSetCodec;
    }
}