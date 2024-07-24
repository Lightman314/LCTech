package io.github.lightman314.lctech;

import io.github.lightman314.lctech.common.util.icons.FluidIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lctech.common.notifications.types.EnergyTradeNotification;
import io.github.lightman314.lctech.common.notifications.types.FluidTradeNotification;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.terminal.filters.FluidTraderSearchFilter;
import io.github.lightman314.lctech.common.core.ModRegistries;
import io.github.lightman314.lctech.proxy.*;

import javax.annotation.Nonnull;

@Mod("lctech")
public class LCTech
{
	
	public static final String MODID = "lctech";
	
	public static final CommonProxy PROXY;

    static {
        if(FMLLoader.getDist() == Dist.CLIENT)
            PROXY = new ClientProxy();
        else
            PROXY = new CommonProxy();
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public LCTech(@Nonnull IEventBus bus) {
    	
        // Register the setup method for modloading
        bus.addListener(this::doCommonStuff);
        // Register the doClientStuff method for modloading
        bus.addListener(this::doClientStuff);

        //Force config class loading
        TechConfig.init();
        
        //.Setup Deferred Registries
        ModRegistries.register(bus);

        //Register the proxy so that it can run custom events
        if(PROXY.isClient())
            NeoForge.EVENT_BUS.register(PROXY);

    }

    private void doCommonStuff(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

    private void commonSetupWork() {

        //Register Trader Search Filters
        TraderAPI.API.RegisterSearchFilter(new FluidTraderSearchFilter());

        //Register the universal data deserializer
        TraderAPI.API.RegisterTrader(FluidTraderData.TYPE);
        TraderAPI.API.RegisterTrader(EnergyTraderData.TYPE);

        //Register custom notification types
        NotificationAPI.registerNotification(FluidTradeNotification.TYPE);
        NotificationAPI.registerNotification(EnergyTradeNotification.TYPE);

        //Register Custom Upgrade Targets
        Upgrades.TRADE_OFFERS.addTarget(TechText.TOOLTIP_UPGRADE_TARGET_TRADER_FLUID.get());

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
