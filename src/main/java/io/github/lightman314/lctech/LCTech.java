package io.github.lightman314.lctech;

import io.github.lightman314.lctech.common.util.icons.FluidIcon;
import io.github.lightman314.lctech.integration.lcdiscord.TechDiscord;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
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
import io.github.lightman314.lctech.common.traders.terminal.filters.FluidTraderSearchFilter;
import io.github.lightman314.lctech.common.core.ModRegistries;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.proxy.*;

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

        //Force config class loading
        TechConfig.init();
        
        //.Setup Deferred Registries
        ModRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        //Register the proxy so that it can run custom events
        PROXY.init();

        IntegrationUtil.SafeRunIfLoaded("lightmansdiscord", TechDiscord::setup, "Error setting up Tech Discord Integration");
        
    }

    private void doCommonStuff(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

    private void commonSetupWork() {

        LCTechPacketHandler.init();

        //Register Trader Search Filters
        TraderAPI.getApi().RegisterSearchFilter(new FluidTraderSearchFilter());

        //Register the universal data deserializer
        TraderAPI.getApi().RegisterTrader(FluidTraderData.TYPE);
        TraderAPI.getApi().RegisterTrader(EnergyTraderData.TYPE);

        //Register custom notification types
        NotificationAPI.getApi().RegisterNotification(FluidTradeNotification.TYPE);
        NotificationAPI.getApi().RegisterNotification(EnergyTradeNotification.TYPE);

        //Register Custom Upgrade Targets
        Upgrades.TRADE_OFFERS.addTarget(TechText.TOOLTIP_UPGRADE_TARGET_TRADER_FLUID.get());
        Upgrades.VOID.addTarget(TechText.TOOLTIP_UPGRADE_TARGET_TRADER_FLUID.get());
        Upgrades.VOID.addTarget(TechText.TOOLTIP_UPGRADE_TARGET_TRADER_ENERGY.get());

        //Register Custom Icons
        FluidIcon.register();

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
