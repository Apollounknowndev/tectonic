package dev.worldgen.tectonic.client;

import dev.worldgen.apollib.client.gui.ApollibConfigScreen;
import dev.worldgen.apollib.client.gui.element.ApollibRowHelper;
import dev.worldgen.apollib.client.gui.element.ApollibRowHelper.Builder;
import dev.worldgen.apollib.client.gui.element.ApollibSlider;
import dev.worldgen.tectonic.Tectonic;
import dev.worldgen.tectonic.client.gui.PresetSelectorScreen;
import dev.worldgen.tectonic.config.ConfigState;
import dev.worldgen.tectonic.config.ConfigState.*;
import dev.worldgen.tectonic.config.object.NoiseState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static dev.worldgen.tectonic.config.ConfigState.Caves.*;
import static dev.worldgen.tectonic.config.ConfigState.Continents.*;
import static dev.worldgen.tectonic.config.ConfigState.General.*;
import static dev.worldgen.tectonic.config.ConfigState.GlobalTerrain.*;
import static dev.worldgen.tectonic.config.ConfigState.Islands.*;
import static dev.worldgen.tectonic.config.ConfigState.Oceans.*;

public class ConfigScreenBuilder {
	public static Screen build(Screen parent) {
		Minecraft minecraft = Minecraft.getInstance();
		return new ApollibConfigScreen<>(Tectonic.MOD_ID, parent, Tectonic.CONFIG, helper -> {
			helper.addBig(Button.builder(
				helper.text("view_presets"),
				button -> minecraft.gui.setScreen(new PresetSelectorScreen(helper.screen()))
			).width(310).build());
			helper.spacer();
			ConfigScreenBuilder.addButtons(helper);
		});
	}
	
	private static void addButtons(ApollibRowHelper helper) {
		ConfigState state = (ConfigState) helper.screen().modifiedState;
		General general = state.general;
		GlobalTerrain globalTerrain = state.globalTerrain;
		Continents continents = state.continents;
		Islands islands = state.islands;
		Oceans oceans = state.oceans;
		Biomes biomes = state.biomes;
		Caves caves = state.caves;
		Experimental experimental = state.experimental;
		
		alignLeft(helper.singleLineText("general")).format(ChatFormatting.BOLD).addBig();
		helper.booleanButton("mod_enabled", bool -> general.modEnabled = bool, general.modEnabled).withTooltip(MOD_ENABLED).addBig();
		helper.intSlider("snow_start_offset", 0, 256, 1, value -> general.snowStartOffset = value.intValue(), general.snowStartOffset).withTooltip(SNOW_START_OFFSET).addBig();
		
		helper.spacer();
		alignLeft(helper.singleLineText("global_terrain")).format(ChatFormatting.BOLD).addBig();
		helper.intSlider("min_y", -2032, -64, 16, value -> globalTerrain.heightLimits.minY = value.intValue(), globalTerrain.heightLimits.minY).withTooltip(HEIGHT_LIMITS.minY).addSmall();
		helper.intSlider("max_y", 256, 2032, 16, value -> globalTerrain.heightLimits.maxY = value.intValue(), globalTerrain.heightLimits.maxY).withTooltip(HEIGHT_LIMITS.maxY).addSmall();
		helper.doubleSlider("vertical_scale", 0.1f, 15, 0.125f, value -> globalTerrain.verticalScale = value, (float) globalTerrain.verticalScale).withTooltip(VERTICAL_SCALE).addBig();
		helper.doubleSlider("elevation_boost", 0, 1, 0.01f, value -> globalTerrain.elevationBoost = value, (float) globalTerrain.elevationBoost).withTooltip(ELEVATION_BOOST).addBig();
		helper.booleanButton("ultrasmooth", bool -> globalTerrain.ultrasmooth = bool, globalTerrain.ultrasmooth).withTooltip(ULTRASMOOTH).addBig();
		
		helper.spacer();
		alignLeft(helper.singleLineText("continents")).format(ChatFormatting.BOLD).addBig();
		helper.doubleSlider("ocean_offset", -2, 0, 0.05f, value -> continents.oceanOffset = value, (float) continents.oceanOffset).withTooltip(OCEAN_OFFSET).addBig();
		helper.doubleSlider("continents_scale", 0.01f, 1, 0.01f, value -> continents.continentsScale = value, (float) continents.continentsScale).withTooltip(CONTINENTS_SCALE).addBig();
		helper.doubleSlider("erosion_scale", 0.01f, 1, 0.01f, value -> continents.erosionScale = value, (float) continents.erosionScale).withTooltip(EROSION_SCALE).addBig();
		helper.doubleSlider("ridge_scale", 0.01f, 2, 0.01f, value -> continents.ridgeScale = value, (float) continents.ridgeScale).withTooltip(RIDGE_SCALE).addBig();
		helper.doubleSlider("flat_terrain_skew", -1, 1, 0.05f, value -> continents.flatTerrainSkew = value, (float) continents.flatTerrainSkew).withTooltip(FLAT_TERRAIN_SKEW).addBig();
		helper.booleanButton("underground_rivers", bool -> continents.undergroundRivers = bool, continents.undergroundRivers).withTooltip(UNDERGROUND_RIVERS).addBig();
		helper.booleanButton("river_lanterns", bool -> continents.riverLanterns = bool, continents.riverLanterns).withTooltip(RIVER_LANTERNS).addBig();
		helper.booleanButton("river_ice", bool -> continents.riverIce = bool, continents.riverIce).withTooltip(RIVER_ICE).addBig();
		helper.booleanButton("rolling_hills", bool -> continents.rollingHills = bool, continents.rollingHills).withTooltip(ROLLING_HILLS).addBig();
		helper.booleanButton("jungle_pillars", bool -> continents.junglePillars = bool, continents.junglePillars).withTooltip(JUNGLE_PILLARS).addBig();
		
		helper.spacer();
		alignLeft(helper.singleLineText("islands")).format(ChatFormatting.BOLD).addBig();
		helper.booleanButton("islands_enabled", bool -> islands.enabled = bool, islands.enabled).withTooltip(ENABLED).addBig();
		addNoise(helper, "noise", state.islands.noise, NOISE);
		
		helper.spacer();
		alignLeft(helper.singleLineText("oceans")).format(ChatFormatting.BOLD).addBig();
		helper.doubleSlider("ocean_depth", -10, -0.05f, 0.01f, value -> oceans.oceanDepth = value, (float) oceans.oceanDepth).withTooltip(OCEAN_DEPTH).addBig();
		helper.doubleSlider("deep_ocean_depth", -10, -0.05f, 0.01f, value -> oceans.deepOceanDepth = value, (float) oceans.deepOceanDepth).withTooltip(DEEP_OCEAN_DEPTH).addBig();
		helper.intSlider("monument_offset", -60, 0, 1, value -> oceans.monumentOffset = value.intValue(), oceans.monumentOffset).withTooltip(MONUMENT_OFFSET).addBig();
		helper.booleanButton("remove_frozen_ocean_ice", bool -> oceans.removeFrozenOceanIce = bool, oceans.removeFrozenOceanIce).withTooltip(REMOVE_FROZEN_OCEAN_ICE).addBig();
		
		helper.spacer();
		alignLeft(helper.singleLineText("biome_noises")).format(ChatFormatting.BOLD).addBig();
		alignLeft(helper.singleLineText("temperature")).format(ChatFormatting.GRAY).addBig();
		addNoise(helper, "temperature", biomes.temperature, NoiseState.create());
		alignLeft(helper.singleLineText("vegetation")).format(ChatFormatting.GRAY).addBig();
		addNoise(helper, "vegetation", biomes.vegetation, NoiseState.create());
		
		helper.spacer();
		alignLeft(helper.singleLineText("caves")).format(ChatFormatting.BOLD).addBig();
		helper.doubleSlider("depth_cutoff_start", -0.1f, 1, 0.1f, value -> caves.depthCutoffStart = value, (float) caves.depthCutoffStart).withTooltip(DEPTH_CUTOFF_START).addBig();
		helper.doubleSlider("depth_cutoff_size", 0, 1, 0.1f, value -> caves.depthCutoffSize = value, (float) caves.depthCutoffSize).withTooltip(DEPTH_CUTOFF_SIZE).addBig();
		helper.booleanButton("cheese_enabled", bool -> caves.cheeseEnabled = bool, caves.cheeseEnabled).withTooltip(CHEESE_ENABLED).addSmall();
		helper.doubleSlider("cheese_additive", -0.5f, 0.5f, 0.01f, value -> caves.cheeseAdditive = value, (float) caves.cheeseAdditive).withTooltip(CHEESE_ADDITIVE).addSmall();
		helper.booleanButton("noodle_enabled", bool -> caves.noodleEnabled = bool, caves.noodleEnabled).withTooltip(NOODLE_ENABLED).addSmall();
		helper.doubleSlider("noodle_additive", -0.25f, 0.25f, 0.025f, value -> caves.noodleAdditive = value, (float) caves.noodleAdditive).withTooltip(NOODLE_ADDITIVE).addSmall();
		helper.booleanButton("spaghetti_enabled", bool -> caves.spaghettiEnabled = bool, caves.spaghettiEnabled).withTooltip(SPAGHETTI_ENABLED).addBig();
		helper.booleanButton("carvers_enabled", bool -> caves.carversEnabled = bool, caves.carversEnabled).withTooltip(CARVERS_ENABLED).addBig();
		helper.booleanButton("lava_tunnels", bool -> globalTerrain.lavaTunnels = bool, globalTerrain.lavaTunnels).withTooltip(LAVA_TUNNELS).addBig();
		helper.booleanButton("ore_fix", bool -> caves.oreFix = bool, caves.oreFix).withTooltip(ORE_FIX).addBig();
		
		helper.spacer();
		alignLeft(helper.singleLineText("experimental")).format(ChatFormatting.BOLD).addBig();
		alignLeft(helper.singleLineText("alternate_noise_scaling")).format(ChatFormatting.GRAY).withTooltip(false).addBig();
		helper.booleanButton("alternate_erosion_scaling", bool -> experimental.alternateErosionScaling = bool, experimental.alternateErosionScaling).addSmall();
		helper.booleanButton("alternate_continents_scaling", bool -> experimental.alternateContinentsScaling = bool, experimental.alternateContinentsScaling).addSmall();
		alignLeft(helper.singleLineText("terrain_changes")).format(ChatFormatting.GRAY).addBig();
		helper.booleanButton("improved_jungle_pillars", bool -> experimental.improvedJunglePillars = bool, experimental.improvedJunglePillars).withTooltip(false).addSmall();
	}
	
	public static void addNoise(ApollibRowHelper helper, String name, NoiseState state, NoiseState defaultState) {
		LinearLayout layout = LinearLayout.horizontal().spacing(5);
		layout.addChild(slider(helper, name, "scale", 0, 1, 0.01f, value -> state.scale = value, state.scale, defaultState.scale));
		layout.addChild(slider(helper, name, "multiplier", 0, 5, 0.1f, value -> state.multiplier = value, state.multiplier, defaultState.multiplier));
		layout.addChild(slider(helper, name, "offset", -1, 1, 0.05f, value -> state.offset = value, state.offset, defaultState.offset));
		helper.wrapped().addChild(layout, 2);
	}
	
	private static ApollibSlider slider(ApollibRowHelper helper, String name, String suffix, float min, float max, float step, Consumer<Float> setter, double getter, double base) {
		ApollibSlider slider = new ApollibSlider(helper.option(suffix), new ApollibSlider.SliderConfig(min, max, step, (float) getter), setter);
		slider.setWidth(100);
		slider.setTooltip(Tooltip.create(
			helper.option(name + "_" + suffix + ".tooltip")
				.append(CommonComponents.NEW_LINE)
				.append(Component.translatable("config.apollib.default"))
				.append(Component.literal(String.valueOf(base)).withStyle(ChatFormatting.YELLOW))
		));
		return slider;
	}
	
	private static Builder<StringWidget> alignLeft(Builder<StringWidget> builder) {
		//? if < 26.1
		//builder.modifyWidget(StringWidget::alignLeft);
		return builder;
	}
}
