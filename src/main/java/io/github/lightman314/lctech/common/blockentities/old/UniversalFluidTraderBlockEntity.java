package io.github.lightman314.lctech.common.blockentities.old;

import java.util.UUID;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blockentities.trader.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.old.OldBlockEntity;
import io.github.lightman314.lightmanscurrency.common.data_updating.DataConverter;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public class UniversalFluidTraderBlockEntity extends OldBlockEntity{
	
	public UniversalFluidTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.UNIVERSAL_FLUID_TRADER.get(), pos, state);
	}
	
	@Override
	protected BlockEntity createReplacement(CompoundTag compound) {
		UUID uuid = compound.getUUID("ID");
		long newID = DataConverter.getNewTraderID(uuid);
		TraderData trader = TraderSaveData.GetTrader(false, newID);
		FluidTraderBlockEntity newBE = new FluidTraderBlockEntity(this.worldPosition, this.getBlockState(), trader != null ? trader.getTradeCount() : 1, true);
		newBE.setTraderID(newID);
		LCTech.LOGGER.info("Successfully converted UniversalFluidTraderBlockEntity into a FluidTraderBlockEntity at " + this.worldPosition.toShortString() + "\nOld ID: " + uuid + "\nNew ID: " + newID);
		return newBE;
	}
	
}
