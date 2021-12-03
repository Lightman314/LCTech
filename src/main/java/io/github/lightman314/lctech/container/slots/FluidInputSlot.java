package io.github.lightman314.lctech.container.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidUtil;

public class FluidInputSlot extends Slot{

	public static final ResourceLocation EMPTY_FLUID_SLOT = new ResourceLocation(LCTech.MODID, "items/empty_fluid_slot");
	
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
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getBackground() {
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EMPTY_FLUID_SLOT);
	}
	
}
