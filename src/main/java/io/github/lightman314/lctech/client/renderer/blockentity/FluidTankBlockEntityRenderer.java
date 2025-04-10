package io.github.lightman314.lctech.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidTankBlockEntityRenderer implements BlockEntityRenderer<FluidTankBlockEntity>{

	public FluidTankBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) { }
	
	@Override
	public void render(FluidTankBlockEntity tileEntity, float partialTicket, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int light, int overlay)
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
