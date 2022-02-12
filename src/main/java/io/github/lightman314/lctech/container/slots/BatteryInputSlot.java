package io.github.lightman314.lctech.container.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.util.EnergyUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BatteryInputSlot extends Slot{

	public static final ResourceLocation EMPTY_BATTERY_SLOT = new ResourceLocation(LCTech.MODID, "items/empty_battery_slot");
	
	public BatteryInputSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return EnergyUtil.getEnergyHandler(stack).isPresent();
	}
	
	@Override
	public int getSlotStackLimit() { return 1; }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getBackground() {
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, EMPTY_BATTERY_SLOT);
	}
	
}
