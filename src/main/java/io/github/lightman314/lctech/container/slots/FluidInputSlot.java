package io.github.lightman314.lctech.container.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidInputSlot extends Slot{

	public FluidInputSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return stack.getItem() instanceof BucketItem || FluidUtil.getFluidHandler(stack).isPresent();
	}
	
	@Override
	public int getSlotStackLimit() { return 1; }
	
}
