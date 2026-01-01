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

}
