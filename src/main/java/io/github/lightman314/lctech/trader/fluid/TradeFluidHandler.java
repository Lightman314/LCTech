package io.github.lightman314.lctech.trader.fluid;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TradeFluidHandler{

	final IFluidTrader trader;
	final Map<Direction,ExternalFluidHandler> externalHandlers = new HashMap<>();
	
	public TradeFluidHandler(IFluidTrader trader) {
		this.trader = trader;
	}
	
	public IFluidHandler getFluidHandler(Direction relativeDirection)
	{
		//Return null if both the input & output are disabled for that side.
		if(!this.trader.getFluidSettings().getInputSides().get(relativeDirection) && !this.trader.getFluidSettings().getOutputSides().get(relativeDirection))
			return null;
		//Otherwise, return the handler for that side
		if(!this.externalHandlers.containsKey(relativeDirection))
			this.externalHandlers.put(relativeDirection, new ExternalFluidHandler(this.trader, relativeDirection));
		return this.externalHandlers.get(relativeDirection);
	}

	public void resetDrainableTank() {
		this.externalHandlers.forEach((relDir,handler) -> handler.resetDrainableTank());
	}
	
	public static class ExternalFluidHandler implements IFluidHandler
	{
		
		protected final IFluidTrader trader;
		protected final Direction relativeDirection;
		
		private FluidTradeData drainableTank = null;
		
		protected ExternalFluidHandler(IFluidTrader trader, Direction relativeDirection) { this.trader = trader; this.relativeDirection = relativeDirection; }

		public final boolean isCreative() { return this.trader.getCoreSettings().isCreative(); }
		public final int getTradeCount() { return this.trader.getTradeCount(); }
		public final FluidTradeData getTrade(int tradeIndex) { return this.trader.getTrade(tradeIndex); }
		public final void markTradesDirty() {	this.trader.markTradesDirty(); }
		
		public void resetDrainableTank() { this.drainableTank = this.getValidDrainTrade(FluidStack.EMPTY); }
		
		public FluidTradeData getValidDrainTrade(FluidStack resource)
		{
			for(int i = 0; i < this.getTradeCount(); i++)
			{
				FluidTradeData trade = this.getTrade(i);
				if(!trade.getTankContents().isEmpty())
				{
					//Can drain sales trades if a pending drain has been made
					if(trade.hasPendingDrain() && (trade.getTankContents().isFluidEqual(resource) || resource.isEmpty()))
						return trade;
					//Can also drain purchase trades if draining is enabled.
					if(trade.isPurchase() && trade.canDrainExternally() && (trade.getTankContents().isFluidEqual(resource) || resource.isEmpty()))
						return trade;
				}
			}
			return null;
		}
		
		public FluidTradeData getValidFillTrade(FluidStack resource)
		{
			for(int i = 0; i < this.getTradeCount(); i++)
			{
				FluidTradeData trade = this.getTrade(i);
				if(trade.canFillExternally() && trade.getProduct().isFluidEqual(resource) && trade.validTankContents() && trade.getTankSpace() > 0)
					return trade;
			}
			return null;
		}
		
		@Override
		public int getTanks() {
			return this.trader.getTradeCount();
		}
		
		@Override
		public FluidStack getFluidInTank(int tank) {
			//Set to only display the contents of the drainable tank to fix the create pump issue.
			//Was originally just "return this.getTrade(tank).getTankContents();"
			//Should be a perfectly reasonable return function, especially since the value is supposed to never be modified,
			//The purpose of this function seems to be more of a read-only query just in case something like waila wants to know what fluids are in the tank, etc.
			//If you want to see if you can drain/fill your specific (or any) fluid, that's what the fill/drain with FluidAction.SIMULATE are for.
			if(drainableTank == null)
				this.resetDrainableTank();
			FluidTradeData trade = this.getTrade(tank);
			if(trade == this.drainableTank)	
			{
				if(trade.hasPendingDrain())
				{
					FluidStack drainableTank = trade.getTankContents();
					if(!drainableTank.isEmpty())
						drainableTank.setAmount(trade.getPendingDrain());
					return drainableTank;
				}
				else if(trade.isPurchase() && trade.canDrainExternally())
				{
					return trade.getTankContents();
				}
			}
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return this.getTrade(tank).getTankCapacity();
		}
		
		protected final boolean canFill() {
			return this.trader.getFluidSettings().getInputSides().get(this.relativeDirection);
		}
		
		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			if(!this.canFill())
				return false;
			return this.getTrade(tank).canFillExternally(stack);
		}
		
		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if(!this.canFill())
				return 0;
			FluidTradeData trade = this.getValidFillTrade(resource);
			if(trade != null)
			{
				//Fill the tank
				int fillAmount = MathUtil.clamp(resource.getAmount(), 0, trade.getTankSpace());
				if(action.execute())
				{
					FluidStack tank = trade.getTankContents();
					if(tank.isEmpty())
					{
						tank = resource.copy();
						tank.setAmount(fillAmount);
					}
					else
						tank.grow(fillAmount);
					trade.setTankContents(tank);
					this.markTradesDirty();
				}
				return fillAmount;
			}
			return 0;
		}
		
		protected final boolean canDrain() {
			return this.trader.getFluidSettings().getOutputSides().get(this.relativeDirection);
		}
		
		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if(!this.canDrain())
				return FluidStack.EMPTY;
			if(resource.isEmpty())
				return FluidStack.EMPTY;
			FluidTradeData trade = getValidDrainTrade(resource);
			if(trade != null)
			{
				int drainAmount = 0;
				if(trade.hasPendingDrain()) //Limit drain amount to pending drain
					drainAmount = MathUtil.clamp(resource.getAmount(), 0, this.isCreative() ? trade.getPendingDrain() : Math.min(trade.getPendingDrain(), trade.getTankContents().getAmount()));
				else //Allow full drainage, as this is a purchase tank drainage
					drainAmount = MathUtil.clamp(resource.getAmount(), 0, trade.getTankContents().getAmount());
				
				FluidStack returnStack = trade.getTankContents();
				returnStack.setAmount(drainAmount);
				
				if(action.execute())
				{
					//If creative, don't modify the tank
					if(!this.isCreative())
					{
						FluidStack tank = trade.getTankContents();
						tank.shrink(drainAmount);
						if(tank.isEmpty())
							tank = FluidStack.EMPTY;
						trade.setTankContents(tank);
					}
					if(trade.hasPendingDrain())
						trade.shrinkPendingDrain(drainAmount);
					this.markTradesDirty();
				}
				
				return returnStack;
				
			}
			return FluidStack.EMPTY;
		}
		
		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if(!this.canDrain())
				return FluidStack.EMPTY;
			FluidTradeData trade = getValidDrainTrade(FluidStack.EMPTY);
			if(trade != null)
			{
				int drainAmount = 0;
				if(trade.hasPendingDrain()) //Limit drain amount to pending drain
					drainAmount = MathUtil.clamp(maxDrain, 0, this.isCreative() ? trade.getPendingDrain() : Math.min(trade.getPendingDrain(), trade.getTankContents().getAmount()));
				else //Allow full drainage
					drainAmount = MathUtil.clamp(maxDrain, 0, trade.getTankContents().getAmount());
				
				FluidStack returnStack = trade.getTankContents();
				returnStack.setAmount(drainAmount);
				
				if(action.execute())
				{
					//If creative, don't modify the tank
					if(!this.isCreative())
					{
						FluidStack tank = trade.getTankContents();
						tank.shrink(drainAmount);
						if(tank.isEmpty())
							tank = FluidStack.EMPTY;
						trade.setTankContents(tank);
					}
					if(trade.hasPendingDrain())
						trade.shrinkPendingDrain(drainAmount);
					this.markTradesDirty();
				}
				
				return returnStack;
			}
			return FluidStack.EMPTY;
		}
		
	}
	
	//Run when the player clicks on the tank gui with a held item on both client and server.
	public void OnPlayerInteraction(PlayerEntity player, int tradeIndex)
	{
		ItemStack heldStack = player.inventory.getItemStack();
		if(heldStack.isEmpty()) //If held stack is empty, do nothing
			return;
		
		FluidTradeData trade = this.trader.getTrade(tradeIndex);
		if(trade == null)
			return;
		
		//Try and fill the tank
		FluidActionResult result = FluidUtil.tryEmptyContainer(heldStack, trade, Integer.MAX_VALUE, player, true);
		if(result.isSuccess())
		{
			this.trader.markTradesDirty();
			//If creative, and the item was a bucket, don't move the items around
			if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldStack.getItem() == Items.BUCKET))
				return;
			if(heldStack.getCount() > 1)
			{
				heldStack.shrink(1);
				player.inventory.setItemStack(heldStack);
				ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
			}
			else
			{
				player.inventory.setItemStack(heldStack);
			}
		}
		else
		{
			//Failed to fill the tank, now attempt to drain it
			result = FluidUtil.tryFillContainer(heldStack, trade, Integer.MAX_VALUE, player, true);
			if(result.isSuccess())
			{
				this.trader.markTradesDirty();
				//If creative, and the item was a bucket, don't move the items around
				if(player.isCreative() && (result.getResult().getItem() == Items.BUCKET || heldStack.getItem() == Items.BUCKET))
					return;
				if(heldStack.getCount() > 1)
				{
					heldStack.shrink(1);
					player.inventory.setItemStack(heldStack);
					ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
				}
				else
				{
					player.inventory.setItemStack(heldStack);
				}
			}
		}
	}
	
}
