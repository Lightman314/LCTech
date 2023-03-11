package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTankTileEntityRenderer;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTraderBlockEntityRenderer;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy{

	@Override
	public void setupClient()
	{

		//Set Render Layers
		RenderTypeLookup.setRenderLayer(ModBlocks.FLUID_TAP.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.FLUID_TAP_BUNDLE.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.IRON_TANK.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.GOLD_TANK.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.DIAMOND_TANK.get(), RenderType.cutout());
		
		//Register Tile Entity Renderers
		ClientRegistry.bindTileEntityRenderer(ModBlockEntities.FLUID_TRADER.get(), FluidTraderBlockEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ModBlockEntities.FLUID_TANK.get(), FluidTankTileEntityRenderer::new);
		
	}
	
	@SubscribeEvent
	public void onLogin(ClientPlayerNetworkEvent.LoggedInEvent event)
	{
		//Initialize the fluid edit widgets fluid list
		FluidEditWidget.initFluidList();
	}

	@Override
	public void handleTankStackPacket(TankStackCache.PacketBuilder data) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.level != null)
		{
			data.build(mc.level).init(false);
		}
	}
	
}
