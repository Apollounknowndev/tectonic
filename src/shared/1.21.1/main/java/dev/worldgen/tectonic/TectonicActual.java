package dev.worldgen.tectonic;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.msrandom.multiplatform.annotations.Actual;

public class TectonicActual {
    @Actual
    public static Identifier idVanilla(String name) {
        return Identifier.withDefaultNamespace(name);
    }

    @Actual
    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Tectonic.MOD_ID, name);
    }

    @Actual
    public static int getBlendingVersion(CompoundTag tag) {
        return tag.getInt(Tectonic.BLENDING_KEY);
    }

    @Actual
    public static boolean isMonumentTag(CompoundTag tag) {
        return "minecraft:monument".equals(tag.getString("id"));
    }

    @Actual
    public static int computeMonumentOffset(CompoundTag tag) {
        ListTag children = tag.getList("Children", ListTag.TAG_COMPOUND);
        if (!children.isEmpty()) {
            CompoundTag firstChild = children.getCompound(0);
            int[] bb = firstChild.getIntArray("BB");
            if (bb.length >= 2) {
                // Vanilla monument building minY is 39; stored offset = storedMinY - 39
                return bb[1] - 39;
            }
        }
        return 0;
    }

    @Actual
    public static boolean canRunCommand(CommandSourceStack stack) {
        return stack.hasPermission(2);
    }

}
