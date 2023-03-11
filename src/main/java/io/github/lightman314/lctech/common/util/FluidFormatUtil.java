package io.github.lightman314.lctech.common.util;

import java.text.DecimalFormat;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

public class FluidFormatUtil {

	public static String formatFluidAmount(int amount)
	{
		return new DecimalFormat().format(amount);
	}
	
	public static IFormattableTextComponent getFluidName(FluidStack fluid)
	{
		return getFluidName(fluid, null);
	}
	
	public static IFormattableTextComponent getFluidName(FluidStack fluid, @Nullable TextFormatting colorOverride)
	{
		IFormattableTextComponent component = EasyText.translatable(fluid.getTranslationKey()).withStyle(fluid.getFluid().getAttributes().getRarity(fluid).color);
		if(colorOverride != null && fluid.getFluid().getAttributes().getRarity(fluid) == Rarity.COMMON)
			component.withStyle(colorOverride);
		return component;
	}
	
}
