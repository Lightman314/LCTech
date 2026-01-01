package io.github.lightman314.lctech.integration.computercraft.peripherals.energy;

import dan200.computercraft.api.lua.LuaException;
import io.github.lightman314.lctech.common.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.InputTraderPeripheral;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyTraderPeripheral extends InputTraderPeripheral<EnergyTraderBlockEntity,EnergyTraderData> {

    public EnergyTraderPeripheral(EnergyTraderBlockEntity energyTraderBlockEntity) { super(energyTraderBlockEntity); }
    public EnergyTraderPeripheral(EnergyTraderData trader) { super(trader); }

    @Override
    public String getType() { return "lct_trader_energy"; }

    public int getEnergy() throws LuaException { return this.getTrader().getTotalEnergy(); }
    public int getEnergyPendingDrain() throws LuaException { return this.getTrader().getPendingDrain(); }
    public int getEnergyAvailable() throws LuaException { return this.getTrader().getAvailableEnergy(); }
    public int getEnergyCapacity() throws LuaException{ return this.getTrader().getMaxEnergy(); }

    private Supplier<EnergyTradeData> tradeSource(int index) {
        return () -> {
            EnergyTraderData trader = this.safeGetTrader();
            if(trader != null && index >= 0 && index < trader.getTradeCount())
                return trader.getTrade(index);
            return null;
        };
    }

    @Override
    protected AccessTrackingPeripheral wrapTrade(TradeData trade) throws LuaException {
        int index = this.getTrader().indexOfTrade(trade);
        EnergyTradeWrapper wrapper = new EnergyTradeWrapper(this.tradeSource(index),this::safeGetTrader);
        wrapper.setParent(this);
        return wrapper;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("getEnergy").simple(this::getEnergy));
        registration.register(LCPeripheralMethod.builder("getEnergyPendingDrain").simple(this::getEnergyPendingDrain));
        registration.register(LCPeripheralMethod.builder("getEnergyAvailable").simple(this::getEnergyAvailable));
        registration.register(LCPeripheralMethod.builder("getEnergyCapacity").simple(this::getEnergyCapacity));
        this.registerGetTrade(registration);
    }
}