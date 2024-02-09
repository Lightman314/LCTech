package io.github.lightman314.lctech;

import com.google.common.collect.Lists;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.integration.lcdiscord.TechDiscord;
import io.github.lightman314.lightmanscurrency.ModCreativeGroups;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
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
import io.github.lightman314.lctech.common.traders.terminal.traderSearching.FluidTraderSearchFilter;
import io.github.lightman314.lctech.common.core.ModRegistries;
import io.github.lightman314.lctech.common.crafting.condition.TechCraftingConditions;
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
        MinecraftForge.EVENT_BUS.register(PROXY);

        IntegrationUtil.SafeRunIfLoaded("lightmansdiscord", TechDiscord::setup, "Error setting up Tech Discord Integration");

    }

    private void doCommonStuff(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

    private void commonSetupWork() {
        LCTechPacketHandler.init();

        //Register Trader Search Filters
        TraderAPI.registerSearchFilter(new FluidTraderSearchFilter());

        //Register the universal data deserializer
        TraderAPI.registerTrader(FluidTraderData.TYPE);
        TraderAPI.registerTrader(EnergyTraderData.TYPE);

        //Register custom notification types
        NotificationAPI.registerNotification(FluidTradeNotification.TYPE);
        NotificationAPI.registerNotification(EnergyTradeNotification.TYPE);

        //Register Crafting Conditions
        CraftingHelper.register(TechCraftingConditions.FluidTrader.SERIALIZER);
        CraftingHelper.register(TechCraftingConditions.FluidTank.SERIALIZER);
        CraftingHelper.register(TechCraftingConditions.EnergyTrader.SERIALIZER);
        CraftingHelper.register(TechCraftingConditions.Batteries.SERIALIZER);

        //Add our items/blocks to the creative tab sorting
        try {
            ModCreativeGroups.getMachineGroup().addToSortingList(Lists.newArrayList(
                    ModBlocks.IRON_TANK.get(), ModBlocks.GOLD_TANK.get(), ModBlocks.DIAMOND_TANK.get(),
                    ModBlocks.FLUID_TRADER_INTERFACE.get(),
                    ModItems.BATTERY.get(), ModItems.BATTERY_LARGE.get(),
                    ModBlocks.ENERGY_TRADER_INTERFACE.get()
            ));

            ModCreativeGroups.getUpgradeGroup().addToSortingList(Lists.newArrayList(
                    ModItems.FLUID_CAPACITY_UPGRADE_1.get(), ModItems.FLUID_CAPACITY_UPGRADE_2.get(), ModItems.FLUID_CAPACITY_UPGRADE_3.get(),
                    ModItems.ENERGY_CAPACITY_UPGRADE_1.get(), ModItems.ENERGY_CAPACITY_UPGRADE_2.get(), ModItems.ENERGY_CAPACITY_UPGRADE_3.get()
            ));

            ModCreativeGroups.getTradingGroup().addToSortingList(Lists.newArrayList(
                    ModBlocks.FLUID_TAP.get(), ModBlocks.FLUID_TAP_BUNDLE.get(),
                    ModBlocks.FLUID_NETWORK_TRADER_1.get(), ModBlocks.FLUID_NETWORK_TRADER_2.get(), ModBlocks.FLUID_NETWORK_TRADER_3.get(), ModBlocks.FLUID_NETWORK_TRADER_4.get(),
                    ModBlocks.BATTERY_SHOP.get(), ModBlocks.ENERGY_NETWORK_TRADER.get()
            ));

        } catch(Throwable t) { LOGGER.error("Error adding items to LC Creative Tabs.", t); }

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
