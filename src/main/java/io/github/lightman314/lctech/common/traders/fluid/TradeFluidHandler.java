package io.github.lightman314.lctech.common.traders.fluid;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public class TradeFluidHandler{

	final FluidTraderData trader;
	Map<Direction,ExternalFluidHandler> externalHandlers = new HashMap<>();
	
	public TradeFluidHandler(FluidTraderData trader) {
		this.trader = trader;
	}
	
	public IFluidHandler getExternalHandler(Direction relativeDirection)
	{
		//Otherwise, return the handler for that side
		if(!this.externalHandlers.containsKey(relativeDirection))
			this.externalHandlers.put(relativeDirection, new ExternalFluidHandler(this.trader, relativeDirection));
		return this.externalHandlers.get(relativeDirection);
	}
	
	public static class ExternalFluidHandler implements IFluidHandler
	{
		
		protected final FluidTraderData trader;
		protected final Direction relativeDirection;
		
		protected ExternalFluidHandler(FluidTraderData trader, Direction relativeDirection) { this.trader = trader; this.relativeDirection = relativeDirection; }

		public final boolean isCreative() { return this.trader.isCreative(); }
		public final FluidEntry getTankEntry(FluidStack fluid) { return this.trader.getStorage().getTank(fluid); }
		public final FluidEntry getTankEntry(int tank) { return (tank < 0 || tank >= this.trader.getStorage().getContents().size()) ? null : this.trader.getStorage().getContents().get(tank); }
		public final void markStorageDirty() {	this.trader.getStorage().clearInvalidTanks(); this.trader.markStorageDirty(); }
		
		public FluidEntry getValidDrainTank(FluidStack resource)
		{
			for(FluidEntry entry : this.trader.getStorage().getContents())
			{
				if(!entry.filter.isEmpty())
				{
					//Can drain sales trades if a pending drain has been made
					if(entry.hasPendingDrain() && (FluidStack.isSameFluidSameComponents(entry.filter,resource) || resource.isEmpty()))
						return entry;
					//Can also drain purchase trades if draining is enabled.
					if(this.allowAutoDraining(entry.filter) && entry.drainable)
						return entry;
				}
			}
			return null;
		}
		
		private boolean allowAutoDraining(FluidStack fluid) {
			if(fluid.isEmpty())
				return false;
			for(int i = 0; i < this.trader.getTradeCount(); ++i)
			{
				FluidTradeData trade = this.trader.getTrade(i);
				if(trade.isSale() && FluidStack.isSameFluidSameComponents(trade.getProduct(),fluid))
					return false;
			}
			return true;
		}
		
		@Override
		public int getTanks() { return this.trader.getStorage().getContents().size(); }
		
		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank) {
			if(tank >= 0 && tank < this.trader.getStorage().getContents().size())
				return this.trader.getStorage().getContents().get(tank).getTankContents();
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return this.trader.getTankCapacity();
		}
		
		protected final boolean cannotFill() {
			return !this.trader.allowInputSide(this.relativeDirection);
		}
		
		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
			if(this.cannotFill())
				return false;
			FluidEntry entry = this.getTankEntry(tank);
			if(entry == null)
				return false;
			return entry.fillable && FluidStack.isSameFluidSameComponents(entry.filter,stack);
		}
		
		@Override
		public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
			if(this.cannotFill())
				return 0;
			FluidEntry tank = this.getTankEntry(resource);
			if(tank != null)
			{
				//Fill the tank
				int fillAmount = MathUtil.clamp(resource.getAmount(), 0, this.trader.getStorage().getFillableAmount(resource));
				if(action.execute())
				{
					tank.addAmount(fillAmount);
					this.markStorageDirty();
				}
				return fillAmount;
			}
			return 0;
		}
		
		protected final boolean cannotDrain() {
			return !this.trader.allowOutputSide(this.relativeDirection);
		}
		
		@Nonnull
		@Override
		public FluidStack drain(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
			if(this.cannotDrain())
				return FluidStack.EMPTY;
			if(resource.isEmpty())
				return FluidStack.EMPTY;
			FluidEntry tank = this.getValidDrainTank(resource);
			if(tank != null)
			{
				int drainAmount;
				if(tank.hasPendingDrain()) //Limit drain amount to pending drain
					drainAmount = Math.min(resource.getAmount(), this.isCreative() ? tank.getPendingDrain() : Math.min(tank.getPendingDrain(), tank.getStoredAmount()));
				else //Allow full drainage, as this is a purchase tank drainage
					drainAmount = Math.min(resource.getAmount(), tank.getTankContents().getAmount());
				
				FluidStack returnStack = tank.filter.copy();
				returnStack.setAmount(drainAmount);
				
				if(action.execute())
				{
					//If creative, don't modify the tank
					if(!this.isCreative())
					{
						tank.removeAmount(drainAmount);
					}
					if(tank.hasPendingDrain())
						tank.removePendingDrain(drainAmount);
					this.markStorageDirty();
				}
				
				return returnStack;
				
			}
			return FluidStack.EMPTY;
		}
		
		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, @Nonnull FluidAction action) {
			if(this.cannotDrain())
				return FluidStack.EMPTY;
			FluidEntry tank = getValidDrainTank(FluidStack.EMPTY);
			if(tank != null)
			{
				int drainAmount;
				if(tank.hasPendingDrain()) //Limit drain amount to pending drain
					drainAmount = Math.min(maxDrain, this.isCreative() ? tank.getPendingDrain() : Math.min(tank.getPendingDrain(), tank.getTankContents().getAmount()));
				else //Allow full drainage, as this is a purchase tank drainage
					drainAmount = Math.min(maxDrain, tank.getTankContents().getAmount());
				
				FluidStack returnStack = tank.filter.copy();
				returnStack.setAmount(drainAmount);
				
				if(action.execute())
				{
					//If creative, don't modify the tank
					if(!this.isCreative())
					{
						tank.removeAmount(drainAmount);
					}
					if(tank.hasPendingDrain())
						tank.removePendingDrain(drainAmount);
					this.markStorageDirty();
				}
				
				return returnStack;
			}
			return FluidStack.EMPTY;
		}
		
		
	}
	
}
