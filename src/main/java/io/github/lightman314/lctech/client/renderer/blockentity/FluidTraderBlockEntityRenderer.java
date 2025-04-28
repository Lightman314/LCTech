package io.github.lightman314.lctech.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.client.resourcepacks.data.model_variants.TechProperties;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidTraderBlockEntityRenderer implements BlockEntityRenderer<FluidTraderBlockEntity>{

	public FluidTraderBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) { }
	
	@Override
	public void render(FluidTraderBlockEntity blockEntity, float partialTicket, @Nonnull PoseStack pose, @Nonnull MultiBufferSource bufferSource, int light, int overlay)
	{
		FluidTraderData fluidTrader = blockEntity.getTraderData();
		if(fluidTrader != null)
		{
			ModelVariant variant = ModelVariantDataManager.getVariant(blockEntity.getCurrentVariant());
			TechProperties.FluidRenderDataList renderDataOverrides = null;
			if(variant != null && variant.has(TechProperties.FLUID_RENDER_DATA))
				renderDataOverrides = variant.get(TechProperties.FLUID_RENDER_DATA);
			for(int tradeSlot = 0; tradeSlot < fluidTrader.getTradeCount() && tradeSlot < blockEntity.getTradeRenderLimit(); tradeSlot ++)
			{
				FluidTradeData trade = fluidTrader.getTrade(tradeSlot);
				FluidStack fluid = trade.getProduct();
				if(!fluid.isEmpty())
				{
					int tankQuantity = fluidTrader.getStorage().getActualFluidCount(fluid);
					FluidRenderData renderData = blockEntity.getRenderPosition(tradeSlot);
					if(renderDataOverrides != null)
					{
						FluidRenderData newData = renderDataOverrides.get(blockEntity.getRenderPositionIndex(tradeSlot));
						if(newData != null)
							renderData = newData;
					}
					if(renderData != null && tankQuantity > 0)
					{
						renderData.setFillPercent((float)Math.min(1d, (double)tankQuantity/(double)fluidTrader.getTankCapacity()));
						FluidRenderUtil.drawFluidInWorld(fluid, blockEntity.getLevel(), blockEntity.getBlockPos(), pose, bufferSource, renderData, light);
					}
				}
			}
		}
	}
	
	
	
	
	
}
