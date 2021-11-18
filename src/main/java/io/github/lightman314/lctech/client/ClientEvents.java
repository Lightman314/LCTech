package io.github.lightman314.lctech.client;

import java.util.Map;

import com.google.common.base.Function;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.models.items.FluidShardModel;
import io.github.lightman314.lctech.client.models.items.FluidTankModel;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.items.FluidTankItem;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		FluidTankItem.getTankModelList().forEach(itemModelResourceLocation ->{
			replaceModel(itemModelResourceLocation, event.getModelRegistry(), (existingModel) -> new FluidTankModel(existingModel));
		});
		FluidShardItem.getShardModelList().forEach(itemModelResourceLocation ->{
			replaceModel(itemModelResourceLocation, event.getModelRegistry(), (existingModel) -> new FluidShardModel(existingModel));
		});
	}
	
	private static void replaceModel(ModelResourceLocation itemModelResourceLocation, Map<ResourceLocation, IBakedModel> modelRegistry, Function<IBakedModel,IBakedModel> modelGenerator)
	{
		IBakedModel existingModel = modelRegistry.get(itemModelResourceLocation);
		if(existingModel == null) {
			LCTech.LOGGER.warn("Did not find the expected vanilla baked model for FluidTankModel in registry.");
		}
		else {
			//Replace the model
			//LCTech.LOGGER.info("Replacing the Fluid Tank item model.");
			IBakedModel customModel = modelGenerator.apply(existingModel);
			modelRegistry.put(itemModelResourceLocation, customModel);
		}
	}
	
}
