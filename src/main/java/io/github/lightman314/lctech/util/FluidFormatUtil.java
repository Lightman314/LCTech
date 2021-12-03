package io.github.lightman314.lctech.util;

import javax.annotation.Nullable;

import net.minecraft.item.Rarity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class FluidFormatUtil {

	public static ITextComponent getFluidName(FluidStack fluid)
	{
		return getFluidName(fluid, null);
	}
	
	public static ITextComponent getFluidName(FluidStack fluid, @Nullable TextFormatting colorOverride)
	{
		IFormattableTextComponent component = new TranslationTextComponent(fluid.getTranslationKey()).mergeStyle(fluid.getFluid().getAttributes().getRarity(fluid).color);
		Rarity rarity = fluid.getFluid().getAttributes().getRarity(fluid);
		if(colorOverride != null && rarity == Rarity.COMMON)
			component.mergeStyle(colorOverride);
		else
			component.mergeStyle(rarity.color);
		return component;
	}
	
	
	
}