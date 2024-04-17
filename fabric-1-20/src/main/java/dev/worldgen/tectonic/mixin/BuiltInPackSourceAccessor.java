package dev.worldgen.tectonic.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BuiltInPackSource.class)
public interface BuiltInPackSourceAccessor {
    @Accessor("packType")
    PackType getPackType();

    @Invoker("fixedResources")
    static Pack.ResourcesSupplier createSupplier(final PackResources packResources) {
        throw new AssertionError();
    };
}
