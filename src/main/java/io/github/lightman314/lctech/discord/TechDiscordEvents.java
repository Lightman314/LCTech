package io.github.lightman314.lctech.discord;

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.discord.events.DiscordTraderSearchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TechDiscordEvents {

	@SubscribeEvent
	public static void onTraderSearch(DiscordTraderSearchEvent event) {
		UniversalTraderData trader = event.getTrader();
		if(trader instanceof UniversalFluidTraderData)
		{
			UniversalFluidTraderData fluidTrader = (UniversalFluidTraderData)trader;
			if(event.acceptTrader(fluidTrader))
			{
				boolean showStock = !fluidTrader.isCreative();
				boolean firstTrade = true;
				for(int i = 0; i < fluidTrader.getTradeCount(); ++i)
				{
					FluidTradeData trade = fluidTrader.getTrade(i);
					if(trade.isValid() && event.acceptTradeType(trade))
					{
						if(trade.isSale())
						{
							
							FluidStack sellFluid = trade.getProduct();
							String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
							
							//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
							if(!event.filterByTrades() || event.getSearchText().isEmpty() || fluidName.toLowerCase().contains(event.getSearchText()))
							{
								if(firstTrade)
								{
									event.addToOutput("--" + fluidTrader.getCoreSettings().getOwnerName() + "'s **" + fluidTrader.getName().getString() + "**--");
									firstTrade = false;
								}
								//Passed the search
								String priceText = trade.getCost().getString();
								event.addToOutput("Selling " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " for " + priceText);
								if(showStock)
									event.addToOutput("*" + trade.getStock(fluidTrader) + " trades in stock.*");
							}
							
						}
						else if(trade.isPurchase())
						{
							FluidStack sellFluid = trade.getProduct();
							String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
							
							if(!event.filterByTrades() || event.getSearchText().isEmpty() || fluidName.toLowerCase().contains(event.getSearchText()))
							{
								if(firstTrade)
								{
									event.addToOutput("--" + fluidTrader.getCoreSettings().getOwnerName() + "'s **" + fluidTrader.getName().getString() + "**--");
									firstTrade = false;
								}
								String priceText = trade.getCost().getString();
								event.addToOutput("Purchasing " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " for " + priceText);
								if(showStock)
									event.addToOutput("*" + trade.getStock(fluidTrader) + " trades in stock.*");
							}
						}
					}
				}
			}
		}
		else if(trader instanceof UniversalEnergyTraderData)
		{
			UniversalEnergyTraderData energyTrader = (UniversalEnergyTraderData)trader;
			if(event.acceptTrader(energyTrader))
			{
				boolean showStock = !energyTrader.isCreative();
				boolean firstTrade = true;
				for(int i = 0; i < energyTrader.getTradeCount(); ++i)
				{
					EnergyTradeData trade = energyTrader.getTrade(i);
					if(trade.isValid() && event.acceptTradeType(trade))
					{
						if(!event.filterByTrades() || (event.getSearchText().isEmpty()|| EnergyUtil.ENERGY_UNIT.toLowerCase().contains(event.getSearchText()) || "Energy".toLowerCase().contains(event.getSearchText())))
						{
							if(firstTrade)
							{
								event.addToOutput("--" + energyTrader.getCoreSettings().getOwnerName() + "'s **" + energyTrader.getName().getString() + "**--");
								firstTrade = false;
							}
							if(trade.isSale())
							{
								String priceText = trade.getCost().getString();
								event.addToOutput("Selling " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " for " + priceText);
								if(showStock)
									event.addToOutput("*" + trade.getStock(energyTrader) + " trades in stock.*");
							}
							else if(trade.isPurchase())
							{
								String priceText = trade.getCost().getString();
								event.addToOutput("Purchasing " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " for " + priceText);
								if(showStock)
									event.addToOutput("*" + trade.getStock(energyTrader) + " trades in stock.*");
							}
						}
					}
				}
			}
		}
	}
	
}
