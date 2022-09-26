package io.github.lightman314.lctech.proxy;

import io.github.lightman314.lctech.client.gui.widget.FluidEditWidget;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTankTileEntityRenderer;
import io.github.lightman314.lctech.client.renderer.tileentity.FluidTraderBlockEntityRenderer;
import io.github.lightman314.lctech.core.ModBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ClientProxy extends CommonProxy{

	@Override
	public void setupClient()
	{
		
		//Register Tile Entity Renderers
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TRADER.get(), FluidTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_TANK.get(), FluidTankTileEntityRenderer::new);
		
		FluidEditWidget.initFluidList();
		
	}
	
}