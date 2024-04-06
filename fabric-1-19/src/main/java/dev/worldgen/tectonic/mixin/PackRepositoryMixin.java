package dev.worldgen.tectonic.mixin;

import dev.worldgen.tectonic.TectonicRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mixin(value = PackRepository.class, priority = 2000)
public class PackRepositoryMixin {

    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;


    // To circumvent Fabric's lack of built-in pack ordering, Tectonic's built-in packs are in a separate repository source.
    @Inject(method = "<init>(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)V", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void tectonic$addTectonicSource(Pack.PackConstructor packConstructor, RepositorySource[] repositorySources, CallbackInfo ci) {
        sources = new LinkedHashSet<>(sources);
        for (RepositorySource source : sources) {
            if (source instanceof ServerPacksSource) {
                sources.add(new TectonicRepositorySource());
                break;
            }
        }
    }

    // Make available packs sorted, to allow Tectonic's packs to load in the correct order consistently.
    @ModifyVariable(method = "discoverAvailable", at = @At("STORE"), ordinal = 0)
    private Map<String, Pack> tectonic$linkedAvailableMap(Map<String, Pack> value) {
        return new LinkedHashMap<>();
    }
}
