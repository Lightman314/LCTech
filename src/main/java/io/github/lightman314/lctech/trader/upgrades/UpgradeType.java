package io.github.lightman314.lctech.trader.upgrades;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lctech.LCTech;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class UpgradeType implements IForgeRegistryEntry<UpgradeType>{

	private static final Map<ResourceLocation,UpgradeType> UPGRADE_TYPE_REGISTRY = Maps.newHashMap();
	
	public static final FluidCapacityUpgrade FLUID_CAPACITY = register(new ResourceLocation(LCTech.MODID,"fluid_trader"), new FluidCapacityUpgrade());
	
	private ResourceLocation type;

	public abstract boolean allowedForMachine(IUpgradeable machine);
	
	protected abstract List<String> getDataTags();
	protected abstract Object defaultTagValue(String tag);
	public List<Component> getTooltip(UpgradeData data) { return Lists.newArrayList(); }
	public final UpgradeData getDefaultData() { return new UpgradeData(this); }
	
	@Override
	public UpgradeType setRegistryName(ResourceLocation name) {
		this.type = name;
		return this;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return this.type;
	}

	@Override
	public Class<UpgradeType> getRegistryType() {
		return UpgradeType.class;
	}
	
	public static final <T extends UpgradeType> T register(ResourceLocation type, T upgradeType)
	{
		upgradeType.setRegistryName(type);
		UPGRADE_TYPE_REGISTRY.put(type, upgradeType);
		return upgradeType;
	}
	
	public interface IUpgradeable { }
	
	public interface IUpgradeItem
	{
		public UpgradeType getUpgradeType();
		public default boolean upgradeAllowedForMachine(IUpgradeable machine) { return getUpgradeType().allowedForMachine(machine); }
		public UpgradeData getDefaultUpgradeData();
	}
	
	public static class UpgradeData
	{
		
		private final Map<String,Object> data = Maps.newHashMap();
		
		public Set<String> getKeys() { return data.keySet(); }
		
		public UpgradeData(UpgradeType upgrade)
		{
			for(String tag : upgrade.getDataTags())
			{
				Object defaultValue = upgrade.defaultTagValue(tag);
				data.put(tag, defaultValue);
			}
		}
		
		public void setValue(String tag, Object value)
		{
			if(data.containsKey(tag))
				data.put(tag, value);
		}
		
		public Object getValue(String tag)
		{
			if(data.containsKey(tag))
				return data.get(tag);
			return null;
		}
		
		public int getIntValue(String tag)
		{
			Object value = getValue(tag);
			if(value instanceof Integer)
				return (Integer)value;
			return 0;
		}
		
		public float getFloatValue(String tag)
		{
			Object value = getValue(tag);
			if(value instanceof Float)
				return (Float)value;
			return 0f;
		}
		
		public void read(CompoundTag compound)
		{
			
		}
		
		public CompoundTag writeToNBT() { return writeToNBT(null); }
		
		public CompoundTag writeToNBT(@Nullable UpgradeType source)
		{
			Map<String,Object> modifiedEntries = source == null ? this.data : getModifiedEntries(this,source);
			CompoundTag compound = new CompoundTag();
			modifiedEntries.forEach((key,value) ->{
				
			});
			return compound;
		}
		
		public static Map<String,Object> getModifiedEntries(UpgradeData queryData, UpgradeType source)
		{
			Map<String,Object> modifiedEntries = Maps.newHashMap();
			source.getDefaultData().data.forEach((key, value) -> {
				if(queryData.data.containsKey(key) && !Objects.equal(queryData.data.get(key), value))
						modifiedEntries.put(key, value);
			});
			return modifiedEntries;
		}
		
		
		
	}
	
}
