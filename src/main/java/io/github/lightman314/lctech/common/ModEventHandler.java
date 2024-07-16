package io.github.lightman314.lctech.common;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blocks.VoidTankBlock;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.core.util.TechBlockEntityBlockHelper;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = LCTech.MODID)
public class ModEventHandler {

    @SubscribeEvent
    private static void registerCapabilityProviders(@Nonnull RegisterCapabilitiesEvent event)
    {

        //Fluid Handlers
        //Register Fluid Handler for Fluid Traders
        TraderBlockEntity.easyRegisterCapProvider(event, Capabilities.FluidHandler.BLOCK, (t,s) -> {
            if(t instanceof FluidTraderData fluidTrader)
                return fluidTrader.getFluidHandler().getExternalHandler(s);
            return null;
        }, BlockEntityBlockHelper.getBlocksForBlockEntities(TechBlockEntityBlockHelper.FLUID_TRADER_TYPE));

        //Register Fluid Handler for Fluid Trader Interfaces
        IRotatableBlock.registerRotatableCapability(event, Capabilities.FluidHandler.BLOCK, ModBlockEntities.TRADER_INTERFACE_FLUID.get(), (be,relativeSide) -> be.getFluidHandler().getHandler(relativeSide));

        //Register Fluid Handler for Fluid Tanks
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FLUID_TANK.get(), (tank,side) -> tank.handler);
        event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, blockEntity, side) -> VoidTankBlock.VOID_HANDLER, ModBlocks.VOID_TANK.get());

        //Register Fluid Handler for the Fluid Tank Item
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack,c) -> FluidTankItem.createHandler(stack), ModBlocks.IRON_TANK.get(), ModBlocks.GOLD_TANK.get(), ModBlocks.DIAMOND_TANK.get(), ModBlocks.NETHERITE_TANK.get());
        //Register Fluid Handler for the Fluid Shard Item
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack,c) -> FluidShardItem.createHandler(stack), ModItems.FLUID_SHARD.get());

        //Register Energy Handler for the Energy Traders
        TraderBlockEntity.easyRegisterCapProvider(event, Capabilities.EnergyStorage.BLOCK, (t,s) -> {
            if(t instanceof EnergyTraderData energyTrader)
                return energyTrader.getEnergyHandler().getExternalHandler(s);
            return null;
        }, BlockEntityBlockHelper.getBlocksForBlockEntity(TechBlockEntityBlockHelper.ENERGY_TRADER_TYPE));

        //Register Energy Handler for Energy Trader Interfaces
        IRotatableBlock.registerRotatableCapability(event, Capabilities.EnergyStorage.BLOCK, ModBlockEntities.TRADER_INTERFACE_ENERGY.get(), (be,relativeSide) -> be.getEnergyHandler().getHandler(relativeSide));

        //Register Energy Handler for Battery Items
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack,c) -> IBatteryItem.createCapability(stack), ModItems.BATTERY.get(), ModItems.BATTERY_LARGE.get());

    }

}
