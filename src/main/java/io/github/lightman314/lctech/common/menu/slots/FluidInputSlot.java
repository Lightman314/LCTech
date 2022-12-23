package io.github.lightman314.lctech.common.menu.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidInputSlot extends Slot{

	public static final ResourceLocation EMPTY_FLUID_SLOT = new ResourceLocation(LCTech.MODID, "item/empty_fluid_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_FLUID_SLOT);
	
	public FluidInputSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return stack.getItem() instanceof BucketItem || FluidUtil.getFluidHandler(stack).isPresent();
	}
	
	@Override
	public int getMaxStackSize() { return 1; }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return BACKGROUND;
	}
	
}
