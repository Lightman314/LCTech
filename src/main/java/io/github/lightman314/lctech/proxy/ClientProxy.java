package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTankTileEntityRenderer;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTraderBlockEntityRenderer;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.core.ModBlockEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientProxy extends CommonProxy{

	@Override
	public void setupClient()
	{
		
		//Set Render Layers
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_TAP.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_TAP_BUNDLE.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.IRON_TANK.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.GOLD_TANK.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIAMOND_TANK.get(), RenderType.cutout());
		
		//Register Tile Entity Renderers
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TRADER.get(), FluidTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TANK.get(), FluidTankTileEntityRenderer::new);
		
	}
	
	@SubscribeEvent
	public void onLogin(ClientPlayerNetworkEvent.LoggedInEvent event)
	{
		//Initialize the fluid edit widgets fluid list
		FluidEditWidget.initFluidList();
	}
	
}
