package io.github.lightman314.lctech.common.blocks;

import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IFluidTraderBlock extends ITraderBlock {
	
	@OnlyIn(Dist.CLIENT)
	int getTradeRenderLimit();
	
	@OnlyIn(Dist.CLIENT)
	FluidRenderData getRenderPosition(BlockState state, int index);

}
