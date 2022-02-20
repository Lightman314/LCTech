package io.github.lightman314.lctech.core;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.items.*;
import io.github.lightman314.lctech.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
	
	private static final List<Item> ITEMS = Lists.newArrayList();
	
	//Fluid Shard
	public static final Item FLUID_SHARD = register("fluid_shard", new FluidShardItem(new Item.Properties().maxStackSize(1)));
	
	//Fluid Upgrades
	public static final Item FLUID_CAPACITY_UPGRADE_1 = register("fluid_capacity_upgrade_1", new CapacityUpgradeItem(UpgradeType.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity1.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	public static final Item FLUID_CAPACITY_UPGRADE_2 = register("fluid_capacity_upgrade_2", new CapacityUpgradeItem(UpgradeType.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity2.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	public static final Item FLUID_CAPACITY_UPGRADE_3 = register("fluid_capacity_upgrade_3", new CapacityUpgradeItem(UpgradeType.FLUID_CAPACITY, () -> TechConfig.SERVER.fluidUpgradeCapacity3.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	
	//Battery Item
	public static final Item BATTERY = register("battery", new BatteryItem(() -> TechConfig.SERVER.batteryCapacity.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	public static final Item BATTERY_LARGE = register("battery_large", new BatteryItem(() -> TechConfig.SERVER.largeBatteryCapacity.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	
	//Energy Upgrades
	public static final Item ENERGY_CAPACITY_UPGRADE_1 = register("energy_capacity_upgrade_1", new CapacityUpgradeItem(UpgradeType.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity1.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	public static final Item ENERGY_CAPACITY_UPGRADE_2 = register("energy_capacity_upgrade_2", new CapacityUpgradeItem(UpgradeType.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity2.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	public static final Item ENERGY_CAPACITY_UPGRADE_3 = register("energy_capacity_upgrade_3", new CapacityUpgradeItem(UpgradeType.ENERGY_CAPACITY, () -> TechConfig.SERVER.energyUpgradeCapacity3.get(), new Item.Properties().group(LightmansCurrency.MACHINE_GROUP)));
	
	private static Item register(String name, Item item)
	{
		item.setRegistryName(name);
		ITEMS.add(item);
		return item;
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		ITEMS.forEach(item -> event.getRegistry().register(item));
		ITEMS.clear();
	}
	
}
