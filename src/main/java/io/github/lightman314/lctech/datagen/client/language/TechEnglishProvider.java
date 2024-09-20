package io.github.lightman314.lctech.datagen.client.language;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lightmanscurrency.datagen.client.language.TranslationProvider;
import net.minecraft.data.PackOutput;

public class TechEnglishProvider extends TranslationProvider {

    public TechEnglishProvider(PackOutput output) { super(output, LCTech.MODID, "en_us"); }

    @Override
    protected void addTranslations() {

        //Items
        this.translate(TechText.ITEM_FLUID_SHARD,"Fluid Shard");
        this.translate(TechText.ITEM_FLUID_CAPACITY_UPGRADE_1,"Fluid Capacity Upgrade (Iron)");
        this.translate(TechText.ITEM_FLUID_CAPACITY_UPGRADE_2,"Fluid Capacity Upgrade (Gold)");
        this.translate(TechText.ITEM_FLUID_CAPACITY_UPGRADE_3,"Fluid Capacity Upgrade (Diamond)");
        this.translate(TechText.ITEM_FLUID_CAPACITY_UPGRADE_4,"Fluid Capacity Upgrade (Netherite)");
        this.translate(TechText.ITEM_BATTERY,"Battery");
        this.translate(TechText.ITEM_LARGE_BATTERY,"Large Battery");
        this.translate(TechText.ITEM_ENERGY_CAPACITY_UPGRADE_1,"Energy Capacity Upgrade (Iron)");
        this.translate(TechText.ITEM_ENERGY_CAPACITY_UPGRADE_2,"Energy Capacity Upgrade (Gold)");
        this.translate(TechText.ITEM_ENERGY_CAPACITY_UPGRADE_3,"Energy Capacity Upgrade (Diamond)");
        this.translate(TechText.ITEM_ENERGY_CAPACITY_UPGRADE_4,"Energy Capacity Upgrade (Netherite)");

        //Blocks
        this.translate(TechText.BLOCK_IRON_TANK,"Iron Tank");
        this.translate(TechText.BLOCK_GOLD_TANK,"Gold Tank");
        this.translate(TechText.BLOCK_DIAMOND_TANK,"Diamond Tank");
        this.translate(TechText.BLOCK_NETHERITE_TANK,"Netherite Tank");
        this.translate(TechText.BLOCK_VOID_TANK,"Void Tank");
        this.translate(TechText.BLOCK_FLUID_TAP,"Fluid Tap");
        this.translate(TechText.BLOCK_FLUID_TAP_BUNDLE,"Fluid Tap Bundle");
        this.translate(TechText.BLOCK_FLUID_NETWORK_TRADER_T1,"Fluid Network Trader T1");
        this.translate(TechText.BLOCK_FLUID_NETWORK_TRADER_T2,"Fluid Network Trader T2");
        this.translate(TechText.BLOCK_FLUID_NETWORK_TRADER_T3,"Fluid Network Trader T3");
        this.translate(TechText.BLOCK_FLUID_NETWORK_TRADER_T4,"Fluid Network Trader T4");
        this.translate(TechText.BLOCK_FLUID_TRADER_INTERFACE,"Fluid Trader Interface Terminal");

        this.translate(TechText.BLOCK_BATTERY_SHOP, "Battery Shop");
        this.translate(TechText.BLOCK_ENERGY_NETWORK_TRADER, "Energy Network Trader");
        this.translate(TechText.BLOCK_ENERGY_TRADER_INTERFACE, "Energy Trader Interface Terminal");

        //Misc Tooltips
        this.translate(TechText.TOOLTIP_VOID_TANK,"Deletes all fluids placed inside");
        this.translate(TechText.TOOLTIP_TANK_CAPACITY,"Holds up to %smB");

        this.translate(TechText.TOOLTIP_FLUID_TRADER,"Fluid Trader:","Trades: %s","Can be used to Sell or Purchase fluids from other players");
        this.translate(TechText.TOOLTIP_NETWORK_FLUID_TRADER,"Network Fluid Trader:","Trades: %s","Can be used to Sell or Purchase fluids from other players","Accessible from any location via the Trading Terminal");
        this.translate(TechText.TOOLTIP_FLUID_TRADER_INTERFACE,"Fluid Trader Interface Terminal:","Can be used to automatically purchase or sell fluid from any network fluid trader","Can be used to automatically drain or restock fluids to/from any network fluid trader you have access to");
        this.translate(TechText.TOOLTIP_ENERGY_TRADER,"Energy Trader:","Trades: 1-4","Can be used to Sell or Purchase Forge Energy from other players");
        this.translate(TechText.TOOLTIP_NETWORK_ENERGY_TRADER,"Network Energy Trader:","Trades: 1-4","Can be used to Sell or Purchase Forge Energy from other players","Accessible from any location via the Trading Terminal");
        this.translate(TechText.TOOLTIP_ENERGY_TRADER_INTERFACE,"Energy Trader Interface Terminal:","Trades: 1-4","Can be used to automatically purchase or sell energy from any network energy trader", "Can be used to automatically drain or restock energy to/from any network energy trader you have access to","Note: Has no auto-filtering for whether it should only restock or drain, so be sure to set your inputs/mode as appropriate");

        this.translate(TechText.TOOLTIP_UPGRADE_FLUID_CAPACITY,"Increases tank capacity by %smB");
        this.translate(TechText.TOOLTIP_UPGRADE_ENERGY_CAPACITY,"Increases energy capacity by %s");

        this.translate(TechText.TOOLTIP_UPGRADE_TARGET_TRADER_FLUID,"All Fluid Traders");
        this.translate(TechText.TOOLTIP_UPGRADE_TARGET_TRADER_ENERGY,"All Energy Traders");

        //Fluid Menu Text
        this.translate(TechText.TOOLTIP_FLUID_INTERACT,"Click with fluid container to fill/empty");
        this.translate(TechText.TOOLTIP_FLUID_PENDING_DRAIN,"Waiting to drain %smB of fluid");
        this.translate(TechText.TOOLTIP_FLUID_SETTINGS_DRAIN_ENABLED,"External Draining is ENABLED");
        this.translate(TechText.TOOLTIP_FLUID_SETTINGS_DRAIN_DISABLED,"External Draining is DISABLED");
        this.translate(TechText.TOOLTIP_FLUID_SETTINGS_FILL_ENABLED,"External Filling is ENABLED");
        this.translate(TechText.TOOLTIP_FLUID_SETTINGS_FILL_DISABLED,"External Filling is DISABLED");
        this.translate(TechText.TOOLTIP_SETTINGS_INPUT_FLUID,"External Fluid Input & Output");
        this.translate(TechText.TOOLTIP_TRADER_FLUID_EDIT,"Click to Set Fluid");
        this.translate(TechText.TOOLTIP_TRADE_INFO_FLUID_QUANTITY,"%1$s Bucket(s) (%1$smB)");

        //Energy Menu Text
        this.translate(TechText.GUI_SETTINGS_ENERGY_DRAINMODE,"Allow Output: %s");
        this.translate(TechText.GUI_SETTINGS_ENERGY_DRAINMODE_FULL,"Always");
        this.translate(TechText.GUI_SETTINGS_ENERGY_DRAINMODE_SALES,"Purchases Only");
        this.translate(TechText.TOOLTIP_SETTINGS_INPUT_ENERGY,"External Energy Input & Output");
        this.translate(TechText.TOOLTIP_ENERGY_PENDING_DRAIN,"Waiting to drain %s");

        this.translate(TechText.TOOLTIP_TRADE_DRAINABLE,"Drainable");
        this.translate(TechText.TOOLTIP_ALERT_NO_OUTPUT,"No room for output");
        this.translate(TechText.TOOLTIP_TRADE_INFO_SELLING,"Selling %s");
        this.translate(TechText.TOOLTIP_TRADE_INFO_PURCHASING,"Purchasing %s");

        //Trade Differences
        this.translate(TechText.GUI_TRADE_DIFFERENCE_PRODUCT_SALE,"selling");
        this.translate(TechText.GUI_TRADE_DIFFERENCE_PRODUCT_PURCHASE,"expecting");
        this.translate(TechText.GUI_TRADE_DIFFERENCE_FLUID_TYPE,"Trade is %s a different fluid");
        this.translate(TechText.GUI_TRADE_DIFFERENCE_FLUID_NBT,"Fluid NBT data does not match");
        this.translate(TechText.GUI_TRADE_DIFFERENCE_FLUID_MORE,"Trade is %1$s %2$smB more");
        this.translate(TechText.GUI_TRADE_DIFFERENCE_FLUID_LESS,"Trade is %1$s %2$smB less");
        this.translate(TechText.GUI_TRADE_DIFFERENCE_ENERGY_MORE,"Trade is %1$s %2$s more");
        this.translate(TechText.GUI_TRADE_DIFFERENCE_ENERGY_LESS,"Trade is %1$s %2$s less");

        //Notifications
        this.translate(TechText.NOTIFICATION_FLUID_FORMAT,"%1$smB of %2$s");
        this.translate(TechText.NOTIFICATION_TRADE_FLUID,"%1$s %2$s %3$s for %4$s");
        this.translate(TechText.NOTIFICATION_TRADE_ENERGY,"%1$s %2$s %3$s for %4$s");

    }
}
