package dev.worldgen.tectonic.mixin;

import com.google.common.collect.ImmutableMap;
import dev.worldgen.tectonic.Tectonic;
import dev.worldgen.tectonic.TectonicRepositorySource;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Debug(export = true)
@Mixin(value = PackRepository.class, priority = 500)
public class PackRepositoryMixin {

    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Shadow
    @Final
    @Mutable
    private Pack.PackConstructor constructor;

    // Make available packs sorted, to allow Tectonic's packs to load in the correct order consistently.
    @ModifyVariable(method = "discoverAvailable", at = @At("STORE"), ordinal = 0)
    private Map<String, Pack> tectonic$linkedAvailableMap(Map<String, Pack> value) {
        return new LinkedHashMap<>();
    }

    // To circumvent Fabric's lack of built-in pack ordering, Tectonic's built-in packs are in a separate repository source.
    @Inject(method = "discoverAvailable", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void tectonic$linkedAvailableMap(CallbackInfoReturnable<Map<String, Pack>> cir) {
        for (RepositorySource source : sources) {
            if (source instanceof ServerPacksSource) {
                Map<String, Pack> map = new LinkedHashMap<>(cir.getReturnValue());
                new TectonicRepositorySource().loadPacks((pack) -> map.put(pack.getId(), pack), this.constructor);
                cir.setReturnValue(ImmutableMap.copyOf(map));
                break;
            }
        }
    }
}
