package io.github.lightman314.lctech.blocks.traderblocks;

import io.github.lightman314.lctech.blockentities.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockRotatable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnergyTraderBlock extends TraderBlockRotatable {
	
	public EnergyTraderBlock(Properties properties)
	{
		super(properties);
	}
	
	public EnergyTraderBlock(Properties properties, VoxelShape shape)
	{
		super(properties, shape);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if(this.shouldMakeTrader(state))
			return this.makeTrader(pos, state);
		return new DummyBlockEntity(pos, state);
	}

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) {
		return new EnergyTraderBlockEntity(pos, state);
	}

	@Override
	protected BlockEntityType<?> traderType() {
		return ModBlockEntities.ENERGY_TRADER;
	}
	

}
