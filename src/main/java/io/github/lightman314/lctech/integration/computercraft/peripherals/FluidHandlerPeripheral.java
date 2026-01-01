package io.github.lightman314.lctech.integration.computercraft.peripherals;

import dan200.computercraft.api.detail.ForgeDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.generic.GenericPeripheral;
import dan200.computercraft.shared.util.ArgumentHelpers;
import dan200.computercraft.shared.util.CapabilityUtil;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidHandlerPeripheral extends LCPeripheral {

    private final Supplier<Boolean> hasAccess;
    private final Supplier<IFluidHandler> handler;
    public FluidHandlerPeripheral(Supplier<Boolean> hasAccess,IFluidHandler handler) { this(hasAccess,() -> handler); }
    public FluidHandlerPeripheral(Supplier<Boolean> hasAccess,Supplier<IFluidHandler> handler) {
        this.hasAccess = hasAccess;
        this.handler = handler;
    }
    private boolean hasHandler() { return this.handler.get() != null; }
    private IFluidHandler getHandler()
    {
        IFluidHandler handler = this.handler.get();
        if(handler == null)
            return new EmptyHandler();
        return handler;
    }

    //Peripheral Methods
    public Map<Integer,Map<String,?>> tanks()
    {
        Map<Integer,Map<String,?>> result = new HashMap<>();
        IFluidHandler fluids = this.getHandler();
        int size = fluids.getTanks();
        for(int i = 0; i < size; ++i)
        {
            FluidStack stack = fluids.getFluidInTank(i);
            if(!stack.isEmpty())
                result.put(i + 1, ForgeDetailRegistries.FLUID_STACK.getBasicDetails(stack));
        }
        return result;
    }

    public int pushFluid(IComputerAccess computer, IArguments args) throws LuaException
    {
        if(!this.hasAccess.get())
            return 0;
        //Parse Arguments
        String toName = args.getString(0);
        Optional<Integer> limit = args.optInt(1);
        Optional<String> fluidName = args.optString(2);
        Fluid fluid = fluidName.isPresent() ? ArgumentHelpers.getRegistryEntry(fluidName.get(),"fluid", BuiltInRegistries.FLUID) : null;
        //Run Code
        IFluidHandler from = this.getHandler();
        IPeripheral location = computer.getAvailablePeripheral(toName);
        if(location == null)
            throw new LuaException("Target '" + toName + "' does not exist");
        else
        {
            IFluidHandler to = extractHandler(location);
            if(to == null)
                throw new LuaException("Target '" + toName + "' is not an tank");
            else
            {
                int actualLimit = limit.orElse(Integer.MAX_VALUE);
                if(actualLimit <= 0)
                    throw new LuaException("Limit must be > 0");
                else
                    return fluid == null ? moveFluid(from, actualLimit,to) : moveFluid(from,new FluidStack(fluid,actualLimit),to);
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
        Optional<String> fluidName = args.optString(2);
        Fluid fluid = fluidName.isPresent() ? ArgumentHelpers.getRegistryEntry(fluidName.get(),"fluid", BuiltInRegistries.FLUID) : null;
        //Run code
        IFluidHandler to = this.getHandler();
        IPeripheral location = computer.getAvailablePeripheral(fromName);
        if(location == null)
            throw new LuaException("Target '" + fromName + "' does not exist");
        else
        {
            IFluidHandler from = extractHandler(location);
            if(from == null)
                throw new LuaException("Target '" + fromName + "' is not an tank");
            else
            {
                int actualLimit = limit.orElse(Integer.MAX_VALUE);
                if(actualLimit <= 0)
                    throw new LuaException("Limit must be > 0");
                else
                    return fluid == null ? moveFluid(from, actualLimit, to) : moveFluid(from,new FluidStack(fluid,actualLimit),to);
            }
        }
    }

    @Override
    public String getType() { return "fluid_storage"; }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) {
        if(peripheral instanceof FluidHandlerPeripheral other)
            return this.hasHandler() && other.hasHandler() && Objects.equals(this.getHandler(),other.getHandler());
        return false;
    }

    private static class EmptyHandler implements IFluidHandler
    {
        private EmptyHandler() {}
        @Override
        public int getTanks() { return 0; }
        @Override
        public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
        @Override
        public int getTankCapacity(int tank) { return 0; }
        @Override
        public boolean isFluidValid(int tank, FluidStack stack) { return false; }
        @Override
        public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }

    public static @Nullable IFluidHandler extractHandler(IPeripheral peripheral) {
        Object object = peripheral.getTarget();
        Direction var10000;
        if (peripheral instanceof GenericPeripheral sided) {
            var10000 = sided.side();
        } else {
            var10000 = null;
        }

        Direction direction = var10000;
        if (object instanceof BlockEntity blockEntity) {
            if (blockEntity.isRemoved()) {
                return null;
            }

            Level level = blockEntity.getLevel();
            if (!(level instanceof ServerLevel serverLevel)) {
                return null;
            }

            IFluidHandler result = CapabilityUtil.getCapability(serverLevel, Capabilities.FluidHandler.BLOCK, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, direction);
            if (result != null) {
                return result;
            }
        }

        if (object instanceof IFluidHandler handler) {
            return handler;
        } else {
            return null;
        }
    }

    public static int moveFluid(IFluidHandler from, int limit, IFluidHandler to) {
        return moveFluid(from, from.drain(limit, IFluidHandler.FluidAction.SIMULATE), limit, to);
    }

    public static int moveFluid(IFluidHandler from, FluidStack fluid, IFluidHandler to) {
        return moveFluid(from, from.drain(fluid, IFluidHandler.FluidAction.SIMULATE), fluid.getAmount(), to);
    }

    public static int moveFluid(IFluidHandler from, FluidStack extracted, int limit, IFluidHandler to) {
        if (extracted.getAmount() <= 0) {
            return 0;
        } else {
            extracted = extracted.copy();
            extracted.setAmount(Math.min(extracted.getAmount(), limit));
            int inserted = to.fill(extracted.copy(), IFluidHandler.FluidAction.EXECUTE);
            if (inserted <= 0) {
                return 0;
            } else {
                extracted.setAmount(inserted);
                from.drain(extracted, IFluidHandler.FluidAction.EXECUTE);
                return inserted;
            }
        }
    }

    @Override
    protected void registerMethods(LCPeripheralMethod.Registration registration) {
        registration.register(LCPeripheralMethod.builder("tanks").simple(this::tanks));
        registration.register(LCPeripheralMethod.builder("pushFluid").withContext(this::pushFluid));
        registration.register(LCPeripheralMethod.builder("pullFluid").withContext(this::pullFluid));
    }

}
