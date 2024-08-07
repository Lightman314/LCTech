package io.github.lightman314.lctech.common.core;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.items.*;
import io.github.lightman314.lctech.common.upgrades.TechUpgradeTypes;
import io.github.lightman314.lightmanscurrency.common.items.CapacityUpgradeItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

public class ModItems {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		FLUID_SHARD = ModRegistries.ITEMS.register("fluid_shard", () -> new FluidShardItem(new Item.Properties()));
		
		FLUID_CAPACITY_UPGRADE_1 = ModRegistries.ITEMS.register("fluid_capacity_upgrade_1", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity1.get() * FluidType.BUCKET_VOLUME, new Item.Properties()));
		FLUID_CAPACITY_UPGRADE_2 = ModRegistries.ITEMS.register("fluid_capacity_upgrade_2", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity2.get() * FluidType.BUCKET_VOLUME, new Item.Properties()));
		FLUID_CAPACITY_UPGRADE_3 = ModRegistries.ITEMS.register("fluid_capacity_upgrade_3", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity3.get() * FluidType.BUCKET_VOLUME, new Item.Properties()));
		FLUID_CAPACITY_UPGRADE_4 = ModRegistries.ITEMS.register("fluid_capacity_upgrade_4", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity4.get() * FluidType.BUCKET_VOLUME, new Item.Properties()));

		BATTERY = ModRegistries.ITEMS.register("battery", () -> new BatteryItem(TechConfig.SERVER.batteryCapacity, new Item.Properties()));
		BATTERY_LARGE = ModRegistries.ITEMS.register("battery_large", () -> new BatteryItem(TechConfig.SERVER.largeBatteryCapacity, new Item.Properties()));
		
		ENERGY_CAPACITY_UPGRADE_1 = ModRegistries.ITEMS.register("energy_capacity_upgrade_1", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, TechConfig.SERVER.energyUpgradeCapacity1, new Item.Properties()));
		ENERGY_CAPACITY_UPGRADE_2 = ModRegistries.ITEMS.register("energy_capacity_upgrade_2", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, TechConfig.SERVER.energyUpgradeCapacity2, new Item.Properties()));
		ENERGY_CAPACITY_UPGRADE_3 = ModRegistries.ITEMS.register("energy_capacity_upgrade_3", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, TechConfig.SERVER.energyUpgradeCapacity3, new Item.Properties()));
		ENERGY_CAPACITY_UPGRADE_4 = ModRegistries.ITEMS.register("energy_capacity_upgrade_4", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, TechConfig.SERVER.energyUpgradeCapacity4, new Item.Properties()));

	}
	
	//Fluid Shard
	public static final Supplier<Item> FLUID_SHARD;
	
	//Fluid Upgrades
	public static final Supplier<Item> FLUID_CAPACITY_UPGRADE_1;
	public static final Supplier<Item> FLUID_CAPACITY_UPGRADE_2;
	public static final Supplier<Item> FLUID_CAPACITY_UPGRADE_3;
	public static final Supplier<Item> FLUID_CAPACITY_UPGRADE_4;

	//Battery Item
	public static final Supplier<BatteryItem> BATTERY;
	public static final Supplier<BatteryItem> BATTERY_LARGE;
	
	//Energy Upgrades
	public static final Supplier<Item> ENERGY_CAPACITY_UPGRADE_1;
	public static final Supplier<Item> ENERGY_CAPACITY_UPGRADE_2;
	public static final Supplier<Item> ENERGY_CAPACITY_UPGRADE_3;
	public static final Supplier<Item> ENERGY_CAPACITY_UPGRADE_4;

}
