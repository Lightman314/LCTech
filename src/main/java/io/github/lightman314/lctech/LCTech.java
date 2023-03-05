package io.github.lightman314.lctech;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lctech.common.notifications.types.EnergyTradeNotification;
import io.github.lightman314.lctech.common.notifications.types.FluidTradeNotification;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.terminal.traderSearching.FluidTraderSearchFilter;
import io.github.lightman314.lctech.common.core.ModRegistries;
import io.github.lightman314.lctech.common.crafting.condition.TechCraftingConditions;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.proxy.*;
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

    private void doCommonStuff(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

    private void commonSetupWork() {
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
    }

    private void doClientStuff(final FMLClientSetupEvent event) { safeEnqueueWork(event, "Error during client setup!", PROXY::setupClient); }

    public static void safeEnqueueWork(ParallelDispatchEvent event, String errorMessage, Runnable work) {
        event.enqueueWork(() -> {
            try{
                work.run();
            } catch(Throwable t) {
                LOGGER.error(errorMessage, t);
            }
        });
    }

}
