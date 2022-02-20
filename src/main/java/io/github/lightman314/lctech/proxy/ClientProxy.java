package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.client.gui.screen.inventory.*;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTankTileEntityRenderer;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTraderTileEntityRenderer;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.core.ModTileEntities;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy{

	@Override
	public void setupClient()
	{
		
		//Set Render Layers
		RenderTypeLookup.setRenderLayer(ModBlocks.FLUID_TAP.block, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.FLUID_TAP_BUNDLE.block, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.IRON_TANK.block, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.GOLD_TANK.block, RenderType.getCutout());
		
		//Register Screens
		ScreenManager.registerFactory(ModContainers.FLUID_TRADER, FluidTraderScreen::new);
		ScreenManager.registerFactory(ModContainers.FLUID_TRADER_CR, FluidTraderScreen::new);
		ScreenManager.registerFactory(ModContainers.UNIVERSAL_FLUID_TRADER, FluidTraderScreen::new);
		
		ScreenManager.registerFactory(ModContainers.FLUID_TRADER_STORAGE, FluidTraderStorageScreen::new);
		ScreenManager.registerFactory(ModContainers.UNIVERSAL_FLUID_TRADER_STORAGE, FluidTraderStorageScreen::new);
		
		ScreenManager.registerFactory(ModContainers.FLUID_EDIT, FluidEditScreen::new);
		ScreenManager.registerFactory(ModContainers.UNIVERSAL_FLUID_EDIT, FluidEditScreen::new);
		
		ScreenManager.registerFactory(ModContainers.ENERGY_TRADER, EnergyTraderScreen::new);
		ScreenManager.registerFactory(ModContainers.ENERGY_TRADER_CR, EnergyTraderScreen::new);
		ScreenManager.registerFactory(ModContainers.ENERGY_TRADER_UNIVERSAL, EnergyTraderScreen::new);
		ScreenManager.registerFactory(ModContainers.ENERGY_TRADER_STORAGE, EnergyTraderStorageScreen::new);
		ScreenManager.registerFactory(ModContainers.ENERGY_TRADER_STORAGE_UNIVERSAL, EnergyTraderStorageScreen::new);
		
		//Register Tile Entity Renderers
		ClientRegistry.bindTileEntityRenderer(ModTileEntities.FLUID_TRADER, FluidTraderTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ModTileEntities.FLUID_TANK, FluidTankTileEntityRenderer::new);
		
		
	}
	
}
