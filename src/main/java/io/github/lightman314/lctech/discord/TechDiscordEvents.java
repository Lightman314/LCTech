package io.github.lightman314.lctech.discord;

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.discord.CurrencyMessages;
import io.github.lightman314.lightmanscurrency.discord.events.DiscordPostTradeEvent;
import io.github.lightman314.lightmanscurrency.discord.events.DiscordTraderSearchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TechDiscordEvents {

	@SubscribeEvent
	public static void onTraderSearch(DiscordTraderSearchEvent event) {
		UniversalTraderData trader = event.getTrader();
		boolean listTrader = (event.findOwners() && (event.getSearchText().isEmpty() || trader.getCoreSettings().getOwnerName().toLowerCase().contains(event.getSearchText())))
				|| (event.findTraders() && (event.getSearchText().isEmpty() || trader.getName().getString().toLowerCase().contains(event.getSearchText())));
		if(trader instanceof UniversalFluidTraderData)
		{
			
			UniversalFluidTraderData fluidTrader = (UniversalFluidTraderData)trader;
			if(listTrader)
			{
				boolean firstTrade = true;
				for(int i = 0; i < fluidTrader.getTradeCount(); ++i)
				{
					FluidTradeData trade = fluidTrader.getTrade(i);
					if(trade.isValid())
					{
						if(firstTrade)
						{
							event.addToOutput("--" + fluidTrader.getCoreSettings().getOwnerName() + "'s **" + fluidTrader.getName().getString() + "**--");
							firstTrade = false;
						}
						if(trade.isSale())
						{
							FluidStack sellFluid = trade.getProduct();
							String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
							String priceText = trade.getCost().getString();
							event.addToOutput("Selling " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " for " + priceText);
						}
						else if(trade.isPurchase())
						{
							FluidStack sellFluid = trade.getProduct();
							String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
							String priceText = trade.getCost().getString();
							event.addToOutput("Purchasing " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " for " + priceText);
						}
					}
				}
			}
			else
			{
				for(int i = 0; i < fluidTrader.getTradeCount(); ++i)
				{
					FluidTradeData trade = fluidTrader.getTrade(i);
					if(trade.isValid())
					{
						if(trade.isSale() && event.findSales())
						{
							FluidStack sellFluid = trade.getProduct();
							String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
							
							//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
							if(event.getSearchText().isEmpty() || fluidName.toLowerCase().contains(event.getSearchText()))
							{
								//Passed the search
								String priceText = trade.getCost().getString();
								event.addToOutput(fluidTrader.getCoreSettings().getOwnerName() + " is selling " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " at " + fluidTrader.getName().getString() + " for " + priceText);
							}
						}
						else if(trade.isPurchase() && event.findPurchases())
						{
							FluidStack sellFluid = trade.getProduct();
							String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
							
							//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
							if(event.getSearchText().isEmpty() || fluidName.toLowerCase().contains(event.getSearchText()))
							{
								//Passed the search
								String priceText = trade.getCost().getString();
								event.addToOutput(fluidTrader.getCoreSettings().getOwnerName() + " is buying " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "x " + fluidName + " at " + fluidTrader.getName().getString() + " for " + priceText);
							}
						}
					}
				}
			}
			
		}
		else if(trader instanceof UniversalEnergyTraderData)
		{
			UniversalEnergyTraderData energyTrader = (UniversalEnergyTraderData)trader;
			if(listTrader)
			{
				boolean firstTrade = true;
				for(int i = 0; i < energyTrader.getTradeCount(); ++i)
				{
					EnergyTradeData trade = energyTrader.getTrade(i);
					if(trade.isValid())
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
						}
						else if(trade.isPurchase())
						{
							String priceText = trade.getCost().getString();
							event.addToOutput("Purchasing " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " for " + priceText);
						}
					}
				}
			}
			else
			{
				for(int i = 0; i < energyTrader.getTradeCount(); ++i)
				{
					EnergyTradeData trade = energyTrader.getTrade(i);
					//Energy Trades always have the same product name ("FE" or "ENERGY") so perform the search check before knowing the trade type
					if(trade.isValid() && (event.getSearchText().isEmpty()|| EnergyUtil.ENERGY_UNIT.toLowerCase().contains(event.getSearchText()) || "Energy".toLowerCase().contains(event.getSearchText())))
					{
						if(trade.isSale() && event.findSales())
						{
							String priceText = trade.getCost().getString();
							event.addToOutput(energyTrader.getCoreSettings().getOwnerName() + " is selling " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " at " + energyTrader.getName().getString() + " for " + priceText);
						}
						else if(trade.isPurchase() && event.findPurchases())
						{
							String priceText = trade.getCost().getString();
							event.addToOutput(energyTrader.getCoreSettings().getOwnerName() + " is buying " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " at " + energyTrader.getName().getString() + " for " + priceText);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onTradeCarriedOut(DiscordPostTradeEvent d) {
		if(d.event.getTrade() instanceof FluidTradeData)
		{
			FluidTradeData fluidTrade = (FluidTradeData)d.event.getTrade();
			StringBuffer message = new StringBuffer();
			//Customer name
			message.append(d.event.getPlayerReference().lastKnownName());
			//Action (bought, sold, ???)
			switch(fluidTrade.getTradeDirection())
			{
			case SALE: message.append(" bought "); break;
			case PURCHASE: message.append(" sold "); break;
				default: message.append(" ??? ");
			}
			//Item bought/sold
			FluidStack boughtFluid = fluidTrade.getProduct();
			String boughtFluidName = FluidFormatUtil.getFluidName(boughtFluid).getString();
			message.append(FluidFormatUtil.formatFluidAmount(fluidTrade.getQuantity())).append("mB of ").append(boughtFluidName);
			//Price
			message.append(" for ");
			if(d.event.getPricePaid().isFree() || d.event.getPricePaid().getRawValue() <= 0)
				message.append("free");
			else
				message.append(d.event.getPricePaid().getString());
			
			//From trader name
			message.append(" from your ").append(d.event.getTrader().getName().getString());
			
			//Send the message directly to the linked user
			//Create as pending message to avoid message spamming them when a player buys a ton of the same item
			d.addPendingMessage(message.toString());
			//MessageUtil.sendPrivateMessage(linkedUser, message.toString());
			
			//Check if out of stock
			if(d.event.getTrader() instanceof IFluidTrader)
			{
				if(fluidTrade.getStock((IFluidTrader)d.event.getTrader()) < 1)
				{
					d.addPendingMessage(CurrencyMessages.M_NOTIFICATION_OUTOFSTOCK.get());
				}
			}
		}
		else if(d.event.getTrade() instanceof EnergyTradeData)
		{
			EnergyTradeData energyTrade = (EnergyTradeData)d.event.getTrade();
			StringBuffer message = new StringBuffer();
			//Customer name
			message.append(d.event.getPlayerReference().lastKnownName());
			//Action (bought, sold, ???)
			switch(energyTrade.getTradeDirection())
			{
			case SALE: message.append(" bought "); break;
			case PURCHASE: message.append(" sold "); break;
				default: message.append(" ??? ");
			}
			//Item bought/sold
			message.append(EnergyUtil.formatEnergyAmount(energyTrade.getAmount()));
			//Price
			message.append(" for ");
			if(d.event.getPricePaid().isFree() || d.event.getPricePaid().getRawValue() <= 0)
				message.append("free");
			else
				message.append(d.event.getPricePaid().getString());
			
			//From trader name
			message.append(" from your ").append(d.event.getTrader().getName().getString());
			
			//Send the message directly to the linked user
			//Create as pending message to avoid message spamming them when a player buys a ton of the same item
			d.addPendingMessage(message.toString());
			//MessageUtil.sendPrivateMessage(linkedUser, message.toString());
			
			//Check if out of stock
			if(d.event.getTrader() instanceof IEnergyTrader)
			{
				if(energyTrade.getStock((IEnergyTrader)d.event.getTrader()) < 1)
				{
					d.addPendingMessage(CurrencyMessages.M_NOTIFICATION_OUTOFSTOCK.get());
				}
			}
		}
	}
	
}
