package io.github.lightman314.lctech.tileentities;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.PlayerUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class TradeFluidHandler implements IFluidHandler{

	public final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> this);
	
	final Supplier<IFluidTrader> traderSource;
	
	private final IFluidTrader getTrader() {
		if(this.traderSource.get() != null)
			return this.traderSource.get();
		return new NullTrader();
	}
	private final int getTradeCount(){ return this.getTrader().getTradeCount(); }
	private final FluidTradeData getTrade(int tradeIndex) { return this.getTrader().getTrade(tradeIndex); }
	private final void markTradesDirty() { this.getTrader().markTradesDirty(); }
	
	public TradeFluidHandler(Supplier<IFluidTrader> traderSource) {
		this.traderSource = traderSource;
	}

	@Override
	public int getTanks() {
		return this.getTradeCount();
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return this.getTrade(tank).getTankContents();
	}

	@Override
	public int getTankCapacity(int tank) {
		return this.getTrade(tank).getTankCapacity();
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return this.getTrade(tank).canFill(stack);
	}

	private FluidTradeData getValidFillTrade(FluidStack resource)
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
	public int fill(FluidStack resource, FluidAction action) {
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

	private FluidTradeData getValidDrainTrade(FluidStack resource)
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
	
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		FluidTradeData trade = getValidDrainTrade(resource);
		if(trade != null)
		{
			int drainAmount = 0;
			if(trade.hasPendingDrain()) //Limit drain amount to pending drain
				drainAmount = MathUtil.clamp(resource.getAmount(), 0, Math.min(trade.getPendingDrain(), trade.getTankContents().getAmount()));
			else //Allow full drainage
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
	
	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
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
	
	public void OnPlayerInteraction(PlayerEntity player, int tradeIndex)
	{
		ItemStack heldStack = player.inventory.getItemStack();
		if(heldStack.isEmpty()) //If held stack is empty, do nothing
			return;
		
		FluidTradeData trade = this.getTrade(tradeIndex);
		//Return if the tank is empty, and no product is defined
		if(trade.getProduct().isEmpty() && trade.getTankContents().isEmpty())
			return;
		
		FluidUtil.getFluidHandler(heldStack).ifPresent(fluidHandler ->{
			if(fluidHandler instanceof FluidBucketWrapper) //Bucket interactions
			{
				FluidBucketWrapper bucketWrapper = (FluidBucketWrapper)fluidHandler;
				
				FluidStack bucketFluid = bucketWrapper.getFluid();
				FluidStack tank = trade.getTankContents();
				if(!bucketWrapper.getFluid().isEmpty())
				{
					//Attempt to fill the tank
					if((tank.isEmpty() || tank.isFluidEqual(bucketFluid)) && trade.getProduct().isFluidEqual(bucketFluid))
					{
						//Fluid is valid; Attempt to fill the tank
						if(trade.getTankSpace() >= bucketFluid.getAmount())
						{
							//Has space, fill the tank
							if(tank.isEmpty())
								tank = bucketFluid;
							else
								tank.grow(bucketFluid.getAmount());
							trade.setTankContents(tank);
							this.markTradesDirty();
							
							//Drain the bucket (unless we're in creative mode)
							if(!player.abilities.isCreativeMode)
							{
								ItemStack emptyBucket = new ItemStack(Items.BUCKET);
								heldStack.shrink(1);
								if(heldStack.isEmpty())
								{
									player.inventory.setItemStack(emptyBucket);
									player.inventory.markDirty();
								}	
								else
								{
									player.inventory.setItemStack(heldStack);
									player.inventory.markDirty();
									PlayerUtil.givePlayerItem(player, emptyBucket);
								}
							}
						}
					}
				} else
				{
					//Attempt to drain the tank
					if(!tank.isEmpty() && trade.getDrainableAmount() >= FluidAttributes.BUCKET_VOLUME)
					{
						//Tank has more than 1 bucket of fluid, drain the tank
						Item filledBucket = tank.getFluid().getFilledBucket();
						if(filledBucket != null && filledBucket != Items.AIR)
						{
							//Filled bucket is not null or air; Fill the bucket, and drain the tank
							tank.shrink(FluidAttributes.BUCKET_VOLUME);
							trade.setTankContents(tank);
							this.markTradesDirty();
							
							//Fill the bucket (unless we're in creative mode)
							if(!player.abilities.isCreativeMode)
							{
								ItemStack newBucket = new ItemStack(filledBucket, 1);
								heldStack.shrink(1);
								if(heldStack.isEmpty())
								{
									player.inventory.setItemStack(newBucket);
									player.inventory.markDirty();
								}
								else
								{
									player.inventory.setItemStack(heldStack);
									player.inventory.markDirty();
									PlayerUtil.givePlayerItem(player, newBucket);
								}
							}
							
						}
					}
				}
			}
			else //Normal fluid handler interaction
			{
				//If the product is empty, but the tank is not empty, attempt to drain the contents of the tank
				//If the tank is full, attempt to drain first as well
				if(trade.getProduct().isEmpty() || trade.getTankContents().getAmount() >= trade.getTankCapacity())
				{
					FluidStack tank = trade.getTankContents();
					
					//Limit drain request to the drainable amount, just in case a pending drain is active
					FluidStack fillRequest = tank.copy();
					fillRequest.setAmount(trade.getDrainableAmount());
					
					int drainedAmount = fluidHandler.fill(fillRequest, FluidAction.EXECUTE);
					if(drainedAmount > 0)
					{
						tank.shrink(drainedAmount);
						trade.setTankContents(tank);
						this.markTradesDirty();
					}
				}
				//Normal tank operations
				else if(!trade.getProduct().isEmpty() && trade.validTankContents())
				{
					
					//Attempt to deposit first
					FluidStack drainRequest = trade.getProduct();
					drainRequest.setAmount(trade.getTankSpace());
					
					FluidStack drainResults = fluidHandler.drain(drainRequest, FluidAction.EXECUTE);
					if(!drainResults.isEmpty())
					{
						FluidStack tank = trade.getTankContents();
						if(tank.isEmpty())
							tank = drainResults;
						else
							tank.grow(drainResults.getAmount());
						trade.setTankContents(tank);
						this.markTradesDirty();
					}
					else
					{
						
						//Deposit failed, attempt to drain the fluid then
						FluidStack tank = trade.getTankContents();
						//Limit drain request to the drainable amount, just in case a pending drain is active
						FluidStack fillRequest = tank.copy();
						fillRequest.setAmount(trade.getDrainableAmount());
						
						int drainedAmount = fluidHandler.fill(fillRequest, FluidAction.EXECUTE);
						if(drainedAmount > 0)
						{
							tank.shrink(drainedAmount);
							trade.setTankContents(tank);
							this.markTradesDirty();
						}
					}
				}
			}
		});
	}
	
	private class NullTrader implements IFluidTrader
	{
		
		@Override
		public ITextComponent getName() { return new StringTextComponent("NULL"); }
		@Override
		public UUID getOwnerID() { return new UUID(0,0); }

		@Override
		public CoinValue getStoredMoney() { return CoinValue.EMPTY; }
		@Override
		public int getTradeCount() { return 0; }

		@Override
		public int getTradeStock(int index) { return 0; }

		@Override
		public boolean hasCustomName() { return false; }

		@Override
		public boolean isCreative() { return false; }

		@Override
		public FluidTradeData getTrade(int tradeIndex) { return new FluidTradeData(); }

		@Override
		public List<FluidTradeData> getAllTrades() { return Lists.newArrayList(); }

		@Override
		public void markTradesDirty() { }

		@Override
		public IInventory getUpgradeInventory() { return new Inventory(5); }
		
		@Override
		public void reapplyUpgrades() { }
		
		@Override
		public TradeFluidHandler getFluidHandler() { return null; }

		@Override
		public boolean drainCapable() { return false; }

		@Override
		public void openTradeMenu(PlayerEntity player) { }

		@Override
		public void openStorageMenu(PlayerEntity player) { }

		@Override
		public void openFluidEditMenu(PlayerEntity player, int tradeIndex) { }
		
	}
	
}
