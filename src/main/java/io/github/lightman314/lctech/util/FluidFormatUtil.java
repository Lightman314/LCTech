package io.github.lightman314.lctech.util;

import java.text.DecimalFormat;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fluids.FluidStack;

public class FluidFormatUtil {

	public static String formatFluidAmount(int amount)
	{
		return new DecimalFormat().format(amount);
	}
	
	public static MutableComponent getFluidName(FluidStack fluid)
	{
		return getFluidName(fluid, null);
	}
	
	public static MutableComponent getFluidName(FluidStack fluid, @Nullable ChatFormatting colorOverride)
	{
		MutableComponent component = Component.translatable(fluid.getTranslationKey()).withStyle(fluid.getFluid().getFluidType().getRarity(fluid).getStyleModifier());
		if(colorOverride != null && fluid.getFluid().getFluidType().getRarity(fluid) == Rarity.COMMON)
			component.withStyle(colorOverride);
		return component;
	}
	
}
