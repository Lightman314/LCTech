package io.github.lightman314.lctech.client;

import java.util.Map;

import com.google.common.base.Function;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.models.items.FluidShardModel;
import io.github.lightman314.lctech.client.models.items.FluidTankModel;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderDataManager;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.client.EnergyTradeButtonRenderer;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.client.FluidTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterTradeRenderManagersEvent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = LCTech.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void onModelBakeEvent(ModelEvent.ModifyBakingResult event)
	{
		FluidTankItem.getTankModelList().forEach(itemModelResourceLocation -> replaceModel(itemModelResourceLocation, event.getModels(), FluidTankModel::new));
		FluidShardItem.getShardModelList().forEach(itemModelResourceLocation -> replaceModel(itemModelResourceLocation, event.getModels(), FluidShardModel::new));
	}
	
	private static void replaceModel(ModelResourceLocation itemModelResourceLocation, Map<ModelResourceLocation, BakedModel> modelRegistry, Function<BakedModel,BakedModel> modelGenerator)
	{
		BakedModel existingModel = modelRegistry.get(itemModelResourceLocation);
		if(existingModel == null) {
			LCTech.LOGGER.warn("Did not find the expected vanilla baked model for FluidTankModel in registry.");
		}
		else {
			//Replace the model
			//LCTech.LOGGER.info("Replacing the Fluid Tank item model.");
			BakedModel customModel = modelGenerator.apply(existingModel);
			modelRegistry.put(itemModelResourceLocation, customModel);
		}
	}

	@SubscribeEvent
	public static void registerResourceListener(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(FluidRenderDataManager.INSTANCE);
	}

    @SubscribeEvent
    public static void registerTradeRenderManagers(RegisterTradeRenderManagersEvent event)
    {
        event.register(EnergyTradeButtonRenderer::new,EnergyTradeData.class);
        event.register(FluidTradeButtonRenderer::new,FluidTradeData.class);
    }
	
}
