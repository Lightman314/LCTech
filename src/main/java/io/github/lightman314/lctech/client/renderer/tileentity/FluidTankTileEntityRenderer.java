package io.github.lightman314.lctech.client.renderer.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.tileentities.FluidTankTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankTileEntityRenderer implements BlockEntityRenderer<FluidTankTileEntity>{

	public FluidTankTileEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		
	}
	
	@Override
	public void render(FluidTankTileEntity tileEntity, float partialTicket, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay)
	{
		
		FluidStack tank = tileEntity.getTankContents();
		if(!tank.isEmpty())
		{
			FluidRenderData renderData = tileEntity.getRenderPosition();
			if(renderData != null)
			{
				renderData.setFillPercent((float)tileEntity.getTankFillPercent());
				FluidRenderUtil.drawFluidInWorld(tank, tileEntity.getLevel(), tileEntity.getBlockPos(), poseStack, bufferSource, renderData, light);
			}
		}
		
	}
	
}
