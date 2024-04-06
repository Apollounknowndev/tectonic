package dev.worldgen.tectonic.mixin;

import dev.worldgen.tectonic.Tectonic;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Forcefully fix Tectonic's pack format
@Mixin(ModNioResourcePack.class)
public abstract class ModNioResourcePackMixin {
    @Shadow
    public abstract ResourceLocation getId();

    @Inject(
        method = "getMetadataSection(Lnet/minecraft/server/packs/metadata/MetadataSectionSerializer;)Ljava/lang/Object;",
        at = @At("RETURN"),
        cancellable = true
    )
    private <T> void tectonic$fixPackFormat(MetadataSectionSerializer<T> metaReader, CallbackInfoReturnable<T> cir) {
        if (this.getId().getNamespace().equals(Tectonic.MOD_ID) && cir.getReturnValue() instanceof PackMetadataSection metadataSection) {
            cir.setReturnValue((T)new PackMetadataSection(metadataSection.getDescription(), 10));
        }
    }
}
