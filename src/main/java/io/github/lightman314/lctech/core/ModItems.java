package io.github.lightman314.lctech.core;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.items.*;
import io.github.lightman314.lctech.upgrades.TechUpgradeTypes;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.CapacityUpgradeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		FLUID_SHARD = ModRegistries.ITEMS.register("fluid_shard", () -> new FluidShardItem(new Item.Properties()));
		
		FLUID_CAPACITY_UPGRADE_1 = ModRegistries.ITEMS.register("fluid_capacity_upgrade_1", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity1.get() * FluidAttributes.BUCKET_VOLUME, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		FLUID_CAPACITY_UPGRADE_2 = ModRegistries.ITEMS.register("fluid_capacity_upgrade_2", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity2.get() * FluidAttributes.BUCKET_VOLUME, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		FLUID_CAPACITY_UPGRADE_3 = ModRegistries.ITEMS.register("fluid_capacity_upgrade_3", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity3.get() * FluidAttributes.BUCKET_VOLUME, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		
		BATTERY = ModRegistries.ITEMS.register("battery", () -> new BatteryItem(() -> TechConfig.SERVER.batteryCapacity.get(), new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		BATTERY_LARGE = ModRegistries.ITEMS.register("battery_large", () -> new BatteryItem(() -> TechConfig.SERVER.largeBatteryCapacity.get(), new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		ENERGY_CAPACITY_UPGRADE_1 = ModRegistries.ITEMS.register("energy_capacity_upgrade_1", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity1.get(), new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		ENERGY_CAPACITY_UPGRADE_2 = ModRegistries.ITEMS.register("energy_capacity_upgrade_2", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity2.get(), new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		ENERGY_CAPACITY_UPGRADE_3 = ModRegistries.ITEMS.register("energy_capacity_upgrade_3", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity3.get(), new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		
	}
	
	//Fluid Shard
	public static final RegistryObject<Item> FLUID_SHARD;
	
	//Fluid Upgrades
	public static final RegistryObject<Item> FLUID_CAPACITY_UPGRADE_1;
	public static final RegistryObject<Item> FLUID_CAPACITY_UPGRADE_2;
	public static final RegistryObject<Item> FLUID_CAPACITY_UPGRADE_3;
	
	//Battery Item
	public static final RegistryObject<BatteryItem> BATTERY;
	public static final RegistryObject<BatteryItem> BATTERY_LARGE;
	
	//Energy Upgrades
	public static final RegistryObject<Item> ENERGY_CAPACITY_UPGRADE_1;
	public static final RegistryObject<Item> ENERGY_CAPACITY_UPGRADE_2;
	public static final RegistryObject<Item> ENERGY_CAPACITY_UPGRADE_3;
	
}
