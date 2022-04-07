package io.github.lightman314.lctech.trader.fluid;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.menu.slots.FluidInputSlot;
import io.github.lightman314.lightmanscurrency.trader.common.InteractionSlotData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidInteractionSlot extends InteractionSlotData{

	public static FluidInteractionSlot INSTANCE = new FluidInteractionSlot();
	
	private FluidInteractionSlot() { super(InteractionSlotData.FLUID_TYPE); }

	@Override
	public boolean allowItemInSlot(ItemStack stack) { return stack.getItem() instanceof BucketItem || FluidUtil.getFluidHandler(stack).isPresent(); }
	
	@Override
	@Nullable
	public Pair<ResourceLocation,ResourceLocation> emptySlotBG() { return FluidInputSlot.BACKGROUND; }
	
}
