package io.github.lightman314.lctech.blockentities.handler;

import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings.FluidHandlerSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public class TradeFluidHandler{

	final IFluidTrader trader;
	final FluidHandlerTemplate inputOnly;
	final FluidHandlerTemplate outputOnly;
	final FluidHandlerTemplate inputAndOutput;
	
	public TradeFluidHandler(IFluidTrader trader) {
		this.trader = trader;
		this.inputOnly = new InputOnly(trader);
		this.outputOnly = new OutputOnly(trader);
		this.inputAndOutput = new InputAndOutput(trader);
	}
	
	public IFluidHandler getFluidHandler(FluidHandlerSettings setting)
	{
		switch(setting)
		{
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

	public void resetDrainableTank() {
		this.inputOnly.resetDrainableTank();
		this.outputOnly.resetDrainableTank();
		this.inputAndOutput.resetDrainableTank();
	}
	
	private abstract static class FluidHandlerTemplate implements IFluidHandler
	{
		
		protected final IFluidTrader trader;
		
		private FluidTradeData drainableTank = null;
		
		protected FluidHandlerTemplate(IFluidTrader trader) { this.trader = trader; }

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
					if(trade.isPurchase() && trade.canDrain() && (trade.getTankContents().isFluidEqual(resource) || resource.isEmpty()))
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
				if(trade.canFill() && trade.getProduct().isFluidEqual(resource) && trade.validTankContents() && trade.getTankSpace() > 0)
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
				else if(trade.isPurchase() && trade.canDrain())
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
		
		
	}
	
	private interface IInputHandler
	{
		
		public FluidTradeData getTrade(int tank);
		public void markTradesDirty();
		public FluidTradeData getValidFillTrade(FluidStack resource);
		
		public default boolean isFluidValidX(int tank, FluidStack stack) {
			return this.getTrade(tank).canFill(stack);
		}
		
		public default int fillX(FluidStack resource, FluidAction action) {
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
		
	}
	
	public interface IOutputHandler
	{
		
		public FluidTradeData getTrade(int tank);
		public void markTradesDirty();
		FluidTradeData getValidDrainTrade(FluidStack resource);
		
		public default FluidStack drainX(FluidStack resource, FluidAction action) {
			if(resource.isEmpty())
				return FluidStack.EMPTY;
			FluidTradeData trade = getValidDrainTrade(resource);
			if(trade != null)
			{
				int drainAmount = 0;
				if(trade.hasPendingDrain()) //Limit drain amount to pending drain
					drainAmount = MathUtil.clamp(resource.getAmount(), 0, Math.min(trade.getPendingDrain(), trade.getTankContents().getAmount()));
				else //Allow full drainage, as this is a purchase tank drainage
					drainAmount = MathUtil.clamp(resource.getAmount(), 0, trade.getTankContents().getAmount());
				
				FluidStack returnStack = trade.getTankContents();
				returnStack.setAmount(drainAmount);
				
				if(action.execute())
				{
					FluidStack tank = trade.getTankContents();
					tank.shrink(drainAmount);
					if(tank.isEmpty())
						tank = FluidStack.EMPTY;
					trade.setTankContents(tank);
					if(trade.hasPendingDrain())
						trade.shrinkPendingDrain(drainAmount);
					this.markTradesDirty();
				}
				
				return returnStack;
				
			}
			return FluidStack.EMPTY;
		}
		
		public default FluidStack drainX(int maxDrain, FluidAction action) {
			FluidTradeData trade = getValidDrainTrade(FluidStack.EMPTY);
			if(trade != null)
			{
				int drainAmount = 0;
				if(trade.hasPendingDrain()) //Limit drain amount to pending drain
					drainAmount = MathUtil.clamp(maxDrain, 0, Math.min(trade.getPendingDrain(), trade.getTankContents().getAmount()));
				else //Allow full drainage
					drainAmount = MathUtil.clamp(maxDrain, 0, trade.getTankContents().getAmount());
				
				FluidStack returnStack = trade.getTankContents();
				returnStack.setAmount(drainAmount);
				
				if(action.execute())
				{
					FluidStack tank = trade.getTankContents();
					tank.shrink(drainAmount);
					if(tank.isEmpty())
						tank = FluidStack.EMPTY;
					trade.setTankContents(tank);
					if(trade.hasPendingDrain())
						trade.shrinkPendingDrain(drainAmount);
					this.markTradesDirty();
				}
				
				return returnStack;
			}
			return FluidStack.EMPTY;
		}
		
	}

	private static class InputOnly extends FluidHandlerTemplate implements IInputHandler
	{

		private InputOnly(IFluidTrader trader) { super(trader); }

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return this.isFluidValidX(tank, stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			return this.fillX(resource, action);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return FluidStack.EMPTY;
		}
		
	}

	private static class OutputOnly extends FluidHandlerTemplate implements IOutputHandler
	{
		
		private OutputOnly(IFluidTrader trader) { super(trader); }

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return this.drainX(resource, action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return this.drainX(maxDrain, action);
		}
		
	}
	
	private static class InputAndOutput extends FluidHandlerTemplate implements IInputHandler, IOutputHandler
	{

		private InputAndOutput(IFluidTrader trader) { super(trader); }

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return this.isFluidValidX(tank, stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			return this.fillX(resource, action);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return this.drainX(resource, action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return this.drainX(maxDrain, action);
		}
		
	}
	
	//Run when the player clicks on the tank gui with a held item on both client and server.
	public void OnPlayerInteraction(AbstractContainerMenu menu, Player player, int tradeIndex)
	{
		ItemStack heldStack = menu.getCarried();
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
				menu.setCarried(heldStack);
				ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
			}
			else
			{
				menu.setCarried(result.getResult());
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
					menu.setCarried(heldStack);
					ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
				}
				else
				{
					menu.setCarried(result.getResult());
				}
			}
				
		}
		
	}
	
}
