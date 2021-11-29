package io.github.lightman314.lctech;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lctech.client.ClientModEvents;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.proxy.*;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("lctech")
public class LCTech
{
	
	public static final String MODID = "lctech";
	
	private static boolean lightmansCurrencyLoaded = false;
	
	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public LCTech() {
    	
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doCommonStuff);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        //Register the model event on client
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new RegisterClientModEvent());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        lightmansCurrencyLoaded = ModList.get().isLoaded("lightmanscurrency");
        
    }
    
    public static boolean isLCLoaded()
    {
    	return lightmansCurrencyLoaded;
    }

    private void doCommonStuff(final FMLCommonSetupEvent event)
    {
        LCTechPacketHandler.init();
        
        //Register the universal data deserializer
        TradingOffice.RegisterDataType(UniversalFluidTraderData.TYPE, UniversalFluidTraderData::new);
        
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        PROXY.setupClient();
    }
    
    private static class RegisterClientModEvent implements DistExecutor.SafeRunnable
    {

		private static final long serialVersionUID = 225646674509735485L;

		@Override
		public void run() {
			FMLJavaModLoadingContext.get().getModEventBus().register(new ClientModEvents());
		}
		
    }

}
