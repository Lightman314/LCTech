package io.github.lightman314.lctech.integration.computercraft.peripherals.energy;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TradeWrapper;

import java.util.function.Supplier;

public class EnergyTradeWrapper extends TradeWrapper<EnergyTradeData> {

    public EnergyTradeWrapper(Supplier<EnergyTradeData> tradeSource, Supplier<TraderData> trader) { super(tradeSource, trader); }

    @Override
    public String getType() { return "lct_trade_energy"; }

    public boolean setDirection(IComputerAccess computer, IArguments args) throws LuaException
    {
        TradeDirection direction = LCArgumentHelper.parseEnum(args,0,TradeDirection.class);
        if(direction != TradeDirection.SALE && direction != TradeDirection.PURCHASE)
            throw LuaValues.badArgumentOf(args,0,"tradedirection");
        EnergyTradeData trade = this.getTrade();
        if(this.hasPermission(computer) && trade.getTradeDirection() != direction)
        {
            trade.setTradeDirection(direction);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public int getEnergy() throws LuaException { return this.getTrade().getAmount(); }
    public boolean setEnergy(IComputerAccess computer, IArguments args) throws LuaException
    {
        int newAmount = args.getInt(0);
        if(newAmount <= 0)
            throw new LuaException("New amount must be > 0");
        EnergyTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.setAmount(newAmount);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("setDirection").withContext(this::setDirection));
        registration.register(LCPeripheralMethod.builder("getEnergy").simple(this::getEnergy));
        registration.register(LCPeripheralMethod.builder("setEnergy").withContext(this::setEnergy));
    }
}