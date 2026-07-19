package dev.worldgen.tectonic.platform.fabric;

//? if fabric {
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.tectonic.Tectonic;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;

//? if >= 26.1 {
import net.minecraft.resources.RegistryOps;
//? } else
//import net.minecraft.core.HolderLookup;

public record ConfigResourceCondition(String key) implements ResourceCondition {
	public static final MapCodec<ConfigResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(ConfigResourceCondition::key)
	).apply(instance, ConfigResourceCondition::new));
	public static final ResourceConditionType<ConfigResourceCondition> TYPE = ResourceConditionType.create(Tectonic.id("config"), CODEC);
	
	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}
	
	@Override
	//? if >=26.1 {
	public boolean test(RegistryOps.RegistryInfoLookup registries) {
	//? } else {
	/*public boolean test(HolderLookup.Provider registries) {
	*///? }
		return Tectonic.CONFIG.getState().test(this.key);
	}
}
//? }