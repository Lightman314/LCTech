package io.github.lightman314.lctech.common.blockentities;

import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoidTankBlockEntity extends EasyBlockEntity {

    public static final IFluidHandler VOID_HANDLER = new VoidFluidHandler();
    private static final LazyOptional<IFluidHandler> OPTIONAL = LazyOptional.of(() -> VOID_HANDLER);


    public VoidTankBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { super(ModBlockEntities.VOID_TANK.get(), pos, state); }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) { return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, OPTIONAL); }

    private static class VoidFluidHandler implements IFluidHandler
    {
        @Override
        public int getTanks() { return 1; }
        @Override
        @Nonnull
        public FluidStack getFluidInTank(int i) { return FluidStack.EMPTY; }
        @Override
        public int getTankCapacity(int i) { return Integer.MAX_VALUE / 2; }
        @Override
        public boolean isFluidValid(int i, @Nonnull FluidStack fluidStack) { return true; }
        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction) { return fluidStack.getAmount(); }
        @Override
        @Nonnull
        public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) { return FluidStack.EMPTY; }
        @Override
        @Nonnull
        public FluidStack drain(int i, FluidAction fluidAction) { return FluidStack.EMPTY; }
    }

}
