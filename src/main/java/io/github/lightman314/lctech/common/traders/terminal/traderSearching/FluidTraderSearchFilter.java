package io.github.lightman314.lctech.common.traders.terminal.traderSearching;

import java.util.List;

import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import net.minecraftforge.fluids.FluidStack;

public class FluidTraderSearchFilter extends TraderSearchFilter{

	@Override
	public boolean filter(TraderData data, String searchString) {
		
		//Search the fluids being sold
		if(data instanceof FluidTraderData)
		{
			List<FluidTradeData> trades = ((FluidTraderData)data).getAllTrades();
			for (FluidTradeData trade : trades) {
				if (trade.isValid()) {
					FluidStack sellFluid = trade.getProduct();
					//Search fluid name
					if (!sellFluid.isEmpty() && FluidFormatUtil.getFluidName(sellFluid).getString().toLowerCase().contains(searchString))
						return true;
				}
			}
		}
		return false;
	}
	
}