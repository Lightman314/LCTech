package io.github.lightman314.lctech.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.tileentities.FluidTankTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankTileEntityRenderer extends TileEntityRenderer<FluidTankTileEntity>{

	public FluidTankTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
	}
	
	@Override
	public void render(FluidTankTileEntity tileEntity, float partialTicket, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, int overlay)
	{
		
		FluidStack tank = tileEntity.getTankContents();
		if(!tank.isEmpty())
		{
			FluidRenderData renderData = tileEntity.getRenderPosition();
			if(renderData != null)
			{
				renderData.setFillPercent((float)tileEntity.getTankFillPercent());
				FluidRenderUtil.drawFluidInWorld(tank, tileEntity.getWorld(), tileEntity.getPos(), matrixStack, renderTypeBuffer, renderData, light);
			}
		}
		
	}
	
}
