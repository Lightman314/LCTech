package io.github.lightman314.lctech.common.blockentities.trader;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class EnergyTraderBlockEntity extends TraderBlockEntity<EnergyTraderData> {

	protected boolean networkTrader;
	
	public EnergyTraderBlockEntity(BlockPos pos, BlockState state) { this(pos, state, false); }
	public EnergyTraderBlockEntity(BlockPos pos, BlockState state, boolean networkTrader) {
		super(ModBlockEntities.ENERGY_TRADER.get(), pos, state);
		this.networkTrader = networkTrader;
	}
	
	@Nonnull
	public EnergyTraderData buildNewTrader() {
		EnergyTraderData trader = new EnergyTraderData(this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@Override
	public void saveAdditional(@NotNull CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void load(@NotNull CompoundTag compound)
	{
		super.load(compound);
		this.networkTrader = compound.getBoolean("NetworkTrader");
	}
	
	@Override
	public AABB getRenderBoundingBox()
	{
		if(this.getBlockState() != null)
			return this.getBlockState().getCollisionShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
		return super.getRenderBoundingBox();
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
					
					BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(actualSide));
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