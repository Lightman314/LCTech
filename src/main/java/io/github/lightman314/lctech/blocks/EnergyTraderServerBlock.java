package io.github.lightman314.lctech.blocks;

import io.github.lightman314.lctech.blockentities.UniversalEnergyTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.networktraders.templates.NetworkTraderBlock;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyTraderServerBlock extends NetworkTraderBlock {
	
	public EnergyTraderServerBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new UniversalEnergyTraderBlockEntity(pos, state);
	}
	
	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, UniversalTraderData data) { }

}
