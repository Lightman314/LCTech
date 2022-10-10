package io.github.lightman314.lctech;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.notifications.types.EnergyTradeNotification;
import io.github.lightman314.lctech.common.notifications.types.FluidTradeNotification;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.terminal.traderSearching.FluidTraderSearchFilter;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.core.ModItems;
import io.github.lightman314.lctech.core.ModRegistries;
import io.github.lightman314.lctech.crafting.condition.TechCraftingConditions;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.proxy.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;

@Mod("lctech")
public class LCTech
{
	
	public static final String MODID = "lctech";
	
	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public LCTech() {
    	
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doCommonStuff);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        //Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TechConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TechConfig.serverSpec);
        
        //.Setup Deferred Registries
        ModRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        //Register the proxy so that it can run custom events
        MinecraftForge.EVENT_BUS.register(PROXY);
        
    }

    private void doCommonStuff(final FMLCommonSetupEvent event)
    {
        LCTechPacketHandler.init();
        
        //Register Trader Search Filters
        TraderSearchFilter.addFilter(new FluidTraderSearchFilter());
        
        //Register the universal data deserializer
        TraderData.register(FluidTraderData.TYPE, FluidTraderData::new);
        TraderData.register(EnergyTraderData.TYPE, EnergyTraderData::new);
        
        //Register custom notification types
        Notification.register(FluidTradeNotification.TYPE, FluidTradeNotification::new);
        Notification.register(EnergyTradeNotification.TYPE, EnergyTradeNotification::new);
        
        //Register Crafting Conditions
        CraftingHelper.register(TechCraftingConditions.FluidTrader.SERIALIZER);
        CraftingHelper.register(TechCraftingConditions.FluidTank.SERIALIZER);
        CraftingHelper.register(TechCraftingConditions.EnergyTrader.SERIALIZER);
        CraftingHelper.register(TechCraftingConditions.Batteries.SERIALIZER);
        
        //Add our items/blocks to the creative tab sorting
        try {
        	LightmansCurrency.MACHINE_GROUP.addToSortingList(Lists.newArrayList(
            		ModBlocks.IRON_TANK.get(), ModBlocks.GOLD_TANK.get(), ModBlocks.DIAMOND_TANK.get(),
            		ModBlocks.FLUID_TRADER_INTERFACE.get(),
            		ModItems.BATTERY.get(), ModItems.BATTERY_LARGE.get(),
            		ModBlocks.ENERGY_TRADER_INTERFACE.get()
            		));
            
        	LightmansCurrency.UPGRADE_GROUP.addToSortingList(Lists.newArrayList(
        			ModItems.FLUID_CAPACITY_UPGRADE_1.get(), ModItems.FLUID_CAPACITY_UPGRADE_2.get(), ModItems.FLUID_CAPACITY_UPGRADE_3.get(),
        			ModItems.ENERGY_CAPACITY_UPGRADE_1.get(), ModItems.ENERGY_CAPACITY_UPGRADE_2.get(), ModItems.ENERGY_CAPACITY_UPGRADE_3.get()
        			));
        	
            LightmansCurrency.TRADING_GROUP.addToSortingList(Lists.newArrayList(
            		ModBlocks.FLUID_TAP.get(), ModBlocks.FLUID_TAP_BUNDLE.get(),
            		ModBlocks.FLUID_NETWORK_TRADER_1.get(), ModBlocks.FLUID_NETWORK_TRADER_2.get(), ModBlocks.FLUID_NETWORK_TRADER_3.get(), ModBlocks.FLUID_NETWORK_TRADER_4.get(),
            		ModBlocks.BATTERY_SHOP.get(), ModBlocks.ENERGY_NETWORK_TRADER.get()
            		));
            
        } catch(Throwable t) { LOGGER.error("Error adding items to LC Creative Tabs.", t); }
        
        
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        PROXY.setupClient();
    }

}