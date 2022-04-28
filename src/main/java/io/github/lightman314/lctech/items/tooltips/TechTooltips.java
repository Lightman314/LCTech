package io.github.lightman314.lctech.items.tooltips;

import java.util.List;

import io.github.lightman314.lightmanscurrency.items.TooltipItem;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.NonNullSupplier;

public class TechTooltips {

	public static final NonNullSupplier<List<Component>> FLUID_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lctech.trader.fluid");
	public static final NonNullSupplier<List<Component>> FLUID_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lctech.interface.fluid");
	public static final NonNullSupplier<List<Component>> ENERGY_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lctech.trader.energy");
	public static final NonNullSupplier<List<Component>> ENERGY_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lctech.interface.energy");
	
}
