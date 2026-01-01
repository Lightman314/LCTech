package io.github.lightman314.lctech.integration.computercraft;

import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.integration.computercraft.peripherals.energy.EnergyTraderPeripheral;
import io.github.lightman314.lctech.integration.computercraft.peripherals.fluid.FluidTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.TraderPeripheralSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class TechComputerHelper {

    public static void setup(IEventBus modBus)
    {
        //Register Event Listener
        modBus.addListener(TechComputerHelper::registerCapabilities);
        //Create Trader Peripheral Sources
        //Fluid Trader
        LCComputerHelper.registerTraderPeripheralSource(TraderPeripheralSource.simple(be -> {
            if(be instanceof FluidTraderBlockEntity ftbe)
                return new FluidTraderPeripheral(ftbe);
            return null;
        },trader -> {
            if(trader instanceof FluidTraderData ft)
                return new FluidTraderPeripheral(ft);
            return null;
        }));
        //Energy Trader
        LCComputerHelper.registerTraderPeripheralSource(TraderPeripheralSource.simple(be -> {
            if(be instanceof EnergyTraderBlockEntity etbe)
                return new EnergyTraderPeripheral(etbe);
            return null;
        },trader -> {
            if(trader instanceof EnergyTraderData et)
                return new EnergyTraderPeripheral(et);
            return null;
        }));
    }


    private static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        LCComputerHelper.registerTraderCapability(event,ModBlockEntities.FLUID_TRADER);
        LCComputerHelper.registerTraderCapability(event,ModBlockEntities.ENERGY_TRADER);
    }

}
