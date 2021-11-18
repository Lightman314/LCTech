package io.github.lightman314.lctech.blocks;

import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import net.minecraft.block.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IFluidTraderBlock extends ITraderBlock{
	
	@OnlyIn(Dist.CLIENT)
	public int getTradeRenderLimit();
	
	@OnlyIn(Dist.CLIENT)
	public FluidRenderData getRenderPosition(BlockState state, int index);
}
