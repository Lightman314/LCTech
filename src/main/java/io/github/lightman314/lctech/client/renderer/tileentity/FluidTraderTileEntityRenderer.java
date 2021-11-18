package io.github.lightman314.lctech.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fluids.FluidStack;

public class FluidTraderTileEntityRenderer extends TileEntityRenderer<FluidTraderTileEntity>{

	public FluidTraderTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
	}
	
	@Override
	public void render(FluidTraderTileEntity tileEntity, float partialTicket, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, int overlay)
	{
		
		for(int tradeSlot = 0; tradeSlot < tileEntity.getTradeCount() && tradeSlot < tileEntity.getTradeRenderLimit(); tradeSlot ++)
		{
			FluidTradeData trade = tileEntity.getTrade(tradeSlot);
			if(!trade.getTankContents().isEmpty())
			{
				FluidStack tank = trade.getTankContents();
				FluidRenderData renderData = tileEntity.getRenderPosition(tradeSlot);
				if(renderData != null)
				{
					renderData.setFillPercent((float)trade.getTankFillPercent());
					FluidRenderUtil.drawFluidInWorld(tank, tileEntity.getWorld(), tileEntity.getPos(), matrixStack, renderTypeBuffer, renderData, light);
				}
			}
		}
		
	}
	
	
	
	
	
}
