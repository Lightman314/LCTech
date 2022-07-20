package io.github.lightman314.lctech.client;

import java.util.Map;

import com.google.common.base.Function;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.models.items.FluidShardModel;
import io.github.lightman314.lctech.client.models.items.FluidTankModel;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lctech.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.menu.slots.FluidInputSlot;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCTech.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void onModelBakeEvent(ModelEvent.BakingCompleted event)
	{
		FluidTankItem.getTankModelList().forEach(itemModelResourceLocation ->{
			replaceModel(itemModelResourceLocation, event.getModels(), (existingModel) -> new FluidTankModel(existingModel));
		});
		FluidShardItem.getShardModelList().forEach(itemModelResourceLocation ->{
			replaceModel(itemModelResourceLocation, event.getModels(), (existingModel) -> new FluidShardModel(existingModel));
		});
	}
	
	private static void replaceModel(ModelResourceLocation itemModelResourceLocation, Map<ResourceLocation, BakedModel> modelRegistry, Function<BakedModel,BakedModel> modelGenerator)
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
	public static void stitchTextures(TextureStitchEvent.Pre event)
	{
		if(event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
			//Add bucket slot background
			event.addSprite(FluidInputSlot.EMPTY_FLUID_SLOT);
			//Add battery slot background
			event.addSprite(BatteryInputSlot.EMPTY_BATTERY_SLOT);
		}
	}
	
}
