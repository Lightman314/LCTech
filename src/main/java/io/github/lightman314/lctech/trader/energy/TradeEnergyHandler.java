package io.github.lightman314.lctech.trader.energy;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lctech.util.EnergyUtil.EnergyActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;

public class TradeEnergyHandler {

	final IEnergyTrader trader;
	final Map<Direction,IEnergyStorage> externalHandlers = new HashMap<>();
	final IEnergyStorage batteryInteractable;
	final IEnergyStorage tradeExecutor;
	
	public TradeEnergyHandler(IEnergyTrader trader)
	{
		this.trader = trader;
		this.batteryInteractable = new BatteryInteractionEnergyHandler(trader);
		this.tradeExecutor = new TradeExecutionEnergyHandler(trader);
	}
	
	/**
	 * Internal handler is for use by trade executions & player storage interactions.
	 */
	public IEnergyStorage getTradeExecutor() { return this.tradeExecutor; }
	
	/**
	 * Handler used for external interactions
	 */
	public IEnergyStorage getHandler(Direction relativeDirection)
	{
		//Return null if both the input & output are disabled for that side.
		if(!this.trader.getEnergySettings().getInputSides().get(relativeDirection) && !this.trader.getEnergySettings().getOutputSides().get(relativeDirection))
			return null;
		//Otherwise, return the handler for that side
		if(!this.externalHandlers.containsKey(relativeDirection)) //Create new handler for the requested side, if one doesn't exist
			this.externalHandlers.put(relativeDirection, new ExternalEnergyHandler(this.trader, relativeDirection));
		return this.externalHandlers.get(relativeDirection);
	}
	
	public static class ExternalEnergyHandler implements IEnergyStorage
	{
		
		protected final IEnergyTrader trader;
		protected final Direction relativeDirection;
		public final boolean isCreative() { return this.trader.getCoreSettings().isCreative(); }
	
		public ExternalEnergyHandler(IEnergyTrader trader, Direction relativeDirection) {
			this.trader = trader;
			this.relativeDirection = relativeDirection;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if(!this.canReceive())
				return 0;
			int receiveAmount = Math.min(maxReceive, Math.max(0, this.trader.getMaxEnergy() - this.trader.getTotalEnergy()));
			if(!simulate && receiveAmount > 0)
			{
				//Add the energy
				this.trader.addEnergy(receiveAmount);
				//Mark energy storage dirty
				this.trader.markEnergyStorageDirty();
			}
			return receiveAmount;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if(!this.canExtract())
				return 0;
			int extractAmount = Math.min(maxExtract, this.isCreative() ? this.trader.getPendingDrain() : Math.min(this.trader.getPendingDrain(), this.trader.getTotalEnergy()));
			if(!simulate && extractAmount > 0)
			{
				if(!this.isCreative())
				{
					//Drain the energy from the trader
					this.trader.shrinkEnergy(extractAmount);
				}
				//Shrink the pending drain
				this.trader.shrinkPendingDrain(extractAmount);
				//Mark energy storage dirty
				this.trader.markEnergyStorageDirty();
				
			}
			
			return extractAmount;
		}

		@Override
		public int getEnergyStored() {
			return this.trader.getTotalEnergy();
		}

		@Override
		public int getMaxEnergyStored() {
			return this.trader.getMaxEnergy();
		}

		@Override
		public boolean canExtract() {
			return this.trader.getEnergySettings().getOutputSides().get(this.relativeDirection);
		}

		@Override
		public boolean canReceive() {
			return this.trader.getEnergySettings().getInputSides().get(this.relativeDirection);
		}
		
	}
	
	public ItemStack batteryInteraction(ItemStack batteryStack)
	{
		EnergyActionResult result = EnergyUtil.tryEmptyContainer(batteryStack, this.batteryInteractable, Integer.MAX_VALUE, true);
		if(result.success())
		{
			this.trader.markEnergyStorageDirty();
			return result.getResult();
		}
		else
		{
			result = EnergyUtil.tryFillContainer(batteryStack, this.batteryInteractable, Integer.MAX_VALUE, true);
			if(result.success())
			{
				this.trader.markEnergyStorageDirty();
				return result.getResult();
			}
		}
		return batteryStack;
	}
	
	private static class BatteryInteractionEnergyHandler implements IEnergyStorage
	{
		protected final IEnergyTrader trader;
		
		public BatteryInteractionEnergyHandler(IEnergyTrader trader) { this.trader = trader; }

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int receiveAmount = Math.min(maxReceive, this.trader.getMaxEnergy() - this.trader.getTotalEnergy());
			if(!simulate)
			{
				//Add the energy to storage
				this.trader.addEnergy(receiveAmount);
				//Mark the energy storage dirty
				this.trader.markEnergyStorageDirty();
			}
			return receiveAmount;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int extractAmount = Math.min(maxExtract, this.trader.getAvailableEnergy());
			if(!simulate)
			{
				//Remove the energy from storage
				this.trader.shrinkEnergy(extractAmount);
				//Mark the energy storage dirty
				this.trader.markEnergyStorageDirty();
			}
			return extractAmount;
		}

		@Override
		public int getEnergyStored() {
			return this.trader.getAvailableEnergy();
		}

		@Override
		public int getMaxEnergyStored() {
			return this.trader.getMaxEnergy();
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
		
	}
	
	private static class TradeExecutionEnergyHandler implements IEnergyStorage
	{
		protected final IEnergyTrader trader;
		public final boolean isCreative() { return this.trader.getCoreSettings().isCreative(); }
		
		public TradeExecutionEnergyHandler(IEnergyTrader trader) { this.trader = trader; }

		//Trade Execution Energy Handler always assumes that enough energy is in storage to receive/extract the energy
		
		@Override
		public int receiveEnergy(int amount, boolean simulate) {
			if(!simulate && !this.isCreative())
			{
				//Add the energy to storage
				this.trader.addEnergy(amount);
				//Mark the energy storage dirty
				this.trader.markEnergyStorageDirty();
			}
			return amount;
		}

		@Override
		public int extractEnergy(int amount, boolean simulate) {
			if(!simulate && !this.isCreative())
			{
				//Remove the energy from storage
				this.trader.shrinkEnergy(amount);
				//Mark the energy storage dirty
				this.trader.markEnergyStorageDirty();
			}
			return amount;
		}

		@Override
		public int getEnergyStored() {
			return this.trader.getTotalEnergy();
		}

		@Override
		public int getMaxEnergyStored() {
			return this.trader.getMaxEnergy();
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
		
	}
	
}
