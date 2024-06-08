package io.github.lightman314.lctech;

import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.notifications.types.*;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.resources.ResourceLocation;

public class TechText {

    private static final String MODID = LCTech.MODID;

    //Items
    public static final TextEntry ITEM_FLUID_SHARD = TextEntry.item(ModItems.FLUID_SHARD);
    public static final TextEntry ITEM_FLUID_CAPACITY_UPGRADE_1 = TextEntry.item(ModItems.FLUID_CAPACITY_UPGRADE_1);
    public static final TextEntry ITEM_FLUID_CAPACITY_UPGRADE_2 = TextEntry.item(ModItems.FLUID_CAPACITY_UPGRADE_2);
    public static final TextEntry ITEM_FLUID_CAPACITY_UPGRADE_3 = TextEntry.item(ModItems.FLUID_CAPACITY_UPGRADE_3);
    public static final TextEntry ITEM_BATTERY = TextEntry.item(ModItems.BATTERY);
    public static final TextEntry ITEM_LARGE_BATTERY = TextEntry.item(ModItems.BATTERY_LARGE);
    public static final TextEntry ITEM_ENERGY_CAPACITY_UPGRADE_1 = TextEntry.item(ModItems.ENERGY_CAPACITY_UPGRADE_1);
    public static final TextEntry ITEM_ENERGY_CAPACITY_UPGRADE_2 = TextEntry.item(ModItems.ENERGY_CAPACITY_UPGRADE_2);
    public static final TextEntry ITEM_ENERGY_CAPACITY_UPGRADE_3 = TextEntry.item(ModItems.ENERGY_CAPACITY_UPGRADE_3);

    //Blocks
    public static final TextEntry BLOCK_IRON_TANK = TextEntry.block(ModBlocks.IRON_TANK);
    public static final TextEntry BLOCK_GOLD_TANK = TextEntry.block(ModBlocks.GOLD_TANK);
    public static final TextEntry BLOCK_DIAMOND_TANK = TextEntry.block(ModBlocks.DIAMOND_TANK);
    public static final TextEntry BLOCK_VOID_TANK = TextEntry.block(ModBlocks.VOID_TANK);
    public static final TextEntry BLOCK_FLUID_TAP = TextEntry.block(ModBlocks.FLUID_TAP);
    public static final TextEntry BLOCK_FLUID_TAP_BUNDLE = TextEntry.block(ModBlocks.FLUID_TAP_BUNDLE);
    public static final TextEntry BLOCK_FLUID_NETWORK_TRADER_T1 = TextEntry.block(ModBlocks.FLUID_NETWORK_TRADER_1);
    public static final TextEntry BLOCK_FLUID_NETWORK_TRADER_T2 = TextEntry.block(ModBlocks.FLUID_NETWORK_TRADER_2);
    public static final TextEntry BLOCK_FLUID_NETWORK_TRADER_T3 = TextEntry.block(ModBlocks.FLUID_NETWORK_TRADER_3);
    public static final TextEntry BLOCK_FLUID_NETWORK_TRADER_T4 = TextEntry.block(ModBlocks.FLUID_NETWORK_TRADER_4);
    public static final TextEntry BLOCK_FLUID_TRADER_INTERFACE = TextEntry.block(ModBlocks.FLUID_TRADER_INTERFACE);

    public static final TextEntry BLOCK_BATTERY_SHOP = TextEntry.block(ModBlocks.BATTERY_SHOP);
    public static final TextEntry BLOCK_ENERGY_NETWORK_TRADER = TextEntry.block(ModBlocks.ENERGY_NETWORK_TRADER);
    public static final TextEntry BLOCK_ENERGY_TRADER_INTERFACE = TextEntry.block(ModBlocks.ENERGY_TRADER_INTERFACE);

    //Misc Tooltips
    public static final TextEntry TOOLTIP_VOID_TANK = TextEntry.tooltip(MODID,"void_tank");
    public static final TextEntry TOOLTIP_TANK_CAPACITY = TextEntry.tooltip(MODID,"fluid_tank.capacity");

    public static final MultiLineTextEntry TOOLTIP_FLUID_TRADER = MultiLineTextEntry.tooltip(MODID,"trader.fluid");
    public static final MultiLineTextEntry TOOLTIP_NETWORK_FLUID_TRADER = MultiLineTextEntry.tooltip(MODID,"trader.network.fluid");
    public static final MultiLineTextEntry TOOLTIP_FLUID_TRADER_INTERFACE = MultiLineTextEntry.tooltip(MODID,"interface.fluid");
    public static final MultiLineTextEntry TOOLTIP_ENERGY_TRADER = MultiLineTextEntry.tooltip(MODID,"trader.energy");
    public static final MultiLineTextEntry TOOLTIP_NETWORK_ENERGY_TRADER = MultiLineTextEntry.tooltip(MODID,"trader.network.energy");
    public static final MultiLineTextEntry TOOLTIP_ENERGY_TRADER_INTERFACE = MultiLineTextEntry.tooltip(MODID,"interface.energy");

    public static final TextEntry TOOLTIP_UPGRADE_FLUID_CAPACITY = TextEntry.tooltip(MODID,"upgrade.fluid_capacity");
    public static final TextEntry TOOLTIP_UPGRADE_ENERGY_CAPACITY = TextEntry.tooltip(MODID,"upgrade.energy_capacity");

    public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER_FLUID = TextEntry.tooltip(MODID,"upgrade.target.traders.fluid");
    public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER_ENERGY = TextEntry.tooltip(MODID,"upgrade.target.traders.energy");

    //Fluid Menu Text
    public static final TextEntry TOOLTIP_FLUID_INTERACT = TextEntry.tooltip(MODID,"fluid.interact");
    public static final TextEntry TOOLTIP_FLUID_PENDING_DRAIN = TextEntry.tooltip(MODID,"fluid.pending_drain");
    public static final TextEntry TOOLTIP_FLUID_SETTINGS_DRAIN_ENABLED = TextEntry.tooltip(MODID,"trader.fluid_settings.drain.enabled");
    public static final TextEntry TOOLTIP_FLUID_SETTINGS_DRAIN_DISABLED = TextEntry.tooltip(MODID,"trader.fluid_settings.drain.disabled");
    public static final TextEntry TOOLTIP_FLUID_SETTINGS_FILL_ENABLED = TextEntry.tooltip(MODID,"trader.fluid_settings.fill.enabled");
    public static final TextEntry TOOLTIP_FLUID_SETTINGS_FILL_DISABLED = TextEntry.tooltip(MODID,"trader.fluid_settings.fill.disabled");
    public static final TextEntry TOOLTIP_SETTINGS_INPUT_FLUID = TextEntry.tooltip(MODID,"settings.fluid_input");
    public static final TextEntry TOOLTIP_TRADER_FLUID_EDIT = TextEntry.tooltip(MODID,"trader.fluid_edit");
    public static final TextEntry TOOLTIP_TRADE_INFO_FLUID_QUANTITY = TextEntry.tooltip(MODID,"fluid_trade.info.quantity");

    //Energy Menu Text
    public static final TextEntry GUI_SETTINGS_ENERGY_DRAINMODE = TextEntry.gui(MODID,"settings.energy.drainmode");
    public static final TextEntry GUI_SETTINGS_ENERGY_DRAINMODE_FULL = TextEntry.gui(MODID,"settings.energy.drainmode.full");
    public static final TextEntry GUI_SETTINGS_ENERGY_DRAINMODE_SALES = TextEntry.gui(MODID,"settings.energy.drainmode.sales");
    public static final TextEntry TOOLTIP_SETTINGS_INPUT_ENERGY = TextEntry.tooltip(MODID,"settings.energy_input");
    public static final TextEntry TOOLTIP_ENERGY_PENDING_DRAIN = TextEntry.tooltip(MODID,"energy.pending_drain");

    public static final TextEntry TOOLTIP_TRADE_DRAINABLE = TextEntry.tooltip(MODID,"trader.settings.drainable");
    public static final TextEntry TOOLTIP_ALERT_NO_OUTPUT = TextEntry.tooltip(MODID,"no_output_container");
    public static final TextEntry TOOLTIP_TRADE_INFO_SELLING = TextEntry.tooltip(MODID,"trade.info.sale");
    public static final TextEntry TOOLTIP_TRADE_INFO_PURCHASING = TextEntry.tooltip(MODID,"trade.info.purchase");

    //Trade Differences
    public static final TextEntry GUI_TRADE_DIFFERENCE_PRODUCT_SALE = TextEntry.gui(MODID,"interface.difference.product.sale");
    public static final TextEntry GUI_TRADE_DIFFERENCE_PRODUCT_PURCHASE = TextEntry.gui(MODID,"interface.difference.product.purchase");
    public static final TextEntry GUI_TRADE_DIFFERENCE_FLUID_TYPE = TextEntry.gui(MODID,"interface.fluid.difference.type");
    public static final TextEntry GUI_TRADE_DIFFERENCE_FLUID_NBT = TextEntry.gui(MODID,"interface.fluid.difference.nbt");
    public static final TextEntry GUI_TRADE_DIFFERENCE_FLUID_MORE = TextEntry.gui(MODID,"interface.fluid.difference.quantity.more");
    public static final TextEntry GUI_TRADE_DIFFERENCE_FLUID_LESS = TextEntry.gui(MODID,"interface.fluid.difference.quantity.less");

    public static final TextEntry GUI_TRADE_DIFFERENCE_ENERGY_MORE = TextEntry.gui(MODID,"interface.energy.difference.quantity.more");
    public static final TextEntry GUI_TRADE_DIFFERENCE_ENERGY_LESS = TextEntry.gui(MODID,"interface.energy.difference.quantity.less");

    //Permissions


    //Notifications
    public static final TextEntry NOTIFICATION_FLUID_FORMAT = TextEntry.notification(new ResourceLocation(MODID,"fluids"),"format");
    public static final TextEntry NOTIFICATION_TRADE_FLUID = TextEntry.notification(FluidTradeNotification.TYPE);
    public static final TextEntry NOTIFICATION_TRADE_ENERGY = TextEntry.notification(EnergyTradeNotification.TYPE);

}
