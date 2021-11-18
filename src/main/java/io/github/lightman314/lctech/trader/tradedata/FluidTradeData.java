package io.github.lightman314.lctech.trader.tradedata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

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
	
	public static final int DEFAULT_TANK_CAPACITY = FluidAttributes.BUCKET_VOLUME * 10;
	
	//Tank is stored locally, as each fluid-type being sold is stored independently
	FluidStack tank = FluidStack.EMPTY;
	public FluidStack getTankContents() { return tank.copy(); }
	public int getDrainableAmount() { return tank.getAmount() - this.pendingDrain; }
	public boolean validTankContents() { return this.tank.isEmpty() || this.tank.isFluidEqual(this.product); }
	public void setTankContents(FluidStack tank) { this.tank = tank.copy(); }
	public double getTankFillPercent() { return (double)tank.getAmount() / (double)this.tankCapacity; }
	
	FluidStack product = FluidStack.EMPTY;
	public FluidStack getProduct() { return product.copy(); }
	public void setProduct(FluidStack newProduct) {
		this.product = newProduct.copy();
		if(this.product.getFluid() != Fluids.EMPTY)
			this.product.setAmount(FluidAttributes.BUCKET_VOLUME);
	}
	
	boolean canDrain = false;
	public boolean canDrain() { return this.canDrain; }
	public void setDrainable(boolean value) { this.canDrain = value; }
	boolean canFill = false;
	public boolean canFill() { return canFill; }
	public void setFillable(boolean value) { this.canFill = value; }
	public boolean canFill(FluidStack fluid)
	{
		if(!canFill || fluid.isEmpty())
			return false;
		return (this.tank.containsFluid(this.product) || this.tank.isEmpty()) && this.product.isFluidEqual(fluid);
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
	public int getTankSpace() { return this.tankCapacity - this.tank.getAmount(); }
	
	public ItemStack getFilledBucket() { return FluidUtil.getFilledBucket(this.product);}
	
	public FluidTradeData()
	{
		
	}
	
	public FluidTradeData(int tankCapacity)
	{
		this.tankCapacity = tankCapacity;
	}
	
	public boolean hasStock(IFluidTrader trader, CoinValue price) { return this.getStock(trader, price) > 0; }
	public int getStock(IFluidTrader trader, CoinValue cost)
	{
		if(this.product.isEmpty())
			return 0;
		
		if(this.isSale())
		{
			if(this.tank.isFluidEqual(this.product))
			{
				//How many buckets the tank holds
				return this.tank.getAmount() / FluidAttributes.BUCKET_VOLUME;
			}
		}
		else if(this.isPurchase())
		{
			//How many payments the trader can make
			if(this.isFree)
				return 1;
			if(this.cost.getRawValue() == 0)
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
			return this.getTankSpace() >= FluidAttributes.BUCKET_VOLUME;
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
		if(this.isSale())
		{
			if(!this.tank.isFluidEqual(this.product) || this.tank.getAmount() < FluidAttributes.BUCKET_VOLUME)
				return false;
			//Check the bucket stack for a fluid handler that can hold 1 bucket of product
			if(!bucketStack.isEmpty())
			{
				if(bucketStack.getItem() == Items.BUCKET)
					return !this.getFilledBucket().isEmpty();
				AtomicBoolean passes = new AtomicBoolean(false);
				FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
					if(fluidHandler.fill(this.product, FluidAction.SIMULATE) == this.product.getAmount())
						passes.set(true);
				});
				if(passes.get())
					return true;
			}
			if(this.canDrain)
				return true;
		}
		else if(this.isPurchase())
		{
			//Check if the bucket stack can have 1 bucket of the product extracted
			if(this.tank.isEmpty() || this.tank.isFluidEqual(this.product))
			{
				if(this.getFilledBucket().getItem() == bucketStack.getItem())
					return true;
				AtomicBoolean passes = new AtomicBoolean(false);
				FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
					if(fluidHandler.drain(this.product, FluidAction.SIMULATE).getAmount() == this.product.getAmount())
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
			if(bucketStack.getItem() == Items.BUCKET)
			{
				//Drain the fluid from the tank
				if(!isCreative)
					this.tank.shrink(FluidAttributes.BUCKET_VOLUME);
				return this.getFilledBucket();
			}
			AtomicBoolean drained = new AtomicBoolean(false);
			FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
				//Add the fluid to the fluid handler
				int fillAmount = fluidHandler.fill(this.getProduct(), FluidAction.EXECUTE);
				//Drain the fluid from the tank
				if(!isCreative)
					this.tank.shrink(fillAmount);
				drained.set(true);
			});
			if(!drained.get())
			{
				//Set a pending drain
				this.pendingDrain += FluidAttributes.BUCKET_VOLUME;
			}
			return bucketStack;
		}
		else if(this.isPurchase())
		{
			if(bucketStack.getItem() instanceof BucketItem)
			{
				//Fill the tank
				if(!isCreative)
				{
					if(this.tank.isEmpty())
						this.tank = this.getProduct().copy();
					else
						this.tank.grow(FluidAttributes.BUCKET_VOLUME);
				}
				return new ItemStack(Items.BUCKET);
			}
			//Remove the bucket from the bucketStack
			FluidUtil.getFluidHandler(bucketStack).ifPresent(fluidHandler ->{
				//Remove the fluid from the fluid handler
				FluidStack drainStack = fluidHandler.drain(this.getProduct(), FluidAction.EXECUTE);
				//Add the fluid to the tank
				if(!isCreative)
				{
					if(this.tank.isEmpty())
						this.tank = drainStack;
					else
						this.tank.grow(drainStack.getAmount());
				}
					
			});
			return bucketStack;
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
		compound.put("Trade", this.product.writeToNBT(new CompoundNBT()));
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
		//Load the product
		this.product = FluidStack.loadFluidStackFromNBT(compound.getCompound("Trade"));
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
	
	public static List<FluidTradeData> listOfSize(int tradeCount, int tankCapacity)
	{
		List<FluidTradeData> list = new ArrayList<>();
		while(list.size() < tradeCount)
		{
			list.add(new FluidTradeData(tankCapacity));
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
	
	public static List<FluidTradeData> LoadNBTList(int tradeCount, int tankCapacity, CompoundNBT compound)
	{
		return LoadNBTList(tradeCount, tankCapacity, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static List<FluidTradeData> LoadNBTList(int tradeCount, int tankCapacity, CompoundNBT compound, String tag)
	{
		List<FluidTradeData> tradeData = listOfSize(tradeCount, tankCapacity);
		
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
