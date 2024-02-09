package io.github.lightman314.lctech.common.traders.terminal.traderSearching;

import java.util.List;

import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidTraderSearchFilter implements ITraderSearchFilter {

	@Override
	public boolean filter(@Nonnull TraderData data, @Nonnull String searchString) {
		
		//Search the fluids being sold
		if(data instanceof FluidTraderData ft)
		{
			List<FluidTradeData> trades = ft.getTradeData();
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
