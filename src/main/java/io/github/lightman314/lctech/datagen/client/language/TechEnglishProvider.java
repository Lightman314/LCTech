package io.github.lightman314.lctech.datagen.client.language;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lightmanscurrency.datagen.client.language.TranslationProvider;
import net.minecraft.data.PackOutput;

public class TechEnglishProvider extends TranslationProvider {

    public TechEnglishProvider(PackOutput output) { super(output, LCTech.MODID, "en_us"); }

    @Override
    protected void createTranslations() {

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
        this.translate(TechText.TOOLTIP_TRADE_INFO_FLUID_QUANTITY,"%1$s Bucket(s) (%2$smB)");

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

        //Data
        this.translate(TechText.DATA_ENTRY_TRADER_TRADE_FLUID,"Fluid: %2$sB of %1$s");
        this.translate(TechText.DATA_ENTRY_TRADER_TRADE_ENERGY,"Energy");


        //Config Options
        //Common Config
        this.translateConfigName(TechConfig.COMMON,"Common Config");
        this.translateConfigSection(TechConfig.COMMON,"crafting","Crafting Settings","/reload required for any changes made here to take effect.", "Disabling will not remove any existing items/blocks from the world, nor prevent their use.");
        this.translateConfigOption(TechConfig.COMMON.canCraftFluidTraders,"Fluid Traders","Whether Fluid Traders can be crafted.","Also affects crafting of fluid trader accessories (Fluid Trader Interface, Fluid Capacity Upgrades, etc.)");
        this.translateConfigOption(TechConfig.COMMON.canCraftFluidTanks,"Fluid Tanks","Whether Fluid Tanks can be crafted.");
        this.translateConfigOption(TechConfig.COMMON.canCraftVoidTanks,"Void Tank","Whether the Void Tank can be crafted.");
        this.translateConfigOption(TechConfig.COMMON.canCraftEnergyTraders,"Energy Traders","Whether Energy Traders can be crafted.","Also affects crafting of energy trader accessories (Energy Trader Interface, Energy Capacity Upgrades, etc.)");
        this.translateConfigOption(TechConfig.COMMON.canCraftBatteries,"Batteries","Whether Batteries can be crafted.");

        //Server Config
        this.translateConfigName(TechConfig.SERVER,"Server Config");
        this.translateConfigSection(TechConfig.SERVER,"fluid","Fluid Settings");
        this.translateConfigSection(TechConfig.SERVER,"fluid.trader","Fluid Trader Settings");
        this.translateConfigOption(TechConfig.SERVER.fluidTraderDefaultStorage,"Default Fluid Storage","The amount of fluid storage a fluid trade has by default in Buckets (1,000mB).");
        this.translateConfigOption(TechConfig.SERVER.fluidTradeMaxQuantity,"Trade Fluid Limit","The maximum quantity of fluids allowed to be sold or purchased in a single trade in Buckets (1,000mB).", "Regardless of the input, it will always be enforced to be less than or equal to the fluid trades current maximum capacity.");
        this.translateConfigSection(TechConfig.SERVER,"fluid.tank","Fluid Tank Settings");
        this.translateConfigOption(TechConfig.SERVER.ironTankCapacity,"Iron Tank Capacity","The amount of fluid storage the Iron Tank can hold in Buckets (1,000mB)");
        this.translateConfigOption(TechConfig.SERVER.goldTankCapacity,"Gold Tank Capacity","The amount of fluid storage the Gold Tank can hold in Buckets (1,000mB)");
        this.translateConfigOption(TechConfig.SERVER.diamondTankCapacity,"Diamond Tank Capacity","The amount of fluid storage the Diamond Tank can hold in Buckets (1,000mB)");
        this.translateConfigOption(TechConfig.SERVER.netheriteTankCapacity,"Netherite Tank Capacity","The amount of fluid storage the Netherite Tank can hold in Buckets (1,000mB)");
        this.translateConfigSection(TechConfig.SERVER,"fluid.upgrades","Fluid Upgrade Settings");
        this.translateConfigOption(TechConfig.SERVER.fluidUpgradeCapacity1,"T1 Capacity","The amount of fluid storage added by the Fluid Capacity upgrade (Iron) in Buckets (1,000mB)");
        this.translateConfigOption(TechConfig.SERVER.fluidUpgradeCapacity2,"T2 Capacity","The amount of fluid storage added by the Fluid Capacity upgrade (Gold) in Buckets (1,000mB)");
        this.translateConfigOption(TechConfig.SERVER.fluidUpgradeCapacity3,"T3 Capacity","The amount of fluid storage added by the Fluid Capacity upgrade (Diamond) in Buckets (1,000mB)");
        this.translateConfigOption(TechConfig.SERVER.fluidUpgradeCapacity4,"T4 Capacity","The amount of fluid storage added by the Fluid Capacity upgrade (Netherite) in Buckets (1,000mB)");
        this.translateConfigSection(TechConfig.SERVER,"fluid.interface","Fluid Trader Interface Settings");
        this.translateConfigOption(TechConfig.SERVER.fluidRestockSpeed,"Restock Rate","The amount of fluid in mB that can be drained or restocked in a single drain tick (once per second).");
        this.translateConfigSection(TechConfig.SERVER,"energy","Energy Settings");
        this.translateConfigSection(TechConfig.SERVER,"energy.trader","Energy Trader Settings");
        this.translateConfigOption(TechConfig.SERVER.energyTraderDefaultStorage,"Default Energy Storage","The amount of FE an energy trader can store by default.");
        this.translateConfigOption(TechConfig.SERVER.energyTradeMaxQuantity,"Trade Energy Limit","The maximum amount of FE an energy trader can sell or purchase in a single trade.",  "Regardless of the input, it will always be enforced to be less than or equal to the energy traders current maximum capacity");
        this.translateConfigSection(TechConfig.SERVER,"energy.battery","Battery Settings");
        this.translateConfigOption(TechConfig.SERVER.batteryCapacity,"Battery Capacity","The amount of FE a Battery can hold.");
        this.translateConfigOption(TechConfig.SERVER.largeBatteryCapacity,"Large Battery Capacity","The amount of FE a Large Battery can hold.");
        this.translateConfigSection(TechConfig.SERVER,"energy.upgrades","Energy Upgrade Settings");
        this.translateConfigOption(TechConfig.SERVER.energyUpgradeCapacity1,"T1 Capacity","The amount of energy storage added by the Energy Capacity Upgrade (Iron).");
        this.translateConfigOption(TechConfig.SERVER.energyUpgradeCapacity2,"T2 Capacity","The amount of energy storage added by the Energy Capacity Upgrade (Gold).");
        this.translateConfigOption(TechConfig.SERVER.energyUpgradeCapacity3,"T3 Capacity","The amount of energy storage added by the Energy Capacity Upgrade (Diamond).");
        this.translateConfigOption(TechConfig.SERVER.energyUpgradeCapacity4,"T4 Capacity","The amount of energy storage added by the Energy Capacity Upgrade (Netherite).");
        this.translateConfigSection(TechConfig.SERVER,"energy.interface","Energy Interface Settings");
        this.translateConfigOption(TechConfig.SERVER.energyRestockSpeed,"Restock Rate","The amount of FE that can be drained or restocked in a single drain tick (once per second).");

    }
}
