package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.client.renderer.tileentity.FluidTankTileEntityRenderer;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTraderBlockEntityRenderer;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.core.ModBlockEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ClientProxy extends CommonProxy{

	
	@Override
	@SuppressWarnings("removal")
	public void setupClient()
	{
		
		//Set Render Layers
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_TAP.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_TAP_BUNDLE.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.IRON_TANK.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.GOLD_TANK.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIAMOND_TANK.get(), RenderType.cutout());
		
		//Register Screens
		/*MenuScreens.register(ModMenus.FLUID_TRADER, FluidTraderScreen::new);
		MenuScreens.register(ModMenus.FLUID_TRADER_CR, FluidTraderScreen::new);
		MenuScreens.register(ModMenus.UNIVERSAL_FLUID_TRADER, FluidTraderScreen::new);
		
		MenuScreens.register(ModMenus.FLUID_TRADER_STORAGE, FluidTraderStorageScreen::new);
		MenuScreens.register(ModMenus.UNIVERSAL_FLUID_TRADER_STORAGE, FluidTraderStorageScreen::new);
		
		MenuScreens.register(ModMenus.FLUID_EDIT, FluidEditScreen::new);
		MenuScreens.register(ModMenus.UNIVERSAL_FLUID_EDIT, FluidEditScreen::new);
		
		MenuScreens.register(ModMenus.ENERGY_TRADER, EnergyTraderScreen::new);
		MenuScreens.register(ModMenus.ENERGY_TRADER_CR, EnergyTraderScreen::new);
		MenuScreens.register(ModMenus.ENERGY_TRADER_UNIVERSAL, EnergyTraderScreen::new);
		MenuScreens.register(ModMenus.ENERGY_TRADER_STORAGE, EnergyTraderStorageScreen::new);
		MenuScreens.register(ModMenus.ENERGY_TRADER_STORAGE_UNIVERSAL, EnergyTraderStorageScreen::new);*/
		
		//Register Tile Entity Renderers
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TRADER.get(), FluidTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TANK.get(), FluidTankTileEntityRenderer::new);
		
	}
	
}
