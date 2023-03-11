package io.github.lightman314.lctech.common.blockentities.trader;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyTraderBlockEntity extends TraderBlockEntity<EnergyTraderData> {

	protected boolean networkTrader;
	
	public EnergyTraderBlockEntity() { this(false); }
	public EnergyTraderBlockEntity( boolean networkTrader) {
		super(ModBlockEntities.ENERGY_TRADER.get());
		this.networkTrader = networkTrader;
	}
	
	public EnergyTraderData buildNewTrader() {
		EnergyTraderData trader = new EnergyTraderData(this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound)
	{
		compound = super.save(compound);
		compound.putBoolean("NetworkTrader", this.networkTrader);
		return compound;
	}
	
	@Override
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{
		super.load(state, compound);
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(this.getBlockState() != null)
			return this.getBlockState().getCollisionShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
		return super.getRenderBoundingBox();
	}
	
	@Override @Deprecated
	protected EnergyTraderData createTraderFromOldData(CompoundNBT compound) {
		EnergyTraderData newTrader = new EnergyTraderData(this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		return newTrader;
	}

	@Override
	protected void loadAsFormerNetworkTrader(@Nullable EnergyTraderData energyTraderData, CompoundNBT compoundNBT) {
		this.networkTrader = true;
		this.setChanged();
	}

	@Override
	public void tick() {
		super.tick();
		if(this.isClient())
			return;

		EnergyTraderData trader = this.getTraderData();
		if(trader != null && trader.canDrainExternally() && trader.getDrainableEnergy() > 0)
		{
			for(Direction relativeSide : Direction.values())
			{
				if(trader.allowOutputSide(relativeSide) && trader.getDrainableEnergy() > 0)
				{
					//LCTech.LOGGER.debug("Attempting to EXPORT energy from an energy trader.\nAvailable Energy: " + trader.getTotalEnergy() + "\nDrainable Energy: " + trader.getDrainableEnergy());
					Direction actualSide = relativeSide;
					if(this.getBlockState().getBlock() instanceof IRotatableBlock)
					{
						IRotatableBlock b = (IRotatableBlock)this.getBlockState().getBlock();
						actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);
					}
					
					TileEntity be = this.level.getBlockEntity(this.worldPosition.relative(actualSide));
					if(be != null)
					{
						be.getCapability(CapabilityEnergy.ENERGY, actualSide.getOpposite()).ifPresent(energyHandler -> {
							int extractedAmount = energyHandler.receiveEnergy(trader.getDrainableEnergy(), false);
							if(extractedAmount > 0)
							{
								//LCTech.LOGGER.debug("Exporting " + extractedAmount + " energy from the trader.");
								if(trader.isPurchaseDrainMode()) //Only shrink pending drain if in purchase mode.
									trader.shrinkPendingDrain(extractedAmount);
								trader.shrinkEnergy(extractedAmount);
								trader.markEnergyStorageDirty();
							}
						});
					}
				}
			}
		}
	}
	
}