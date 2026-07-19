package dev.worldgen.tectonic.platform.fabric;

//? if fabric {
import dev.worldgen.tectonic.Tectonic;
import dev.worldgen.tectonic.command.TectonicCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
//? if >= 26.1 {
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
//? } else {
/*import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.PackActivationType;
*///? }
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import static dev.worldgen.tectonic.Tectonic.id;

public class TectonicFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Tectonic.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> TectonicCommand.register(dispatcher));
        
        ResourceConditions.register(ConfigResourceCondition.TYPE);

        if (Tectonic.CONFIG.getState().enabled()) {
            //? if >= 26.1 {
            ResourceLoader.registerBuiltinPack(
            //? } else
            //ResourceManagerHelper.registerBuiltinResourcePack(
                id("tectonic"),
                FabricLoader.getInstance().getModContainer("tectonic").get(),
                Component.literal("Tectonic"),
                PackActivationType.ALWAYS_ENABLED
            );
        }
    }
}
//? }