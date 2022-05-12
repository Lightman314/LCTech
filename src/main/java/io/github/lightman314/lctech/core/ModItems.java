package io.github.lightman314.lctech.core;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.items.*;
import io.github.lightman314.lctech.upgrades.TechUpgradeTypes;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.CapacityUpgradeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LCTech.MODID)
public class ModItems {
	
	public static void init() {
		
		ModRegistries.ITEMS.register("fluid_shard", () -> new FluidShardItem(new Item.Properties()));
		
		ModRegistries.ITEMS.register("fluid_capacity_upgrade_1", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity1.get() * FluidAttributes.BUCKET_VOLUME, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("fluid_capacity_upgrade_2", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity2.get() * FluidAttributes.BUCKET_VOLUME, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("fluid_capacity_upgrade_3", () -> new CapacityUpgradeItem(TechUpgradeTypes.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity3.get() * FluidAttributes.BUCKET_VOLUME, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		ModRegistries.ITEMS.register("battery", () -> new BatteryItem(() -> TechConfig.SERVER.batteryCapacity.get(), new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("battery_large", () -> new BatteryItem(() -> TechConfig.SERVER.largeBatteryCapacity.get(), new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		ModRegistries.ITEMS.register("energy_capacity_upgrade_1", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity1.get(), new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("energy_capacity_upgrade_2", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity2.get(), new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("energy_capacity_upgrade_3", () -> new CapacityUpgradeItem(TechUpgradeTypes.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity3.get(), new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
	}
	
	//Fluid Shard
	public static final Item FLUID_SHARD = null;
	
	//Fluid Upgrades
	public static final Item FLUID_CAPACITY_UPGRADE_1 = null;
	public static final Item FLUID_CAPACITY_UPGRADE_2 = null;
	public static final Item FLUID_CAPACITY_UPGRADE_3 = null;
	
	//Battery Item
	public static final BatteryItem BATTERY = null;
	public static final BatteryItem BATTERY_LARGE = null;
	
	//Energy Upgrades
	public static final Item ENERGY_CAPACITY_UPGRADE_1 = null;
	public static final Item ENERGY_CAPACITY_UPGRADE_2 = null;
	public static final Item ENERGY_CAPACITY_UPGRADE_3 = null;
	
}
