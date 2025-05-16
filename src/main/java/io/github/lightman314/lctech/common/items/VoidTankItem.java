package io.github.lightman314.lctech.common.items;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VoidTankItem extends BlockItem {

    public VoidTankItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) { return new VoidFluidHandler(stack); }

    private static class VoidFluidHandler implements IFluidHandlerItem, ICapabilityProvider
    {

        final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
        final ItemStack item;
        VoidFluidHandler(ItemStack item) { this.item = item; }

        @Override
        public ItemStack getContainer() { return item; }
        @Override
        public int getTanks() { return 1; }
        @Override
        public FluidStack getFluidInTank(int i) { return FluidStack.EMPTY; }
        @Override
        public int getTankCapacity(int i) { return Integer.MAX_VALUE / 2; }
        @Override
        public boolean isFluidValid(int i, FluidStack fluidStack) { return true; }
        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction) { return fluidStack.getAmount(); }
        @Override
        public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) { return FluidStack.EMPTY; }
        @Override
        public FluidStack drain(int i, FluidAction fluidAction) { return FluidStack.EMPTY; }
        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability,this.holder);
        }
    }

}
