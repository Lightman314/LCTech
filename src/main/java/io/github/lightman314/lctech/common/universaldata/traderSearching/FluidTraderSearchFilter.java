package io.github.lightman314.lctech.common.universaldata.traderSearching;

import java.util.List;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;
import net.minecraftforge.fluids.FluidStack;

public class FluidTraderSearchFilter extends TraderSearchFilter{

	@Override
	public boolean filter(UniversalTraderData data, String searchString) {
		
		//Search the fluids being sold
		if(data instanceof UniversalFluidTraderData)
		{
			List<FluidTradeData> trades = ((UniversalFluidTraderData)data).getAllTrades();
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
