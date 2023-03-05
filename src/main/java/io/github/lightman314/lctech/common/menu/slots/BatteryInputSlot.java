package io.github.lightman314.lctech.common.menu.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BatteryInputSlot extends SimpleSlot{

	public static final ResourceLocation EMPTY_BATTERY_SLOT = new ResourceLocation(LCTech.MODID, "item/empty_battery_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_BATTERY_SLOT);
	
	public boolean locked = false;
	
	public BatteryInputSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack)
	{
		if(locked)
			return false;
		return EnergyUtil.getEnergyHandler(stack).isPresent();
	}
	
	@Override
	public int getMaxStackSize() { return 1; }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() {
		return BACKGROUND;
	}
	
}
