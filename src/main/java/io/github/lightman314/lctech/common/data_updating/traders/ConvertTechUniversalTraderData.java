package io.github.lightman314.lctech.common.data_updating.traders;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lightmanscurrency.common.data_updating.events.ConvertUniversalTraderEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID)
@SuppressWarnings("deprecation")
public class ConvertTechUniversalTraderData {

	public static final ResourceLocation FLUID_TYPE = new ResourceLocation(LCTech.MODID, "fluid_trader");
	public static final ResourceLocation ENERGY_TYPE = new ResourceLocation(LCTech.MODID, "energy_trader");
	
	@SubscribeEvent
	public static void convertTextTraders(ConvertUniversalTraderEvent event) {
		if(event.type.equals(FLUID_TYPE))
		{
			FluidTraderData newTrader = new FluidTraderData();
			newTrader.loadOldUniversalTraderData(event.compound);
			event.setTrader(newTrader);
		}
		else if(event.type.equals(ENERGY_TYPE))
		{
			EnergyTraderData newTrader = new EnergyTraderData();
			newTrader.loadOldUniversalTraderData(event.compound);
			event.setTrader(newTrader);
		}
	}
	
	
}
