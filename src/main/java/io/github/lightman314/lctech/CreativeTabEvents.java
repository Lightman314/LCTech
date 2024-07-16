package io.github.lightman314.lctech;

import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lightmanscurrency.ModCreativeGroups;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = LCTech.MODID)
public class CreativeTabEvents {

    @SubscribeEvent
    public static void buildTabContents(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab() == ModCreativeGroups.MACHINE_GROUP.get())
        {
            //Fluid Tanks
            event.accept(ModBlocks.IRON_TANK.get());
            event.accept(ModBlocks.GOLD_TANK.get());
            event.accept(ModBlocks.DIAMOND_TANK.get());
            event.accept(ModBlocks.NETHERITE_TANK.get());
            event.accept(ModBlocks.VOID_TANK.get());
            //Fluid Interface
            event.accept(ModBlocks.FLUID_TRADER_INTERFACE.get());
            //Batteries
            event.accept(ModItems.BATTERY.get());
            event.accept(IBatteryItem.getFullBattery(ModItems.BATTERY.get()));
            event.accept(ModItems.BATTERY_LARGE.get());
            event.accept(IBatteryItem.getFullBattery(ModItems.BATTERY_LARGE.get()));
            //Energy Interface
            event.accept(ModBlocks.ENERGY_TRADER_INTERFACE.get());
        }
        if(event.getTab() == ModCreativeGroups.TRADER_GROUP.get())
        {
            //Fluid Traders
            event.accept(ModBlocks.FLUID_TAP.get());
            event.accept(ModBlocks.FLUID_TAP_BUNDLE.get());
            //Fluid Network Traders
            event.accept(ModBlocks.FLUID_NETWORK_TRADER_1.get());
            event.accept(ModBlocks.FLUID_NETWORK_TRADER_2.get());
            event.accept(ModBlocks.FLUID_NETWORK_TRADER_3.get());
            event.accept(ModBlocks.FLUID_NETWORK_TRADER_4.get());
            //Energy Traders
            event.accept(ModBlocks.BATTERY_SHOP.get());
            //Energy Network Traders
            event.accept(ModBlocks.ENERGY_NETWORK_TRADER.get());
        }
        if(event.getTab() == ModCreativeGroups.UPGRADE_GROUP.get())
        {
            //Fluid Capacity Upgrades
            event.accept(ModItems.FLUID_CAPACITY_UPGRADE_1.get());
            event.accept(ModItems.FLUID_CAPACITY_UPGRADE_2.get());
            event.accept(ModItems.FLUID_CAPACITY_UPGRADE_3.get());
            event.accept(ModItems.FLUID_CAPACITY_UPGRADE_4.get());
            //Energy Capacity Upgrades
            event.accept(ModItems.ENERGY_CAPACITY_UPGRADE_1.get());
            event.accept(ModItems.ENERGY_CAPACITY_UPGRADE_2.get());
            event.accept(ModItems.ENERGY_CAPACITY_UPGRADE_3.get());
            event.accept(ModItems.ENERGY_CAPACITY_UPGRADE_4.get());
        }
    }

}
