package io.github.lightman314.lctech.client.renderer.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.fluids.FluidStack;

public class FluidTraderTileEntityRenderer implements BlockEntityRenderer<FluidTraderTileEntity>{

	public FluidTraderTileEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		
	}
	
	@Override
	public void render(FluidTraderTileEntity tileEntity, float partialTicket, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay)
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
					FluidRenderUtil.drawFluidInWorld(tank, tileEntity.getLevel(), tileEntity.getBlockPos(), poseStack, bufferSource, renderData, light);
				}
			}
		}
		
	}
	
	
	
	
	
}
