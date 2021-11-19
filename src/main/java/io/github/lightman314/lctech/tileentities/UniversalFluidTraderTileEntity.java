package io.github.lightman314.lctech.tileentities;

import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.core.ModTileEntities;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

public class UniversalFluidTraderTileEntity extends UniversalTraderTileEntity{

	int tradeCount = 1;
	public UniversalFluidTraderTileEntity()
	{
		super(ModTileEntities.UNIVERSAL_FLUID_TRADER);
	}
	
	public UniversalFluidTraderTileEntity(int tradeCount)
	{
		this();
		this.tradeCount = tradeCount;
	}

	@Override
	protected UniversalTraderData createInitialData(Entity owner) {
		return new UniversalFluidTraderData(owner, this.pos, this.world.getDimensionKey(), this.getTraderID(), this.tradeCount);
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
					Block.spawnAsEntity(world, pos, FluidShardItem.GetFluidShard(trade.getTankContents()));
			}
			//Dump upgrade data
			for(int i = 0; i < fluidData.getUpgradeInventory().getSizeInventory(); i++)
			{
				if(!fluidData.getUpgradeInventory().getStackInSlot(i).isEmpty())
					Block.spawnAsEntity(world, pos, fluidData.getUpgradeInventory().getStackInSlot(i));
			}
			
		}
	}
	
}
