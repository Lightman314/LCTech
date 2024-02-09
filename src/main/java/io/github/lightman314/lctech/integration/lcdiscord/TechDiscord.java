package io.github.lightman314.lctech.integration.lcdiscord;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.integration.discord.CurrencyMessages;
import io.github.lightman314.lightmanscurrency.integration.discord.events.DiscordTraderSearchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class TechDiscord {

    public static void setup()
    {
        MinecraftForge.EVENT_BUS.register(TechDiscord.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(TechMessages.class);
    }

    @SubscribeEvent
    public static void onTraderSearch(DiscordTraderSearchEvent event) {
        TraderData trader = event.getTrader();
        String searchText = event.getSearchText();
        if(trader instanceof FluidTraderData fluidTrader)
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
                        if(searchText.isEmpty() || fluidName.toLowerCase().contains(searchText))
                        {
                            if(firstTrade)
                            {
                                event.addToOutput(CurrencyMessages.M_SEARCH_TRADER_NAME.format(trader.getOwner().getOwnerName(false), trader.getName()));
                                firstTrade = false;
                            }
                            //Passed the search
                            String priceText = trade.getCost().getString();
                            event.addToOutput(TechMessages.M_SEARCH_TRADE_FLUID_SALE.format(FluidFormatUtil.formatFluidAmount(trade.getQuantity()), fluidName, priceText));
                            if(showStock)
                                event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_STOCK.format(trade.getStock(fluidTrader)));
                        }

                    }
                    else if(trade.isPurchase())
                    {
                        FluidStack sellFluid = trade.getProduct();
                        String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();

                        if(searchText.isEmpty() || fluidName.toLowerCase().contains(searchText))
                        {
                            if(firstTrade)
                            {
                                event.addToOutput(CurrencyMessages.M_SEARCH_TRADER_NAME.format(trader.getOwner().getOwnerName(false), trader.getName()));
                                firstTrade = false;
                            }
                            String priceText = trade.getCost().getString();
                            event.addToOutput(TechMessages.M_SEARCH_TRADE_FLUID_PURCHASE.format(FluidFormatUtil.formatFluidAmount(trade.getQuantity()), fluidName, priceText));
                            if(showStock)
                                event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_STOCK.format(trade.getStock(fluidTrader)));
                        }
                    }
                }
            }
        }
        else if(trader instanceof EnergyTraderData energyTrader)
        {
            boolean showStock = !energyTrader.isCreative();
            boolean firstTrade = true;
            for(int i = 0; i < energyTrader.getTradeCount(); ++i)
            {
                EnergyTradeData trade = energyTrader.getTrade(i);
                if(trade.isValid() && event.acceptTradeType(trade))
                {
                    if(EnergyUtil.ENERGY_UNIT.toLowerCase().contains(searchText) || "Energy".toLowerCase().contains(searchText))
                    {
                        if(firstTrade)
                        {
                            event.addToOutput(CurrencyMessages.M_SEARCH_TRADER_NAME.format(trader.getOwner().getOwnerName(false), trader.getName()));
                            firstTrade = false;
                        }
                        if(trade.isSale())
                        {
                            String priceText = trade.getCost().getString();
                            event.addToOutput(TechMessages.M_SEARCH_TRADE_ENERGY_SALE.format(EnergyUtil.formatEnergyAmount(trade.getAmount(), false), priceText));
                            if(showStock)
                                event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_STOCK.format(trade.getStock(energyTrader)));
                        }
                        else if(trade.isPurchase())
                        {
                            String priceText = trade.getCost().getString();
                            event.addToOutput(TechMessages.M_SEARCH_TRADE_ENERGY_PURCHASE.format(EnergyUtil.formatEnergyAmount(trade.getAmount(), false), priceText));
                            if(showStock)
                                event.addToOutput(CurrencyMessages.M_SEARCH_TRADE_STOCK.format(trade.getStock(energyTrader)));
                        }
                    }
                }
            }
        }
    }

}