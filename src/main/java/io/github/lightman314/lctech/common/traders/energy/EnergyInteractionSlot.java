package io.github.lightman314.lctech.common.traders.energy;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.common.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EnergyInteractionSlot extends InteractionSlotData {

	public static EnergyInteractionSlot INSTANCE = new EnergyInteractionSlot();
	
	private EnergyInteractionSlot() { super(InteractionSlotData.ENERGY_TYPE); }
	
	@Override
	public boolean allowItemInSlot(ItemStack stack) { return EnergyUtil.getEnergyHandler(stack).isPresent(); }

	@Override
	@Nullable
	public Pair<ResourceLocation, ResourceLocation> emptySlotBG() { return BatteryInputSlot.BACKGROUND; }
	
}
