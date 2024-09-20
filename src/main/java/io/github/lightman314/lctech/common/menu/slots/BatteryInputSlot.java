package io.github.lightman314.lctech.common.menu.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

public class BatteryInputSlot extends EasySlot {

	public static final ResourceLocation EMPTY_BATTERY_SLOT = ResourceLocation.fromNamespaceAndPath(LCTech.MODID, "item/empty_battery_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_BATTERY_SLOT);

	public boolean requireEnergy = false;

	public BatteryInputSlot(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return super.mayPlace(stack) && this.meetsRequirements(stack);
	}

	private boolean meetsRequirements(@Nonnull ItemStack stack) {
		IEnergyStorage storage = EnergyUtil.getEnergyHandler(stack).orElse(null);
		if(storage == null)
			return false;
		if(this.requireEnergy)
			return storage.getEnergyStored() > 0;
		return true;
	}
	
	@Override
	public int getMaxStackSize() { return 1; }
	
	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() { return BACKGROUND; }
	
}
