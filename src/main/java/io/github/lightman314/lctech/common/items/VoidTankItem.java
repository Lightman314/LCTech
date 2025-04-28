package io.github.lightman314.lctech.common.items;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VoidTankItem extends BlockItem {

    public VoidTankItem(Block block, Properties properties) {
        super(block,properties);
    }

    public static IFluidHandlerItem getVoidHandler(ItemStack stack) { return new VoidFluidHandler(stack); }

    private record VoidFluidHandler(ItemStack item) implements IFluidHandlerItem
    {
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
        public ItemStack getContainer() { return this.item; }
    }

}
