package io.github.lightman314.lctech.blockentities.old;

import java.util.UUID;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.trader.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.old.OldBlockEntity;
import io.github.lightman314.lightmanscurrency.common.data_updating.DataConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public class UniversalEnergyTraderBlockEntity extends OldBlockEntity{

	public UniversalEnergyTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.UNIVERSAL_ENERGY_TRADER.get(), pos, state);
	}

	@Override
	protected BlockEntity createReplacement(CompoundTag compound) {
		UUID uuid = compound.getUUID("ID");
		long newID = DataConverter.getNewTraderID(uuid);
		EnergyTraderBlockEntity newBE = new EnergyTraderBlockEntity(this.worldPosition, this.getBlockState(), true);
		newBE.setTraderID(newID);
		LCTech.LOGGER.info("Successfully converted UniversalEnergyTraderBlockEntity into a EnergyTraderBlockEntity at " + this.worldPosition.toShortString() + "\nOld ID: " + uuid + "\nNew ID: " + newID);
		return newBE;
	}
	
}