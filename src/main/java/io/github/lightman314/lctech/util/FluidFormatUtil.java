package io.github.lightman314.lctech.util;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fluids.FluidStack;

public class FluidFormatUtil {

	public static Component getFluidName(FluidStack fluid)
	{
		return getFluidName(fluid, null);
	}
	
	public static Component getFluidName(FluidStack fluid, @Nullable ChatFormatting colorOverride)
	{
		MutableComponent component = new TranslatableComponent(fluid.getTranslationKey()).withStyle(fluid.getFluid().getAttributes().getRarity(fluid).color);
		Rarity rarity = fluid.getFluid().getAttributes().getRarity(fluid);
		if(colorOverride != null && rarity == Rarity.COMMON)
			component.withStyle(colorOverride);
		else
			component.withStyle(rarity.color);
		return component;
	}
	
	
	
}
