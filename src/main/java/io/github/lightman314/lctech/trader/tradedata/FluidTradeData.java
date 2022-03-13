package io.github.lightman314.lctech.trader.tradedata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.items.UpgradeItem;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidTradeData extends TradeData implements IFluidHandler{
	
	public static final int getDefaultTankCapacity() { return TechConfig.SERVER.fluidTraderDefaultStorage.get() * FluidAttributes.BUCKET_VOLUME; }
	
	//Tank is stored locally, as each fluid-type being sold is stored independently
	FluidStack tank = FluidStack.EMPTY;
	public FluidStack getTankContents() { return tank.copy(); }
	public int getDrainableAmount() { return tank.getAmount() - this.pendingDrain; }
	public boolean validTankContents() { return this.tank.isEmpty() || this.tank.isFluidEqual(this.product); }
	public void setTankContents(FluidStack tank) { this.tank = tank.copy(); }
	public double getTankFillPercent() { return MathUtil.clamp((double)tank.getAmount() / (double)this.getTankCapacity(), 0d, 1d); }
	
	FluidStack product = FluidStack.EMPTY;
	public FluidStack getProduct() { return product.copy(); }
	public void setProduct(FluidStack newProduct) {
		this.product = newProduct.copy();
		if(this.product.getFluid() != Fluids.EMPTY)
			this.product.setAmount(FluidAttributes.BUCKET_VOLUME);
	}
	private FluidStack productOfQuantity()
	{
		FluidStack stack = this.product.copy();
		if(!stack.isEmpty())
			stack.setAmount(this.getQuantity());
		return stack;
	}
	
	int bucketQuantity = 1;
	public int getQuantity() { return this.bucketQuantity * FluidAttributes.BUCKET_VOLUME; }
	public int getBucketQuantity() { return this.bucketQuantity; }
	public void setBucketQuantity(int value) { this.bucketQuantity = MathUtil.clamp(value, 1, this.getMaxBucketQuantity()); }
	public int getMaxBucketQuantity() { return Math.max(1, Math.min(TechConfig.SERVER.fluidTradeMaxQuantity.get(), this.tankCapacity / FluidAttributes.BUCKET_VOLUME)); }
	
	public boolean canFillTank(FluidStack fluid)
	{
		return !fluid.isEmpty() && ((this.tank.isEmpty() || this.tank.isFluidEqual(fluid)) && this.product.isFluidEqual(fluid));
	}
	
	boolean canDrain = false;
	public boolean canDrainExternally() { return this.canDrain; }
	public void setDrainableExternally(boolean value) { this.canDrain = value; }
	boolean canFill = false;
	public boolean canFillExternally() { return this.canFill; }
	public void setFillableExternally(boolean value) { this.canFill = value; }
	public boolean canFillExternally(FluidStack fluid)
	{
		if(!canFill || fluid.isEmpty())
			return false;
		return (this.tank.isFluidEqual(this.product) || this.tank.isEmpty()) && this.product.isFluidEqual(fluid);
	}
	
	int pendingDrain = 0;
	public boolean hasPendingDrain() { return this.pendingDrain > 0; }
	public int getPendingDrain() { return this.pendingDrain; }
	public void shrinkPendingDrain(int amount) { this.pendingDrain -= amount; if(this.pendingDrain < 0) this.pendingDrain = 0; }
	
	TradeDirection tradeDirection = TradeDirection.SALE;
	public TradeDirection getTradeDirection() { return this.tradeDirection; }
	public void setTradeDirection(TradeDirection type) { this.tradeDirection = type; }
	public boolean isSale() { return this.tradeDirection == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeDirection == TradeDirection.PURCHASE; }
	
	int tankCapacity = getDefaultTankCapacity();
	public int getTankCapacity() { return this.tankCapacity; }
	public void applyUpgrades(IFluidTrader trader, IInventory upgradeInventory)
	{
		int defaultCapacity = getDefaultTankCapacity();
		this.tankCapacity = defaultCapacity;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < upgradeInventory.getSizeInventory(); i++)
		{
			ItemStack stack = upgradeInventory.getStackInSlot(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
				if(trader.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					{
						int addAmount = upgradeItem.getDefaultUpgradeData().getIntValue(CapacityUpgrade.CAPACITY);
						if(addAmount > defaultCapacity && !baseStorageCompensation)
						{
							addAmount -= defaultCapacity;
							baseStorageCompensation = true;
						}
						this.tankCapacity += addAmount;
					}
				}
			}
		}
	}
	
	public int getTankSpace() { return this.getTankCapacity() - this.tank.getAmount(); }
	
	public ItemStack getFilledBucket() { return FluidUtil.getFilledBucket(this.product);}
	
	public FluidTradeData() { }
	
	public boolean hasStock(IFluidTrader trader) { return this.getStock(trader) > 0; }
	public boolean hasStock(IFluidTrader trader, PlayerEntity player) { return this.getStock(trader, player) > 0; }
	public boolean hasStock(IFluidTrader trader, PlayerReference player) { return this.getStock(trader, player) > 0; }
	public int getStock(IFluidTrader trader) { return this.getStock(trader, (PlayerReference)null); }
	public int getStock(IFluidTrader trader, PlayerEntity player) { return this.getStock(trader, PlayerReference.of(player)); }
	public int getStock(IFluidTrader trader, PlayerReference player)
	{
		
		if(this.product.isEmpty())
			return 0;
		
		if(this.isSale())
		{
			if(this.tank.isFluidEqual(this.product))
			{
				//How many buckets the tank holds
				//Presume that any fluids pending drain are not in the tank
				return (this.tank.getAmount() - this.pendingDrain) / this.getQuantity();
			}
		}
		else if(this.isPurchase())
		{
			//How many payments the trader can make
			if(this.cost.isFree())
				return 1;
			if(cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			CoinValue price = player == null ? this.cost : trader.runTradeCostEvent(player, trader.getAllTrades().indexOf(this)).getCostResult();
			return (int)(coinValue/price.getRawValue());
		}
		return 0;
	}
	public boolean hasSpace()
	{
		if(this.isPurchase())
			return this.getTankSpace() >= this.getQuantity();
		return true;
	}
	
	//Flag as not valid should the tank & product not match
	public boolean isValid() { return super.isValid() && !this.product.isEmpty() && (this.tank.isEmpty() || this.product.isFluidEqual(this.tank)); }
	
	/**
	 * Confirms that the bucket stack can have the proper amount of fluids extracted or poured into them.
	 */
	public boolean canTransferFluids(ItemStack bucketStack)
	{
		if(!this.isValid())
			return false;
		if(this.product.isEmpty())
			return false;
		if(this.isSale())
		{
			if(!this.tank.isFluidEqual(this.product) || this.tank.getAmount() < this.getQuantity())
				return false;
			if(this.canDrain)
				return true;
			//Check the bucket stack for a fluid handler that can hold 1 bucket of product
			if(!bucketStack.isEmpty())
			{
				if(bucketStack.getItem() == Items.BUCKET)
					return !this.getFilledBucket().isEmpty() && this.bucketQuantity == 1;
				AtomicBoolean passes = new AtomicBoolean(false);
				FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
					passes.set(fluidHandler.fill(this.productOfQuantity(), FluidAction.SIMULATE) == this.getQuantity());
				});
				if(passes.get())
					return true;
			}
		}
		else if(this.isPurchase())
		{
			if(bucketStack.isEmpty())
				return false;
			//Check if the bucket stack can have 1 bucket of the product extracted
			if(this.tank.isEmpty() || this.tank.isFluidEqual(this.product))
			{
				//Shouldn't need a manual check for an empty bucket, fluid handler method should work just fine
				//if(this.getFilledBucket().getItem() == bucketStack.getItem() && this.bucketQuantity == 1)
				//	return true;
				AtomicBoolean passes = new AtomicBoolean(false);
				FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
					if(fluidHandler.drain(this.productOfQuantity(), FluidAction.SIMULATE).getAmount() == this.getQuantity())
						passes.set(true);
				});
				if(passes.get())
					return true;
			}
		}
		return false;
	}
	
	public ItemStack transferFluids(ItemStack bucketStack, boolean isCreative)
	{
		if(!this.canTransferFluids(bucketStack))
		{
			LCTech.LOGGER.error("Attempted to transfer fluid trade fluids without confirming that you can.");
			return bucketStack;
		}
			
		if(this.isSale())
		{
			//Check if it can fill the item appropriately
			AtomicBoolean canFillNormally = new AtomicBoolean(false);
			FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
				canFillNormally.set(fluidHandler.fill(this.productOfQuantity(), FluidAction.SIMULATE) == this.getQuantity());
			});
			
			if(canFillNormally.get())
			{
				//If creative, temporarily replace the tank with a filled tank
				FluidStack tankBackup = this.tank.copy();
				if(isCreative)
					this.tank = this.productOfQuantity();
				//Fill the item from the tank
				FluidActionResult result = FluidUtil.tryFillContainer(bucketStack, this, this.getQuantity(), null, true);
				//If creative, restore the backup tank to ensure the tank contents were not changed.
				if(isCreative)
					this.tank = tankBackup;
				
				return result.getResult();
			}
			else if(this.canDrain) //Cannot drain to item, trigger pending drain
			{
				this.pendingDrain += this.getQuantity();
				return bucketStack;
			}
			else
			{
				LCTech.LOGGER.error("Flagged as being able to transfer fluids for the sale, but the bucket stack cannot accept the fluid, and this trade does not allow external drains.");
				return bucketStack;
			}
		}
		else if(this.isPurchase())
		{
			
			//Drain the item into the tank
			//If creative, temporarily replace the tank with an empty tank
			FluidStack tankBackup = this.tank.copy();
			if(isCreative)
				this.tank = FluidStack.EMPTY;
			FluidActionResult result = FluidUtil.tryEmptyContainer(bucketStack, this, this.getQuantity(), null, true);
			//If creative, restore the backup tank to ensure the tank contents were not changed.
			if(isCreative)
				this.tank = tankBackup;
			return result.getResult();
			
		}
		else
		{
			LCTech.LOGGER.error("Fluid Trade type " + this.tradeDirection.name() + " is not a valid FluidTradeType for fluid transfer.");
			return bucketStack;
		}
	}
	
	@Override
	public CompoundNBT getAsNBT()
	{
		CompoundNBT compound = super.getAsNBT();
		
		compound.put("Tank", this.tank.writeToNBT(new CompoundNBT()));
		compound.putInt("Capacity", this.tankCapacity);
		compound.put("Trade", this.product.writeToNBT(new CompoundNBT()));
		compound.putInt("Quantity", this.bucketQuantity);
		compound.putBoolean("CanDrain", this.canDrain);
		compound.putBoolean("CanFill", this.canFill);
		compound.putInt("PendingDrain", this.pendingDrain);
		compound.putString("TradeType", this.tradeDirection.name());
		
		return compound;
	}
	
	@Override
	public void loadFromNBT(CompoundNBT compound)
	{
		super.loadFromNBT(compound);
		//Load the tank
		this.tank = FluidStack.loadFluidStackFromNBT(compound.getCompound("Tank"));
		//Load the tank capacity
		this.tankCapacity = compound.getInt("Capacity");
		//Load the product
		this.product = FluidStack.loadFluidStackFromNBT(compound.getCompound("Trade"));
		//Load the quantity
		if(compound.contains("Quantity", Constants.NBT.TAG_INT))
			this.bucketQuantity = compound.getInt("Quantity");
		//Load whether it can be drained
		this.canDrain = compound.getBoolean("CanDrain");
		//Load whether it can be filled
		this.canFill = compound.getBoolean("CanFill");
		//Load the pending drain
		this.pendingDrain = compound.getInt("PendingDrain");
		//Load the trade type
		this.tradeDirection = loadTradeType(compound.getString("TradeType"));
		
	}
	
	public static TradeDirection loadTradeType(String name)
	{
		TradeDirection value = TradeDirection.SALE;
		try {
			value = TradeDirection.valueOf(name);
		} catch (IllegalArgumentException e) {
			LCTech.LOGGER.error("Could not load '" + name + "' as a TradeDirection.");
		}
		return value;
	}
	
	public static List<FluidTradeData> listOfSize(int tradeCount)
	{
		List<FluidTradeData> list = new ArrayList<>();
		while(list.size() < tradeCount)
		{
			list.add(new FluidTradeData());
		}
		return list;
	}
	
	public static CompoundNBT WriteNBTList(List<FluidTradeData> tradeList, CompoundNBT compound)
	{
		return WriteNBTList(tradeList, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static CompoundNBT WriteNBTList(List<FluidTradeData> tradeList, CompoundNBT compound, String tag)
	{
		ListNBT list = new ListNBT();
		for(int i = 0; i < tradeList.size(); i++)
		{
			list.add(tradeList.get(i).getAsNBT());
			//LCTech.LOGGER.info("Wrote to NBT List: \n" + tradeList.get(i).getAsNBT().toString());
		}
		compound.put(tag, list);
		return compound;
	}
	
	public static List<FluidTradeData> LoadNBTList(int tradeCount, CompoundNBT compound)
	{
		return LoadNBTList(tradeCount, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static List<FluidTradeData> LoadNBTList(int tradeCount, CompoundNBT compound, String tag)
	{
		List<FluidTradeData> tradeData = listOfSize(tradeCount);
		
		if(!compound.contains(tag))
			return tradeData;
		
		ListNBT list = compound.getList(tag, Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < list.size() && i < tradeCount; i++)
		{
			tradeData.get(i).loadFromNBT(list.getCompound(i));
			//LCTech.LOGGER.info("Loaded from NBT List:\n" + list.getCompound(i).toString());
			//LCTech.LOGGER.info("Loaded value writes as:\n" + tradeData.get(i).getAsNBT());
		}
		
		return tradeData;
	}
	
	//Personal fluid handler. Assume that the player has permission to fill or drain the tank
	
	@Override
	public int getTanks() { return 1; }
	@Override
	public FluidStack getFluidInTank(int tank) {
		return tank == 0 ? this.tank.copy() : FluidStack.EMPTY;
	}
	@Override
	public int getTankCapacity(int tank) {
		return tank == 0 ? this.tankCapacity : 0;
	}
	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return tank == 0 ? this.canFillTank(stack) : false;
	}
	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if(!this.canFillTank(resource))
			return 0;
		int fillAmount = Math.min(resource.getAmount(), this.tankCapacity - this.tank.getAmount());
		if(action.execute())
		{
			if(this.tank.isEmpty())
			{
				this.tank = resource.copy();
				this.tank.setAmount(fillAmount);
			}
			else
				this.tank.grow(fillAmount);
		}
		return fillAmount;
	}
	
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if(resource.isEmpty() || this.tank.isEmpty() || !this.tank.isFluidEqual(resource))
			return FluidStack.EMPTY;
		int drainAmount = Math.min(resource.getAmount(), this.tank.getAmount());
		FluidStack result = this.tank.copy();
		result.setAmount(drainAmount);
		if(action.execute())
		{
			//Drain the tank
			this.tank.shrink(drainAmount);
			if(this.tank.isEmpty())
				this.tank = FluidStack.EMPTY;
		}
		return result;
	}
	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if(this.tank.isEmpty())
			return FluidStack.EMPTY;
		FluidStack resource = this.tank.copy();
		resource.setAmount(maxDrain);
		return this.drain(resource, action);
	}
	
	@Override
	public boolean AcceptableDifferences(TradeComparisonResult differences) {
		return false;
	}
	
	@Override
	public TradeComparisonResult compare(TradeData otherTrade) {
		return new TradeComparisonResult();
	}
	
}
