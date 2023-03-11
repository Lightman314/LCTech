package io.github.lightman314.lctech.common.items.tooltips;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.NonNullSupplier;

public class TechTooltips {

	public static final NonNullSupplier<List<ITextComponent>> FLUID_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lctech.trader.fluid");
	public static final NonNullSupplier<List<ITextComponent>> FLUID_NETWORK_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lctech.trader.network.fluid");
	public static final NonNullSupplier<List<ITextComponent>> FLUID_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lctech.interface.fluid");
	public static final NonNullSupplier<List<ITextComponent>> ENERGY_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lctech.trader.energy");
	public static final NonNullSupplier<List<ITextComponent>> ENERGY_NETWORK_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lctech.trader.network.energy");
	public static final NonNullSupplier<List<ITextComponent>> ENERGY_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lctech.interface.energy");
	
}
