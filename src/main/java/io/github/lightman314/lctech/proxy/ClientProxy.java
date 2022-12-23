package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTankTileEntityRenderer;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTraderBlockEntityRenderer;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

public class ClientProxy extends CommonProxy{

	@Override
	public void setupClient()
	{
		//Register Tile Entity Renderers
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TRADER.get(), FluidTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TANK.get(), FluidTankTileEntityRenderer::new);
	}
	
	@SubscribeEvent
	public void onLogin(ClientPlayerNetworkEvent.LoggingIn event)
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