package io.github.lightman314.lctech.tileentities;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.core.ModTileEntities;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalFluidTraderTileEntity extends UniversalTraderBlockEntity{

	int tradeCount = 1;
	public UniversalFluidTraderTileEntity(BlockPos pos, BlockState state)
	{
		super(ModTileEntities.UNIVERSAL_FLUID_TRADER, pos, state);
	}
	
	public UniversalFluidTraderTileEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		this(pos, state);
		this.tradeCount = tradeCount;
	}

	@Override
	protected UniversalTraderData createInitialData(Entity owner) {
		return new UniversalFluidTraderData(owner, this.worldPosition, this.level.dimension(), this.getTraderID(), this.tradeCount);
	}
	
	@Override
	protected void dumpContents(UniversalTraderData data)
	{
		super.dumpContents(data);
		if(data instanceof UniversalFluidTraderData)
		{
			UniversalFluidTraderData fluidData = (UniversalFluidTraderData)data;
			//Dump tank contents as a shard
			for(int i = 0; i < fluidData.getTradeCount(); i++)
			{
				FluidTradeData trade = fluidData.getTrade(i);
				if(!trade.getTankContents().isEmpty())
					Block.popResource(this.level, this.worldPosition, FluidShardItem.GetFluidShard(trade.getTankContents()));
			}
			//Dump upgrade data
			for(int i = 0; i < fluidData.getUpgradeInventory().getContainerSize(); i++)
			{
				if(!fluidData.getUpgradeInventory().getItem(i).isEmpty())
					Block.popResource(this.level, this.worldPosition, fluidData.getUpgradeInventory().getItem(i));
			}
			
		}
	}
	
}
