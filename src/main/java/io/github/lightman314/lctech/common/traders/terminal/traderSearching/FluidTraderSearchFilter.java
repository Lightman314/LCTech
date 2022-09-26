package io.github.lightman314.lctech.common.traders.terminal.traderSearching;

import java.util.List;

import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.tradedata.fluid.FluidTradeData;
import io.github.lightman314.lctech.util.FluidFormatUtil;
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
			for(int i = 0; i < trades.size(); ++i)
			{
				if(trades.get(i).isValid())
				{
					FluidStack sellFluid = trades.get(i).getProduct();
					//Search fluid name
					if(!sellFluid.isEmpty() && FluidFormatUtil.getFluidName(sellFluid).getString().toLowerCase().contains(searchString))
						return true;
				}
			}
		}
		return false;
	}
	
}
