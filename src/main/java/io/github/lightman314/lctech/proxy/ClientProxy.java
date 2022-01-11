package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.client.gui.screen.inventory.*;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTankTileEntityRenderer;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTraderTileEntityRenderer;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.core.ModBlockEntities;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ClientProxy extends CommonProxy{

	@Override
	public void setupClient()
	{
		
		//Set Render Layers
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_TAP.block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_TAP_BUNDLE.block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.IRON_TANK.block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.GOLD_TANK.block, RenderType.cutout());
		
		//Register Screens
		MenuScreens.register(ModContainers.FLUID_TRADER, FluidTraderScreen::new);
		MenuScreens.register(ModContainers.FLUID_TRADER_STORAGE, FluidTraderStorageScreen::new);
		MenuScreens.register(ModContainers.FLUID_TRADER_CR, FluidTraderScreenCR::new);
		MenuScreens.register(ModContainers.FLUID_EDIT, FluidEditScreen::new);
		MenuScreens.register(ModContainers.UNIVERSAL_FLUID_TRADER, UniversalFluidTraderScreen::new);
		MenuScreens.register(ModContainers.UNIVERSAL_FLUID_TRADER_STORAGE, UniversalFluidTraderStorageScreen::new);
		MenuScreens.register(ModContainers.UNIVERSAL_FLUID_EDIT, FluidEditScreen::new);
		
		//Register Tile Entity Renderers
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TRADER, FluidTraderTileEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TANK, FluidTankTileEntityRenderer::new);
		
		
	}
	
}
