package io.github.lightman314.lctech.common.blockentities.trader;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyTraderBlockEntity extends TraderBlockEntity<EnergyTraderData> {

	protected boolean networkTrader;
	
	public EnergyTraderBlockEntity(BlockPos pos, BlockState state) { this(pos, state, false); }
	public EnergyTraderBlockEntity(BlockPos pos, BlockState state, boolean networkTrader) {
		super(ModBlockEntities.ENERGY_TRADER.get(), pos, state);
		this.networkTrader = networkTrader;
	}

	@Nullable
	@Override
	protected EnergyTraderData castOrNullify(@Nonnull TraderData traderData) {
		if(traderData instanceof EnergyTraderData et)
			return et;
		return null;
	}

	@Nonnull
	public EnergyTraderData buildNewTrader() {
		EnergyTraderData trader = new EnergyTraderData(this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@Override
	public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		super.saveAdditional(compound,lookup);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		super.loadAdditional(compound,lookup);
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}
	
	@Override
	public void serverTick() {
		super.serverTick();
		
		EnergyTraderData trader = this.getTraderData();
		if(trader != null && trader.canDrainExternally() && trader.getDrainableEnergy() > 0)
		{
			for(Direction relativeSide : Direction.values())
			{
				if(trader.allowOutputSide(relativeSide) && trader.getDrainableEnergy() > 0)
				{
					//LCTech.LOGGER.debug("Attempting to EXPORT energy from an energy trader.\nAvailable Energy: " + trader.getTotalEnergy() + "\nDrainable Energy: " + trader.getDrainableEnergy());
					Direction actualSide = relativeSide;
					if(this.getBlockState().getBlock() instanceof IRotatableBlock b)
						actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);

					IEnergyStorage energyHandler = this.level.getCapability(Capabilities.EnergyStorage.BLOCK, this.worldPosition.relative(actualSide), actualSide.getOpposite());
					if(energyHandler != null)
					{
						int extractedAmount = energyHandler.receiveEnergy(trader.getDrainableEnergy(), false);
						if(extractedAmount > 0)
						{
							//LCTech.LOGGER.debug("Exporting " + extractedAmount + " energy from the trader.");
							if(trader.isPurchaseDrainMode()) //Only shrink pending drain if in purchase mode.
								trader.shrinkPendingDrain(extractedAmount);
							trader.shrinkEnergy(extractedAmount);
							trader.markEnergyStorageDirty();
						}
					}
				}
			}
		}
	}
	
}
