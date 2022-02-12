package io.github.lightman314.lctech.trader.energy;

import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings.EnergyHandlerSettings;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lctech.util.EnergyUtil.EnergyActionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

public class TradeEnergyHandler {

	final IEnergyTrader trader;
	final IEnergyStorage inputOnly;
	final IEnergyStorage outputOnly;
	final IEnergyStorage inputAndOutput;
	final IEnergyStorage batteryInteractable;
	final IEnergyStorage tradeExecutor;
	
	public TradeEnergyHandler(IEnergyTrader trader)
	{
		this.trader = trader;
		this.inputOnly = new EnergyHandler(trader, true, false);
		this.outputOnly = new EnergyHandler(trader, false, true);
		this.inputAndOutput = new EnergyHandler(trader, true, true);
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
	public IEnergyStorage getHandler(EnergyHandlerSettings settings)
	{
		switch(settings) {
		case INPUT_ONLY:
			return this.inputOnly;
		case OUTPUT_ONLY:
			return this.outputOnly;
		case INPUT_AND_OUTPUT:
			return this.inputAndOutput;
			default:
				return null;
		}
	}
	
	private static class EnergyHandler implements IEnergyStorage
	{
		
		protected final IEnergyTrader trader;
		private final boolean allowInput;
		private final boolean allowOutput;
		public final boolean isCreative() { return this.trader.getCoreSettings().isCreative(); }
	
		public EnergyHandler(IEnergyTrader trader, boolean allowInput, boolean allowOutput) {
			this.trader = trader;
			this.allowInput = allowInput;
			this.allowOutput = allowOutput;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if(!this.allowInput)
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
			if(!this.allowOutput)
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
			return this.allowOutput;
		}

		@Override
		public boolean canReceive() {
			return this.allowInput;
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
