package io.github.lightman314.lctech.integration.computercraft.peripherals.fluid;

import dan200.computercraft.api.detail.ForgeDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.util.ArgumentHelpers;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCArgumentHelper;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TradeWrapper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidTradeWrapper extends TradeWrapper<FluidTradeData> {

    public FluidTradeWrapper(Supplier<FluidTradeData> tradeSource, Supplier<TraderData> trader) { super(tradeSource, trader); }

    @Override
    public String getType() { return "lct_trade_fluid"; }

    public boolean setDirection(IComputerAccess computer, IArguments args) throws LuaException
    {
        TradeDirection direction = LCArgumentHelper.parseEnum(args,0,TradeDirection.class);
        if(direction != TradeDirection.SALE && direction != TradeDirection.PURCHASE)
            throw LuaValues.badArgumentOf(args,0,"tradedirection");
        FluidTradeData trade = this.getTrade();
        if(this.hasPermission(computer) && trade.getTradeDirection() != direction)
        {
            trade.setTradeDirection(direction);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public LCLuaTable getFluid() throws LuaException
    {
        FluidTradeData trade = this.getTrade();
        return new LCLuaTable(ForgeDetailRegistries.FLUID_STACK.getBasicDetails(trade.getProduct()));
    }

    public LCLuaTable getFluidDetails() throws LuaException
    {
        FluidTradeData trade = this.getTrade();
        return new LCLuaTable(ForgeDetailRegistries.FLUID_STACK.getDetails(trade.getProduct()));
    }

    public boolean setFluid(IComputerAccess computer, IArguments args) throws LuaException
    {
        FluidStack newFluid = LCArgumentHelper.parseFluid(args,0);
        FluidTradeData trade = this.getTrade();
        if(this.hasPermission(computer))
        {
            trade.setProduct(newFluid);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    public int getFluidQuantity() throws LuaException { return this.getTrade().getQuantity(); }
    public int getFluidQuantityBuckets() throws LuaException { return this.getTrade().getBucketQuantity(); }

    public boolean setFluidQuantityBuckets(IComputerAccess computer, IArguments args) throws LuaException
    {
        int quantity = args.getInt(0);
        ArgumentHelpers.assertBetween(quantity,1,FluidTradeData.getMaxBucketQuantity(),"Fluid bucket quantity out of range (%s)");
        FluidTradeData trade = this.getTrade();
        if(this.hasPermission(computer) && trade.getBucketQuantity() != quantity)
        {
            trade.setBucketQuantity(quantity);
            this.markTradeDirty();
            return true;
        }
        return false;
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        super.registerMethods(registration);
        registration.register(LCPeripheralMethod.builder("setDirection").withContext(this::setDirection));
        registration.register(LCPeripheralMethod.builder("getFluid").simple(this::getFluid));
        registration.register(LCPeripheralMethod.builder("getFluidDetails").simple(this::getFluidDetails));
        registration.register(LCPeripheralMethod.builder("setFluid").withContext(this::setFluid));
        registration.register(LCPeripheralMethod.builder("getFluidQuantity").simple(this::getFluidQuantity));
        registration.register(LCPeripheralMethod.builder("getFluidQuantityBuckets").simple(this::getFluidQuantityBuckets));
        registration.register(LCPeripheralMethod.builder("setFluidQuantityBuckets").withContext(this::setFluidQuantityBuckets));
    }
}
