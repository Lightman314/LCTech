package io.github.lightman314.lctech.util;

import java.text.DecimalFormat;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fluids.FluidStack;

public class FluidFormatUtil {

	public static String formatFluidAmount(int amount)
	{
		return new DecimalFormat().format(amount);
	}
	
	public static Component getFluidName(FluidStack fluid)
	{
		return getFluidName(fluid, null);
	}
	
	public static Component getFluidName(FluidStack fluid, @Nullable ChatFormatting colorOverride)
	{
		MutableComponent component = new TranslatableComponent(fluid.getTranslationKey()).withStyle(fluid.getFluid().getAttributes().getRarity(fluid).color);
		if(colorOverride != null && fluid.getFluid().getAttributes().getRarity(fluid) == Rarity.COMMON)
			component.withStyle(colorOverride);
		return component;
	}
	
	
	
}
