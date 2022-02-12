package io.github.lightman314.lctech.tileentities;

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.tileentity.UniversalTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;

public class UniversalEnergyTraderTileEntity extends UniversalTraderTileEntity{

	public UniversalEnergyTraderTileEntity()
	{
		super(ModTileEntities.UNIVERSAL_ENERGY_TRADER);
	}

	@Override
	protected UniversalTraderData createInitialData(PlayerEntity owner) {
		return new UniversalEnergyTraderData(PlayerReference.of(owner), this.pos, this.world.getDimensionKey(), this.getTraderID());
	}
	
	@Override
	protected void dumpContents(UniversalTraderData data)
	{
		super.dumpContents(data);
		if(data instanceof UniversalEnergyTraderData)
		{
			UniversalEnergyTraderData energyData = (UniversalEnergyTraderData)data;
			
			//Dump upgrade data
			for(int i = 0; i < energyData.getUpgradeInventory().getSizeInventory(); i++)
			{
				if(!energyData.getUpgradeInventory().getStackInSlot(i).isEmpty())
					Block.spawnAsEntity(this.world, this.pos, energyData.getUpgradeInventory().getStackInSlot(i));
			}
			
		}
	}
	
}
