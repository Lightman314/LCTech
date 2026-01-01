package io.github.lightman314.lctech.integration.computercraft;

import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.integration.computercraft.peripherals.energy.EnergyTraderPeripheral;
import io.github.lightman314.lctech.integration.computercraft.peripherals.fluid.FluidTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputerHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.TraderPeripheralSource;

public class TechComputerHelper {

    public static void setup()
    {
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

}