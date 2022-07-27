package io.github.lightman314.lctech.blockentities;

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalEnergyTraderBlockEntity extends UniversalTraderBlockEntity{

	public UniversalEnergyTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.UNIVERSAL_ENERGY_TRADER.get(), pos, state);
	}

	@Override
	protected UniversalTraderData createInitialData(Entity owner) {
		return new UniversalEnergyTraderData(PlayerReference.of(owner), this.worldPosition, this.level.dimension(), this.getTraderID());
	}
	
}
