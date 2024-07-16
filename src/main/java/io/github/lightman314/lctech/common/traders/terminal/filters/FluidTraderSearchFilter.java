package io.github.lightman314.lctech.common.traders.terminal.filters;

import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidTraderSearchFilter implements IBasicTraderFilter {

	@Override
	public boolean filterTrade(@Nonnull TradeData tradeData, @Nonnull String searchString, @Nonnull HolderLookup.Provider lookup) {
		if(tradeData instanceof FluidTradeData trade)
		{
			if(trade.isValid())
                return filterFluid(trade.getProduct(),searchString);
		}
		return false;
	}

	public static boolean filterFluid(@Nonnull FluidStack fluid, @Nonnull String searchString)
	{
		if(fluid.isEmpty())
			return false;
		if(FluidFormatUtil.getFluidName(fluid).getString().toLowerCase().contains(searchString))
			return true;
		if(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString().toLowerCase().contains(searchString))
			return true;
		return false;
	}

}
