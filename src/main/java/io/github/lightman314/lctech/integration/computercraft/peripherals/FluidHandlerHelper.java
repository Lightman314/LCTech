package io.github.lightman314.lctech.integration.computercraft.peripherals;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.generic.GenericPeripheral;
import dan200.computercraft.shared.util.CapabilityUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidHandlerHelper {

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

            LazyOptional<IFluidHandler> result = CapabilityUtil.getCapability(blockEntity, ForgeCapabilities.FLUID_HANDLER, direction);
            if (result.isPresent()) {
                return result.orElseThrow(IllegalStateException::new);
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

}