package io.github.lightman314.lctech.trader.tradedata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.items.UpgradeItem;
import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lctech.trader.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class FluidTradeData extends TradeData{
	
	public enum FluidTradeType { SALE, PURCHASE }
	
	public static int MaxTradeTypeStringLength() {
		int length = 0;
		for(FluidTradeType value : FluidTradeType.values())
		{
			int thisLength = value.name().length();
			if(thisLength > length)
				length = thisLength;
		}
		return length;
	}
	
	public static final int MAX_BUCKET_QUANTITY = 10;
	public static final int DEFAULT_TANK_CAPACITY = FluidAttributes.BUCKET_VOLUME * MAX_BUCKET_QUANTITY;
	
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
	public void setBucketQuantity(int value) { this.bucketQuantity = MathUtil.clamp(value, 1, DEFAULT_TANK_CAPACITY / FluidAttributes.BUCKET_VOLUME); }
	
	public boolean canFillTank(FluidStack fluid)
	{
		return (this.product.isEmpty() && this.tank.isEmpty()) || ((this.tank.isEmpty() || this.tank.isFluidEqual(fluid)) && this.product.isFluidEqual(fluid));
	}
	
	boolean canDrain = false;
	public boolean canDrain() { return this.canDrain; }
	public void setDrainable(boolean value) { this.canDrain = value; }
	boolean canFill = false;
	public boolean canFill() { return this.canFill; }
	public void setFillable(boolean value) { this.canFill = value; }
	public boolean canFill(FluidStack fluid)
	{
		if(!canFill || fluid.isEmpty())
			return false;
		return (this.tank.isFluidEqual(this.product) || this.tank.isEmpty()) && this.product.isFluidEqual(fluid);
	}
	
	int pendingDrain = 0;
	public boolean hasPendingDrain() { return this.pendingDrain > 0; }
	public int getPendingDrain() { return this.pendingDrain; }
	public void shrinkPendingDrain(int amount) { this.pendingDrain -= amount; if(this.pendingDrain < 0) this.pendingDrain = 0; }
	
	FluidTradeType tradeType = FluidTradeType.SALE;
	public FluidTradeType getTradeType() { return this.tradeType; }
	public void setTradeType(FluidTradeType type) { this.tradeType = type; }
	public boolean isSale() { return this.tradeType == FluidTradeType.SALE; }
	public boolean isPurchase() { return this.tradeType == FluidTradeType.PURCHASE; }
	
	int tankCapacity = DEFAULT_TANK_CAPACITY;
	public int getTankCapacity() { return this.tankCapacity; }
	public void applyUpgrades(IFluidTrader trader, IInventory upgradeInventory)
	{
		this.tankCapacity = DEFAULT_TANK_CAPACITY;
		for(int i = 0; i < upgradeInventory.getSizeInventory(); i++)
		{
			ItemStack stack = upgradeInventory.getStackInSlot(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
				if(upgradeItem.getUpgradeType().allowedForMachine(trader) && upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					this.tankCapacity += upgradeItem.getDefaultUpgradeData().getIntValue(CapacityUpgrade.CAPACITY);
			}
		}
	}
	
	public int getTankSpace() { return this.getTankCapacity() - this.tank.getAmount(); }
	
	public ItemStack getFilledBucket() { return FluidUtil.getFilledBucket(this.product);}
	
	public FluidTradeData() { }
	
	public boolean hasStock(IFluidTrader trader, CoinValue price) { return this.getStock(trader, price) > 0; }
	public int getStock(IFluidTrader trader, CoinValue cost)
	{
		if(cost == null)
			cost = this.cost;
		if(this.product.isEmpty())
			return 0;
		
		if(this.isSale())
		{
			if(this.tank.isFluidEqual(this.product))
			{
				//How many buckets the tank holds
				return this.tank.getAmount() / this.getQuantity();
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
			long price = cost.getRawValue();
			return (int)(coinValue/price);
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
	//Flag as not valid should there be a pending drain
	public boolean isValid() { return super.isValid() && !this.product.isEmpty() && (this.tank.isEmpty() || this.product.isFluidEqual(this.tank)) && !this.hasPendingDrain(); }
	
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
					if(fluidHandler.fill(this.productOfQuantity(), FluidAction.SIMULATE) == this.getQuantity())
						passes.set(true);
					
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
				if(this.getFilledBucket().getItem() == bucketStack.getItem() && this.bucketQuantity == 1)
					return true;
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
			if(bucketStack.getItem() == Items.BUCKET && this.bucketQuantity == 1)
			{
				//Drain the fluid from the tank
				if(!isCreative)
					this.tank.shrink(FluidAttributes.BUCKET_VOLUME);
				return this.getFilledBucket();
			}
			AtomicBoolean drained = new AtomicBoolean(false);
			FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
				//Add the fluid to the fluid handler
				int fillAmount = fluidHandler.fill(this.productOfQuantity(), FluidAction.EXECUTE);
				//Drain the fluid from the tank
				if(!isCreative)
					this.tank.shrink(fillAmount);
				drained.set(true);
			});
			if(!drained.get())
			{
				//Set a pending drain
				this.pendingDrain += this.getQuantity();
			}
			return bucketStack;
		}
		else if(this.isPurchase())
		{
			
			//Remove the bucket from the bucketStack
			AtomicReference<ItemStack> returnStack = new AtomicReference<ItemStack>(bucketStack);
			FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
				if(fluidHandler instanceof FluidBucketWrapper && this.bucketQuantity == 1)
				{
					//Fill the tank
					if(!isCreative)
					{
						if(this.tank.isEmpty())
							this.tank = this.getProduct().copy();
						else
							this.tank.grow(FluidAttributes.BUCKET_VOLUME);
					}
					returnStack.set(new ItemStack(Items.BUCKET));
				}
				else
				{
					//Remove the fluid from the fluid handler
					FluidStack drainStack = fluidHandler.drain(this.productOfQuantity(), FluidAction.EXECUTE);
					//Add the fluid to the tank
					if(!isCreative)
					{
						if(this.tank.isEmpty())
							this.tank = drainStack;
						else
							this.tank.grow(drainStack.getAmount());
					}
				}
			});
			return returnStack.get();
		}
		else
		{
			LCTech.LOGGER.error("Fluid Trade type " + this.tradeType.name() + " is not a valid FluidTradeType for fluid transfer.");
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
		compound.putString("TradeType", this.tradeType.name());
		
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
		this.tradeType = loadTradeType(compound.getString("TradeType"));
		
	}
	
	public static FluidTradeType loadTradeType(String name)
	{
		FluidTradeType value = FluidTradeType.SALE;
		try {
			value = FluidTradeType.valueOf(name);
		} catch (IllegalArgumentException e) {
			LCTech.LOGGER.error("Could not load '" + name + "' as a TradeType.");
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

	@Override
	public TradeDirection getTradeDirection() { return TradeDirection.SALE; }
	
}
