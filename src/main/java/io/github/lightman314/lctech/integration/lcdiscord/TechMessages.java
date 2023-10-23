package io.github.lightman314.lctech.integration.lcdiscord;

import io.github.lightman314.lightmansdiscord.events.LoadMessageEntriesEvent;
import io.github.lightman314.lightmansdiscord.message.MessageEntry;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class TechMessages {

    private static final List<MessageEntry> ENTRIES = new ArrayList<>();

    public static final MessageEntry M_SEARCH_TRADE_FLUID_SALE = MessageEntry.create(ENTRIES, "command_search_trade_fluid_sale", "Format of a fluid sale when displaying the results of the search.\\n{fluidName} for the fluid being sold.\\n{fluidAmount} for the quantity in mB of the fluid.\\n{price} for the price.", "Selling {fluidAmount}mB of {fluidName} for {price}","fluidAmount","fluidName","price");
    public static final MessageEntry M_SEARCH_TRADE_FLUID_PURCHASE = MessageEntry.create(ENTRIES, "command_search_trade_fluid_purchase", "Format of a fluid purchase when displaying the results of the search.\\n{fluidName} for the fluid being sold.\\n{fluidAmount} for the quantity in mB of the fluid.\\n{price} for the price.", "Purchasing {fluidAmount}mB of {fluidName} for {price}","fluidAmount","fluidName","price");
    public static final MessageEntry M_SEARCH_TRADE_ENERGY_SALE = MessageEntry.create(ENTRIES, "command_search_trade_energy_sale", "Format of an energy sale when displaying the results of the search.\n{amount} for the amount of energy being sold.\n{price} for the price.", "Selling {amount}FE for {price}", "amount", "price");
    public static final MessageEntry M_SEARCH_TRADE_ENERGY_PURCHASE = MessageEntry.create(ENTRIES, "command_search_trade_energy_purchase", "Format of an energy purchase when displaying the results of the search.\n{amount} for the amount of energy being purchased.\n{price} for the price.", "Purchasing {amount}FE for {price}", "amount", "price");


    @SubscribeEvent
    public static void registerMessages(LoadMessageEntriesEvent event) { event.register(ENTRIES); }

}