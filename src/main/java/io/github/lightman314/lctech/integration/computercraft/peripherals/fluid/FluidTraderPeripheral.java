package io.github.lightman314.lctech.integration.computercraft.peripherals.fluid;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.InputTraderPeripheral;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidTraderPeripheral extends InputTraderPeripheral<FluidTraderBlockEntity,FluidTraderData> {

    public FluidTraderPeripheral(FluidTraderBlockEntity fluidTraderBlockEntity) { super(fluidTraderBlockEntity); }
    public FluidTraderPeripheral(FluidTraderData trader) { super(trader); }

    @Override
    public String getType() { return "lct_trader_fluid"; }

    public int getTankCapacity() throws LuaException { return this.getTrader().getTankCapacity(); }

    public int getTankCount() throws LuaException { return this.getTrader().getStorage().getTanks(); }
    public Object getTank(IComputerAccess computer, IArguments args) throws LuaException
    {
        int slot = args.getInt(0);
        TraderFluidStorage storage = this.getTrader().getStorage();
        ArgumentHelpers.assertBetween(slot,1,storage.getTanks(),"Tank Slot is out of bounds (%s)");
        FluidEntryWrapper wrapper = new FluidEntryWrapper(() -> this.hasPermissions(computer,Permissions.OPEN_STORAGE),this::safeGetStorage,slot - 1,this::safeGetTrader);
        wrapper.setParent(this);
        return wrapper.asTable(computer);
    }

    private TraderFluidStorage safeGetStorage()
    {
        FluidTraderData trader = this.safeGetTrader();
        if(trader != null)
            return trader.getStorage();
        return null;
    }

    private Supplier<FluidTradeData> tradeSource(int index) {
        return () -> {
            FluidTraderData trader = this.safeGetTrader();
            if(trader != null && index >= 0 && index < trader.getTradeCount())
                return trader.getTrade(index);
            return null;
        };
    }

    @Override
    protected AccessTrackingPeripheral wrapTrade(TradeData trade) throws LuaException {
        int index = this.getTrader().indexOfTrade(trade);
        FluidTradeWrapper wrapper = new FluidTradeWrapper(this.tradeSource(index),this::safeGetTrader);
        wrapper.setParent(this);
        return wrapper;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("getTankCapacity").simple(this::getTankCapacity));
        registration.register(LCPeripheralMethod.builder("getTankCount").simple(this::getTankCount));
        registration.register(LCPeripheralMethod.builder("getTank").withContext(this::getTank));
        this.registerGetTrade(registration);
    }
}