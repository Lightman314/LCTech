package io.github.lightman314.lctech.blocks;

import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IFluidTraderBlock extends ITraderBlock{
	
	@OnlyIn(Dist.CLIENT)
	public int getTradeRenderLimit();
	
	@OnlyIn(Dist.CLIENT)
	public FluidRenderData getRenderPosition(BlockState state, int index);
}
