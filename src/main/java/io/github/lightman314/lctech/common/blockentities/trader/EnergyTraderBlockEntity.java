package io.github.lightman314.lctech.common.blockentities.trader;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.energy.CapabilityEnergy;

public class EnergyTraderBlockEntity extends TraderBlockEntity<EnergyTraderData> {

	protected boolean networkTrader = false;
	
	public EnergyTraderBlockEntity(BlockPos pos, BlockState state) { this(pos, state, false); }
	public EnergyTraderBlockEntity(BlockPos pos, BlockState state, boolean networkTrader) {
		super(ModBlockEntities.ENERGY_TRADER.get(), pos, state);
		this.networkTrader = networkTrader;
	}
	
	public EnergyTraderData buildNewTrader() {
		EnergyTraderData trader = new EnergyTraderData(this.level, this.worldPosition);
		if(this.networkTrader)
			trader.setAlwaysShowOnTerminal();
		return trader;
	}
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putBoolean("NetworkTrader", this.networkTrader);
	}
	
	@Override
	public void load(CompoundTag compound)
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
	
	@Override @Deprecated
	protected EnergyTraderData createTraderFromOldData(CompoundTag compound) {
		EnergyTraderData newTrader = new EnergyTraderData(this.level, this.worldPosition);
		newTrader.loadOldBlockEntityData(compound);
		return newTrader;
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
					Direction actualSide = relativeSide;
					if(this.getBlockState().getBlock() instanceof IRotatableBlock)
					{
						IRotatableBlock b = (IRotatableBlock)this.getBlockState().getBlock();
						actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);
					}
					
					BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(actualSide));
					if(be != null)
					{
						be.getCapability(CapabilityEnergy.ENERGY, actualSide.getOpposite()).ifPresent(energyHandler -> {
							int extractedAmount = energyHandler.receiveEnergy(trader.getDrainableEnergy(), false);
							if(extractedAmount > 0)
							{
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