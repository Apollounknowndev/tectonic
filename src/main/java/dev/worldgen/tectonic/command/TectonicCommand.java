package dev.worldgen.tectonic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.worldgen.lithostitched.api.worldgen.densityfunction.SimpleContext;
import dev.worldgen.lithostitched.api.worldgen.util.DensityFunctionWrapper;
import dev.worldgen.lithostitched.mixin.common.RandomStateAccessor;
import dev.worldgen.tectonic.Tectonic;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.literal;

public class TectonicCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> locate = literal("locate");
        addTarget(locate, "mountain_range", LocateTargets.MOUNTAIN_RANGE, () -> true);
        addTarget(locate, "underground_river", LocateTargets.UNDERGROUND_RIVER, () -> Tectonic.CONFIG.getState().continents.undergroundRivers);
        addTarget(locate, "jungle_pillars", LocateTargets.JUNGLE_PILLARS, () -> Tectonic.CONFIG.getState().continents.junglePillars);
        addTarget(locate, "rolling_hills", LocateTargets.ROLLING_HILLS, () -> Tectonic.CONFIG.getState().continents.rollingHills);
        addTarget(locate, "badlands_canyon", LocateTargets.BADLANDS_CANYON, () -> true);
        addTarget(locate, "badlands_plateaus", LocateTargets.BADLANDS_PLATEAUS, () -> true);
        addTarget(locate, "desert_dunes", LocateTargets.DESERT_DUNES, () -> true);
        addTarget(locate, "cherry_valley", LocateTargets.CHERRY_VALLEY, () -> true);

        dispatcher.register(
            literal("tectonic").requires(TectonicCommand::canRunCommand)
            .then(literal("debug").executes(TectonicCommand::debugOutput))
            .then(locate)
        );
    }
    
    private static void addTarget(LiteralArgumentBuilder<CommandSourceStack> locate, String name, Map<String, InclusiveRange<Double>> target, Supplier<Boolean> enabled) {
        locate.then(literal(name).executes(context -> locate(context, name, target, enabled)));
    }

    private static int debugOutput(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition());
        var dfRegistry = level.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION);
        
        DensityFunctionWrapper helper = getDensityFunctionWrapper(source);
        if (helper == null) return 0;

        message(source, Component.literal("Tectonic debug info:"));

        message(source, getRegion(
            get(dfRegistry.getOrThrow(key("noise/continent/erosion")), helper, origin),
            get(dfRegistry.getOrThrow(key("noise/region_selector")), helper, origin)
        ));

        message(source, Component.translatableWithFallback(
            "command.tectonic.depth_cutoff",
            "Depth cutoff: %s",
            get(dfRegistry.getOrThrow(key("__constants/cave/depth_cutoff")), helper, origin)
        ));

        return 1;
    }

    private static int locate(CommandContext<CommandSourceStack> context, String name, Map<String, InclusiveRange<Double>> targets, Supplier<Boolean> enabled) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition());
        HolderLookup.RegistryLookup<DensityFunction> registry = level.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION);

        if (!enabled.get()) {
            failure(source, "Tectonic terrain feature " + name + " is not enabled.");
            return 0;
        }
        DensityFunctionWrapper wrapper = getDensityFunctionWrapper(source);
        if (wrapper == null) return 0;

        for (BlockPos.MutableBlockPos offset : BlockPos.spiralAround(BlockPos.ZERO, 400, Direction.EAST, Direction.SOUTH)) {
            int x = origin.getX() + offset.getX() * 64;
            int z = origin.getZ() + offset.getZ() * 64;
            BlockPos pos = new BlockPos(x, 0, z);

            boolean allMatch = true;
            for (var target : targets.entrySet()) {
                if (!target.getValue().isValueInRange(get(registry.getOrThrow(key(target.getKey())), wrapper, pos))) {
                    allMatch = false;
                }
            }
            if (allMatch) {
                MutableComponent component = ComponentUtils.wrapInSquareBrackets(
                    Component.translatable("chat.coordinates", pos.getX(), "~", pos.getZ())
                ).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(getClickEvent(pos)).withHoverEvent(getHoverEvent()));
                int dist = Mth.floor(dist(origin.getX(), origin.getZ(), pos.getX(), pos.getZ()));

                message(source, Component.translatableWithFallback(
                    "command.tectonic.locate." + name,
                    "The nearest " + name + " is at %s (%s blocks away)",
                    component,
                    dist
                ));
                return 1;
            }
        }

        message(source, Component.translatableWithFallback(
            "command.tectonic.locate_failed",
            "Couldn't find " + name + " within a 25,000 block radius."
        ));
        return 0;
    }

    private static float dist(int x1, int z1, int x2, int z2) {
        int i = x2 - x1;
        int j = z2 - z1;
        return Mth.sqrt(i * i + j * j);
    }
    
    private static boolean canRunCommand(CommandSourceStack stack) {
        //? if >=26.1 {
        return Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(stack);
        //? } else {
        /*return stack.hasPermission(2);
         *///? }
    }

    private static ClickEvent getClickEvent(BlockPos pos) {
        //? if >=26.1 {
        return new ClickEvent.SuggestCommand("/tp @s " + pos.getX() + " ~ " + pos.getZ());
        //? } else {
        /*return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " ~ " + pos.getZ());
         *///? }
    }
    
    private static HoverEvent getHoverEvent() {
        //? if >=26.1 {
        return new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"));
        //? } else {
        /*return new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"));
         *///? }
    }

    private static DensityFunctionWrapper getDensityFunctionWrapper(CommandSourceStack source) {
        ServerLevel level = source.getLevel();

        if (!Tectonic.CONFIG.getState().general.modEnabled) {
            failure(source, "Tectonic is not currently enabled.");
            return null;
        }
        if (!(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            failure(source, "Tectonic generation is not present here.");
            return null;
        }
        NoiseGeneratorSettings settings = generator.generatorSettings().value();
        RandomState randomState = RandomState.create(settings, level.registryAccess().lookupOrThrow(Registries.NOISE), level.getSeed());
        return new DensityFunctionWrapper(
            level.getSeed(),
            settings.useLegacyRandomSource(),
            randomState,
            ((RandomStateAccessor)(Object)randomState).getRandom()
        );
    }

    private static void message(CommandSourceStack source, Component message) {
        source.sendSystemMessage(message);
    }

    private static void failure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    private static double get(Holder<DensityFunction> holder, DensityFunctionWrapper wrapper, BlockPos pos) {
        double d = holder.value().mapAll(wrapper).compute(SimpleContext.of(pos));
        return (double) Math.round(d * 1000) / 1000;
    }

    private static Component getRegion(double erosion, double regionSelector) {
        String iconText;
        String nameText;
        if (erosion < 0) {
            if (regionSelector < 0) {
                iconText = "♣";
                nameText = "Club";
            } else {
                iconText = "♥";
                nameText = "Heart";
            }
        } else {
            if (regionSelector < 0) {
                iconText = "♠";
                nameText = "Spade";
            } else {
                iconText = "♦";
                nameText = "Diamond";
            }
        }
        MutableComponent icon = Component.literal(iconText);
        icon.withStyle(regionSelector < 0 ? ChatFormatting.DARK_GRAY : ChatFormatting.RED);

        MutableComponent name = Component.literal(nameText);

        return Component.translatableWithFallback("command.tectonic.region", "Region: %s %s (Erosion %s / Region Selector %s)", icon, name, erosion, regionSelector);
    }

    private static ResourceKey<DensityFunction> key(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, Tectonic.id(name));
    }

    private interface LocateTargets {
        Map<String, InclusiveRange<Double>> UNDERGROUND_RIVER = Map.of(
            "noise/continent/ridges_folded", range(0.0, 0.025),
            "noise/raw_continents", range(-0.1, 64),
            "noise/continent/erosion_folded", range(0.0, 0.225)
        );
        Map<String, InclusiveRange<Double>> MOUNTAIN_RANGE = Map.of(
            "noise/continent/erosion_folded", range(0.0, 0.05),
            "noise/raw_continents", range(0.1, 64)
        );
        Map<String, InclusiveRange<Double>> JUNGLE_PILLARS = Map.of(
            "noise/continent/ridges", range(-0.75, -0.2),
            "noise/vegetation_index", range(4, 5),
            "noise/temperature_index", range(4, 4),
            "noise/region_selector", range(0.1, 64),
            "noise/continent/erosion", range(-0.8, -0.45),
            "noise/raw_continents", range(0.1, 64)
        );
        Map<String, InclusiveRange<Double>> ROLLING_HILLS = Map.of(
            "noise/continent/ridges_folded", range(0.2, 64),
            "noise/vegetation_index", range(0, 2),
            "noise/temperature_index", range(0, 3),
            "noise/region_selector", range(0.1, 64),
            "noise/continent/erosion", range(-0.8, -0.45),
            "noise/raw_continents", range(0.1, 64)
        );
        Map<String, InclusiveRange<Double>> BADLANDS_CANYON = Map.of(
            "noise/continent/ridges_folded", range(0.2, 64),
            "noise/temperature_index", range(5, 5),
            "noise/region_selector", range(-64, 0),
            "noise/continent/erosion", range(0.45, 0.8),
            "noise/raw_continents", range(0.35, 64)
        );
        Map<String, InclusiveRange<Double>> BADLANDS_PLATEAUS = Map.of(
            "noise/continent/ridges_folded", range(0.2, 64),
            "noise/temperature_index", range(5, 5),
            "noise/region_selector", range(-64, 0),
            "noise/continent/erosion", range(-8, -0.45),
            "noise/raw_continents", range(0.35, 64)
        );
        Map<String, InclusiveRange<Double>> DESERT_DUNES = Map.of(
            "noise/continent/ridges_folded", range(0.2, 64),
            "noise/temperature_index", range(5, 5),
            "noise/region_selector", range(0.1, 64),
            "noise/continent/erosion", range(0.45, 0.8),
            "noise/raw_continents", range(0.1, 64)
        );
        Map<String, InclusiveRange<Double>> CHERRY_VALLEY = Map.of(
            "noise/continent/ridges_folded", range(0.4, 64),
            "noise/vegetation_index", range(0, 2),
            "noise/temperature_index", range(3, 3),
            "noise/region_selector", range(-64, 0),
            "noise/continent/erosion", range(-0.8, -0.45),
            "noise/raw_continents", range(0.35, 64)
        );

        private static InclusiveRange<Double> range(double min, double max) {
            return new InclusiveRange<>(min, max);
        }
    }
}
