package io.github.lightman314.lctech.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidTankTileEntityRenderer extends TileEntityRenderer<FluidTankBlockEntity> {

	public FluidTankTileEntityRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); }

	@Override
	public void render(FluidTankBlockEntity tileEntity, float partialTicks, @Nonnull MatrixStack pose, @Nonnull IRenderTypeBuffer buffer, int lightLevel, int id)
	{
		FluidStack tank = tileEntity.getTankContents();
		if(!tank.isEmpty())
		{
			FluidRenderData renderData = tileEntity.getRenderPosition();
			if(renderData != null)
			{
				renderData.setFillPercent((float)tileEntity.getTankFillPercent());
				FluidRenderUtil.drawFluidInWorld(tank, tileEntity.getLevel(), tileEntity.getBlockPos(), pose, buffer, renderData, lightLevel);
			}
		}


	}

}