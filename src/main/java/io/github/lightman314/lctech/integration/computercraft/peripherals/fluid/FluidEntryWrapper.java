package io.github.lightman314.lctech.integration.computercraft.peripherals.fluid;

import dan200.computercraft.api.detail.ForgeDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.integration.computercraft.peripherals.FluidHandlerPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.AccessTrackingPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.LCLuaTable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidEntryWrapper extends AccessTrackingPeripheral {

    private final Supplier<Boolean> hasAccess;
    private final Supplier<TraderFluidStorage> source;
    private final int index;
    public FluidEntryWrapper(Supplier<Boolean> hasAccess,Supplier<TraderFluidStorage> source,int index)
    {
        this.hasAccess = hasAccess;
        this.source = source;
        this.index = index;
    }

    protected TraderFluidStorage getStorage() throws LuaException
    {
        TraderFluidStorage storage = this.source.get();
        if(storage == null)
            throw new LuaException("An unexpected error occurred trying to get the fluid tanks data!");
        return storage;
    }

    protected FluidEntry getEntry() throws LuaException
    {
        TraderFluidStorage storage = this.getStorage();
        if(this.index >= 0 && this.index < storage.getTanks())
            return storage.getContents().get(this.index);
        throw new LuaException("An unexpected error occurred trying to get the fluid tanks data!");
    }

    @Override
    public String getType() { return "lct_fluid_tank"; }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) { return this == peripheral; }

    public LCLuaTable getTargetFluid() throws LuaException { return new LCLuaTable(ForgeDetailRegistries.FLUID_STACK.getBasicDetails(this.getEntry().filter)); }
    public LCLuaTable getTargetFluidDetails() throws LuaException { return new LCLuaTable(ForgeDetailRegistries.FLUID_STACK.getBasicDetails(this.getEntry().filter)); }
    public int getQuantity() throws LuaException { return this.getEntry().getStoredAmount(); }
    public int getCapacity() throws LuaException { return this.getEntry().getTankCapacity(0); }

    public int pushFluid(IComputerAccess computer, IArguments args) throws LuaException
    {
        if(!this.hasAccess.get())
            return 0;
        //Parse Arguments
        String toName = args.getString(0);
        Optional<Integer> limit = args.optInt(1);
        FluidStack fluid = this.getEntry().filter.copy();
        if(fluid.isEmpty())
            return 0;
        //Run code
        IFluidHandler from = this.getStorage();
        IPeripheral location = computer.getAvailablePeripheral(toName);
        if(location == null)
            throw new LuaException("Target '" + toName + "' does not exist");
        else
        {
            IFluidHandler to = FluidHandlerPeripheral.extractHandler(location);
            if(to == null)
                throw new LuaException("Target '" + toName + "' is not an tank");
            else
            {
                int actualLimit = limit.orElse(Integer.MAX_VALUE);
                if(actualLimit <= 0)
                    throw new LuaException("Limit must be > 0");
                else
                {
                    fluid.setAmount(actualLimit);
                    return FluidHandlerPeripheral.moveFluid(from,fluid,to);
                }
            }
        }
    }

    public int pullFluid(IComputerAccess computer, IArguments args) throws LuaException
    {
        if(!this.hasAccess.get())
            return 0;
        //Parse Arguments
        String fromName = args.getString(0);
        Optional<Integer> limit = args.optInt(1);
        FluidStack fluid = this.getEntry().filter.copy();
        if(fluid.isEmpty())
            return 0;
        //Run code
        IFluidHandler to = this.getStorage();
        IPeripheral location = computer.getAvailablePeripheral(fromName);
        if(location == null)
            throw new LuaException("Target '" + fromName + "' does not exist");
        else
        {
            IFluidHandler from = FluidHandlerPeripheral.extractHandler(location);
            if(from == null)
                throw new LuaException("Target '" + fromName + "' is not an tank");
            else
            {
                int actualLimit = limit.orElse(Integer.MAX_VALUE);
                if(actualLimit <= 0)
                    throw new LuaException("Limit must be > 0");
                else
                {
                    fluid.setAmount(actualLimit);
                    return FluidHandlerPeripheral.moveFluid(from,fluid,to);
                }
            }
        }
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("getTargetFluid").simple(this::getTargetFluid));
        registration.register(LCPeripheralMethod.builder("getTargetFluidDetails").simple(this::getTargetFluidDetails));
        registration.register(LCPeripheralMethod.builder("getQuantity").simple(this::getQuantity));
        registration.register(LCPeripheralMethod.builder("getCapacity").simple(this::getCapacity));
        registration.register(LCPeripheralMethod.builder("pushFluid").withContext(this::pushFluid));
        registration.register(LCPeripheralMethod.builder("pullFluid").withContext(this::pullFluid));

    }

}