package io.github.lightman314.lctech.common.traders.terminal.filters;

import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.IBasicTraderFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class FluidTraderSearchFilter implements IBasicTraderFilter {

	public static final String FLUID = "fluid";

	@Override
	public void filterTrade(TradeData data, PendingSearch search, HolderLookup.Provider lookup) {
		if(data instanceof FluidTradeData trade && trade.isValid())
			search.processFilter(FLUID,filterFluid(trade.getProduct()));
	}

	public static Predicate<String> filterFluid(FluidStack fluid)
	{
		return (input) -> {
			if(input.isBlank())
				return false;
			if(fluid.isEmpty())
				return "empty".contains(input);
			if(FluidFormatUtil.getFluidName(fluid).getString().toLowerCase().contains(input))
				return true;
			if(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString().toLowerCase().contains(input))
				return true;
			return false;
		};
	}

}
