package io.github.lightman314.lctech.trader.fluid;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TraderFluidStorage implements IFluidHandler {
	
	private final IFluidTrader trader;
	List<FluidEntry> tanks = new ArrayList<>();
	public List<FluidEntry> getContents() { return this.tanks; }
	
	public TraderFluidStorage(IFluidTrader trader) { this.trader = trader; }
	
	public CompoundTag save(CompoundTag compound, String tag) {
		ListTag list = new ListTag();
		for(int i = 0; i < this.tanks.size(); ++i)
		{
			FluidEntry tank = this.tanks.get(i);
			if(!tank.filter.isEmpty())
			{
				CompoundTag fluidTag = new CompoundTag();
				tank.save(fluidTag);
				list.add(fluidTag);
			}
		}
		compound.put(tag, list);
		return compound;
	}
	
	public void load(CompoundTag compound, String tag) {
		if(compound.contains(tag, Tag.TAG_LIST))
		{
			ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
			this.tanks.clear();
			for(int i = 0; i < list.size(); ++i)
			{
				CompoundTag fluidTag = list.getCompound(i);
				FluidEntry tank = FluidEntry.load(this, fluidTag);
				if(!tank.filter.isEmpty())
					this.tanks.add(tank);
			}
		}
	}
	
	public void loadFromTrades(ListTag fluidTradeList) {
		for(int i = 0; i < fluidTradeList.size(); ++i)
		{
			CompoundTag fluidTrade = fluidTradeList.getCompound(i);
			if(fluidTrade.contains("Tank"))
			{
				FluidStack tankContents = FluidStack.loadFluidStackFromNBT(fluidTrade.getCompound("Tank"));
				int pendingDrain = fluidTrade.contains("PendingDrain") ? fluidTrade.getInt("PendingDrain") : 0;
				if(!tankContents.isEmpty())
					this.tanks.add(new FluidEntry(this, tankContents, pendingDrain));
			}
		}
	}
	
	/**
	 * Gets whether the given fluid is allowed to be drained externally.
	 * If the only trades for this fluid are purchase trades, then this fluid will be drained by a pump automatically.
	 * If the only trades for this fluid are sales, then it will only allow drainage if there is a pending drain for this fluid
	 * (purchased without a tank in the bucket slot)
	 * If there are no trades for this fluid it will always be drainable.
	 */
	public boolean isDrainable(FluidStack fluid) {
		FluidEntry entry = this.getTank(fluid);
		return entry == null ? false : entry.drainable || !this.allowFluid(entry.filter);
	}
	
	/**
	 * Gets the actual tank contents of the given fluid, ignoring any pending drains.
	 */
	public int getActualFluidCount(FluidStack fluid) {
		FluidEntry entry = this.getTank(fluid);
		if(entry != null)
			return entry.getStoredAmount();
		return 0;
	}
	
	/**
	 * Gets the available tank contents of the given fluid, factoring in any pending drains the tank might have.
	 */
	public int getAvailableFluidCount(FluidStack fluid) {
		FluidEntry entry = this.getTank(fluid);
		if(entry != null)
			return entry.getStoredAmount() - entry.getPendingDrain();
		return 0;
	}
	
	/**
	 * Gets the pending drain amount of the given fluid.
	 */
	public int getPendingDrain(FluidStack fluid) {
		FluidEntry entry = this.getTank(fluid);
		if(entry != null)
			return entry.getPendingDrain();
		return 0;
	}
	
	/**
	 * Gets the tank intended for the given fluid.
	 * If no such tank exists, null will be returned.
	 */
	public FluidEntry getTank(FluidStack fluid) {
		for(FluidEntry entry : this.tanks)
		{
			if(entry.filter.isFluidEqual(fluid))
				return entry;
		}
		return null;
	}
	
	/**
	 * Returns the amount of the given fluid the storage can accept.
	 */
	public int getFillableAmount(FluidStack fluid) {
		if(!allowFluid(fluid))
			return 0;
		return Math.max(0, this.getTankCapacity() - this.getActualFluidCount(fluid));
	}
	
	/**
	 * Whether the given fluid is allowed to be stored.
	 */
	public boolean allowFluid(FluidStack fluid) {
		if(fluid.isEmpty())
			return false;
		for(FluidTradeData trade : this.trader.getAllTrades())
		{
			if(trade.getProduct().isFluidEqual(fluid))
				return true;
		}
		return false;
	}
	
	/**
	 * Call after a trades product is changed, so that we can remove any empty & invalid tanks, and create any new tanks for the new product.
	 */
	public void refactorTanks() {
		for(FluidTradeData trade : this.trader.getAllTrades())
		{
			if(!trade.getProduct().isEmpty())
			{
				FluidEntry entry = this.getTank(trade.getProduct());
				if(entry == null)
				{
					FluidEntry newEntry = new FluidEntry(this, trade.getProduct(), 0, 0, false, false);
					this.tanks.add(newEntry);
				}
			}
		}
		this.clearInvalidTanks();
	}
	
	/**
	 * Call after a tank is drained to see if we can remove a now-empty & invalid tank from the list of tanks.
	 */
	public void clearInvalidTanks() {
		for(int i = 0; i < this.tanks.size(); ++i) {
			FluidEntry entry = this.tanks.get(i);
			if(entry.filter.isEmpty())
			{
				this.tanks.remove(i);
				i--;
			}
			else if(entry.isEmpty() && !this.allowFluid(entry.filter))
			{
				this.tanks.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * Forcibly fills the fluid storage with the given fluid, ignoring the allowFluid check.
	 * Used internally in several places after proper checks have been made.
	 */
	public boolean forceFillTank(FluidStack fluid) {
		if(fluid.isEmpty())
			return true;
		FluidEntry entry = this.getTank(fluid);
		if(entry != null)
		{
			entry.addAmount(fluid.getAmount());
			return true;
		}
		if(this.tanks.size() >= 8)
			return false;
		this.tanks.add(new FluidEntry(this, fluid, 0));
		return true;
	}
	
	/**
	 * Drains the given fluid & amount from storage.
	 * Only run after confirming the given fluid/amount is actually present in storage via getAvailableFluidCount
	 */
	public void drain(FluidStack fluid) {
		FluidEntry entry = this.getTank(fluid);
		if(entry == null)
			return;
		entry.removeAmount(fluid.getAmount());
		return;
	}
	
	@Override
	public int getTanks() { return this.tanks.size(); }

	
	@Override
	public FluidStack getFluidInTank(int tank) {
		if(tank < this.tanks.size())
			return this.tanks.get(tank).getTankContents();
		return FluidStack.EMPTY;
	}

	@Override
	public int getTankCapacity(int tank) { return this.trader.getTankCapacity(); }
	
	public int getTankCapacity() { return this.trader.getTankCapacity(); }

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		if(tank < 0 || tank >= this.tanks.size())
			return false;
		return this.tanks.get(tank).filter.isFluidEqual(stack);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		/*int drainableAmount = Math.min(resource.getAmount(), this.getAvailableFluidCount(resource));
		if(drainableAmount <= 0)
			return FluidStack.EMPTY;
		FluidStack drainedFluid = resource.copy();
		drainedFluid.setAmount(drainableAmount);
		if(action.execute())
		{
			this.drain(drainedFluid);
			this.trader.markStorageDirty();
		}
		return drainedFluid;*/
		//Should never drain directly. Only use for filling.
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		//Should never drain directly.
		return FluidStack.EMPTY;
	}
	
	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if(!this.allowFluid(resource))
			return 0;
		int fillAmount = Math.min(resource.getAmount(), this.getFillableAmount(resource));
		if(fillAmount > 0 && action.execute())
		{
			FluidStack fillStack = resource.copy();
			fillStack.setAmount(fillAmount);
			this.forceFillTank(fillStack);
		}
		return fillAmount;
	}

	public static class FluidEntry implements IFluidHandler
	{
		private final TraderFluidStorage parent;
		public final FluidStack filter;
		private int storedAmount = 0;
		public int getStoredAmount() { return this.storedAmount; }
		public boolean isEmpty() { return this.storedAmount <= 0; }
		public void addAmount(int amount) { this.storedAmount += amount; }
		public void setAmount(int amount) { this.storedAmount = amount; }
		public void removeAmount(int amount) { this.storedAmount -= amount; }
		/**
		 * Gets a copy of the tanks contents.
		 * Note this will not be a proper reference to the tank's contents, so any modifications made to the fluid stack will not change the tanks actual contents.
		 */
		public FluidStack getTankContents() {
			FluidStack fluid = this.filter.copy();
			if(!fluid.isEmpty())
				fluid.setAmount(this.storedAmount);
			return fluid;
		}
		private int pendingDrain = 0;
		public int getPendingDrain() { return this.pendingDrain; }
		public boolean hasPendingDrain() { return this.pendingDrain > 0; }
		public void addPendingDrain(int amount) { this.pendingDrain += amount; }
		public void removePendingDrain(int amount) { this.pendingDrain -= amount; }
		public boolean drainable = false;
		public boolean fillable = false;
		
		private FluidEntry(TraderFluidStorage parent, FluidStack fluid) {
			this.parent = parent;
			this.filter = fluid.copy();
			if(!this.filter.isEmpty())
				this.filter.setAmount(1);
			this.storedAmount = fluid.getAmount();
		}
		
		private FluidEntry(TraderFluidStorage parent, FluidStack fluid, int pendingDrain) {
			this.parent = parent;
			this.filter = fluid.copy();
			if(!this.filter.isEmpty())
				this.filter.setAmount(1);
			this.storedAmount = fluid.getAmount();
			this.pendingDrain = pendingDrain;
		}
		
		private FluidEntry(TraderFluidStorage parent, FluidStack filter, int amount, int pendingDrain, boolean drainable, boolean fillable) {
			this.parent = parent;
			this.filter = filter;
			if(!this.filter.isEmpty())
				this.filter.setAmount(1);
			this.storedAmount = amount;
			this.pendingDrain = pendingDrain;
			this.drainable = drainable;
			this.fillable = fillable;
		}
		
		public static FluidEntry load(TraderFluidStorage parent, CompoundTag compound) {
			FluidStack filter = FluidStack.loadFluidStackFromNBT(compound.getCompound("Filter"));
			int amount = compound.getInt("Amount");
			int pendingDrain = compound.getInt("PendingDrain");
			boolean drainable = compound.getBoolean("Drainable");
			boolean fillable = compound.getBoolean("Fillable");
			return new FluidEntry(parent, filter, amount, pendingDrain, drainable, fillable);
		}
		
		private void save(CompoundTag compound) {
			CompoundTag filterTag = new CompoundTag();
			this.filter.writeToNBT(filterTag);
			compound.put("Filter", filterTag);
			compound.putInt("Amount", this.storedAmount);
			compound.putInt("PendingDrain", this.pendingDrain);
			compound.putBoolean("Drainable", this.drainable);
			compound.putBoolean("Fillable", this.fillable);
		}

		@Override
		public int getTanks() { return 1; }

		@Override
		public FluidStack getFluidInTank(int tank) { return this.getTankContents(); }

		@Override
		public int getTankCapacity(int tank) { return this.parent.getTankCapacity(); }

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			//Should never be used. Only draining should be done from a specific entry.
			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			//Should never be used. Only draining should be done from a specific entry.
			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if(resource.isFluidEqual(this.filter) && !this.filter.isEmpty())
				return drain(resource.getAmount(), action);
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if(this.filter.isEmpty())
				return FluidStack.EMPTY;
			int drainableAmount = Math.min(maxDrain, this.storedAmount - this.pendingDrain);
			FluidStack drainStack = this.filter.copy();
			drainStack.setAmount(drainableAmount);
			if(action.execute())
				this.removeAmount(drainableAmount);
			return drainStack;
		}
		
	}
	
	
}
