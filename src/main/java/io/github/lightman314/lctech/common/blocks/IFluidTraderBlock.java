package io.github.lightman314.lctech.common.blocks;

import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IFluidTraderBlock extends ITraderBlock{
	
	@OnlyIn(Dist.CLIENT)
	int getTradeRenderLimit();
	
	@OnlyIn(Dist.CLIENT)
	FluidRenderData getRenderPosition(BlockState state, int index);
}
