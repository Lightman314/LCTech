package io.github.lightman314.lctech.common.menu.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatteryInputSlot extends SimpleSlot {

	public static final ResourceLocation EMPTY_BATTERY_SLOT = new ResourceLocation(LCTech.MODID, "item/empty_battery_slot");
	public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_BATTERY_SLOT);

	public boolean requireEnergy = false;

	public BatteryInputSlot(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return super.mayPlace(stack) && this.meetsRequirements(stack);
	}

	private boolean meetsRequirements(@Nonnull ItemStack stack) {
		LazyOptional<IEnergyStorage> optional = EnergyUtil.getEnergyHandler(stack);
		if(!optional.isPresent())
			return false;
		AtomicBoolean passes = new AtomicBoolean(true);
		if(this.requireEnergy)
			optional.ifPresent(storage -> passes.set(storage.getEnergyStored() > 0));
		return passes.get();
	}

	@Override
	public int getMaxStackSize() { return 1; }

	@Override
	public Pair<ResourceLocation,ResourceLocation> getNoItemIcon() { return BACKGROUND; }

}