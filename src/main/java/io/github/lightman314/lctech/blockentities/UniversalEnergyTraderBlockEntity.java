package io.github.lightman314.lctech.blockentities;

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalEnergyTraderBlockEntity extends UniversalTraderBlockEntity{

	public UniversalEnergyTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.UNIVERSAL_FLUID_TRADER, pos, state);
	}

	@Override
	protected UniversalTraderData createInitialData(Entity owner) {
		return new UniversalEnergyTraderData(PlayerReference.of(owner), this.worldPosition, this.level.dimension(), this.getTraderID());
	}
	
	@Override
	protected void dumpContents(UniversalTraderData data)
	{
		super.dumpContents(data);
		if(data instanceof UniversalEnergyTraderData)
		{
			UniversalEnergyTraderData energyData = (UniversalEnergyTraderData)data;
			
			//Dump upgrade data
			for(int i = 0; i < energyData.getUpgradeInventory().getContainerSize(); i++)
			{
				if(!energyData.getUpgradeInventory().getItem(i).isEmpty())
					Block.popResource(this.level, this.worldPosition, energyData.getUpgradeInventory().getItem(i));
			}
			
		}
	}
	
}
