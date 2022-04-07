package io.github.lightman314.lctech.trader.energy;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.trader.common.InteractionSlotData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EnergyInteractionSlot extends InteractionSlotData {

	public static EnergyInteractionSlot INSTANCE = new EnergyInteractionSlot();
	
	private EnergyInteractionSlot() { super(InteractionSlotData.ENERGY_TYPE); }
	
	@Override
	public boolean allowItemInSlot(ItemStack stack) { return EnergyUtil.getEnergyHandler(stack).isPresent(); }

	@Override
	@Nullable
	public Pair<ResourceLocation,ResourceLocation> emptySlotBG() { return BatteryInputSlot.BACKGROUND; }
	
}
