package io.github.lightman314.lctech.blocks;

import io.github.lightman314.lctech.blockentities.UniversalFluidTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.networktraders.templates.NetworkTraderBlock;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTraderServerBlock extends NetworkTraderBlock {
	
	public static final int SMALL_SERVER_COUNT = 2;
	public static final int MEDIUM_SERVER_COUNT = 4;
	public static final int LARGE_SERVER_COUNT = 6;
	public static final int EXTRA_LARGE_SERVER_COUNT = 8;
	
	final int tradeCount;
	
	public FluidTraderServerBlock(int tradeCount, Properties properties)
	{
		super(properties);
		this.tradeCount = tradeCount;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new UniversalFluidTraderBlockEntity(pos, state, this.tradeCount);
	}

	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, UniversalTraderData data) { }
	

}
