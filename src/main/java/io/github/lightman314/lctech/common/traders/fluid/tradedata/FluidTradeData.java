package io.github.lightman314.lctech.common.traders.fluid.tradedata;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.client.FluidTradeButtonRenderer;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;

public class FluidTradeData extends TradeData {

	FluidStack product = FluidStack.EMPTY;
	public FluidStack getProduct() { return this.product.copy(); }
	public void setProduct(FluidStack newProduct) {
		this.product = newProduct.copy();
		if(this.product.getFluid() != Fluids.EMPTY)
			this.product.setAmount(FluidType.BUCKET_VOLUME);
	}
	public FluidStack productOfQuantity()
	{
		FluidStack stack = this.product.copy();
		if(!stack.isEmpty())
			stack.setAmount(this.getQuantity());
		return stack;
	}

	int bucketQuantity = 1;
	public int getQuantity() { return this.bucketQuantity * FluidType.BUCKET_VOLUME; }
	public int getBucketQuantity() { return this.bucketQuantity; }
	public void setBucketQuantity(int value) { this.bucketQuantity = MathUtil.clamp(value, 1, this.getMaxBucketQuantity()); }
	public int getMaxBucketQuantity() { return Math.max(1, TechConfig.SERVER.fluidTradeMaxQuantity.get()); }

	TradeDirection tradeDirection = TradeDirection.SALE;
	public TradeDirection getTradeDirection() { return this.tradeDirection; }
    @Override
    public void setTradeDirection(TradeDirection type) {
        if(type == TradeDirection.SALE || type == TradeDirection.PURCHASE)
            this.tradeDirection = type;
    }
	public boolean isSale() { return this.tradeDirection == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeDirection == TradeDirection.PURCHASE; }

	public ItemStack getFilledBucket() { return FluidItemUtil.getFluidDispayItem(this.product.getFluid()); }

	public FluidTradeData(boolean validateRules) { super(validateRules); }

	public boolean hasStock(FluidTraderData trader) { return this.getStock(trader) > 0; }
	public boolean hasStock(@Nonnull TradeContext context) { return this.getStock(context) > 0; }
	public int getStock(FluidTraderData trader)
	{
		if(this.product.isEmpty())
			return 0;

		if(this.isSale())
		{
			return trader.getStorage().getAvailableFluidCount(this.product) / this.getQuantity();
		}
		else if(this.isPurchase())
		{
			//How many payments the trader can make
			return this.stockCountOfCost(trader);
		}
		return 0;
	}

	public int getStock(@Nonnull TradeContext context) {
		if(this.product.isEmpty())
			return 0;

		if(!context.hasTrader() || !(context.getTrader() instanceof FluidTraderData trader))
			return 0;

		if(trader.isCreative())
			return 1;

		if(this.isSale())
		{
			return trader.getStorage().getAvailableFluidCount(this.product) / this.getQuantity();
		}
		else if(this.isPurchase())
		{
			//How many payments the trader can make
			return this.stockCountOfCost(context);
		}
		return 0;
	}

	public boolean canAfford(TradeContext context) {
		if(this.isSale())
			return context.hasFunds(this.getCost(context));
		if(this.isPurchase())
			return context.hasFluid(this.productOfQuantity());
		return false;
	}

	public boolean hasSpace(FluidTraderData trader)
	{
		if(this.isPurchase())
			return trader.getStorage().getFillableAmount(this.product) >= this.getQuantity();
		return true;
	}

	//Flag as not valid should the tank & product not match
	public boolean isValid() { return super.isValid() && !this.product.isEmpty(); }

	@Override
	public CompoundTag getAsNBT()
	{
		CompoundTag compound = super.getAsNBT();

		compound.put("Trade", this.product.writeToNBT(new CompoundTag()));
		compound.putInt("Quantity", this.bucketQuantity);
		//compound.putInt("PendingDrain", this.pendingDrain);
		compound.putString("TradeType", this.tradeDirection.name());

		return compound;
	}

	@Override
	public void loadFromNBT(CompoundTag compound)
	{
		super.loadFromNBT(compound);
		//Load the product
		this.product = FluidStack.loadFluidStackFromNBT(compound.getCompound("Trade"));
		//Load the quantity
		if(compound.contains("Quantity", Tag.TAG_INT))
			this.bucketQuantity = compound.getInt("Quantity");
		//Load the trade type
		this.tradeDirection = loadTradeType(compound.getString("TradeType"));

	}

	public static TradeDirection loadTradeType(String name)
	{
		try {
			return TradeDirection.valueOf(name);
		} catch (IllegalArgumentException e) {
            LCTech.LOGGER.error("Could not load '{}' as a TradeType.", name);
			return TradeDirection.SALE;
		}
	}

	public static List<FluidTradeData> listOfSize(int tradeCount, boolean validateRules)
	{
		List<FluidTradeData> list = new ArrayList<>();
		while(list.size() < tradeCount)
		{
			list.add(new FluidTradeData(validateRules));
		}
		return list;
	}

	public static void WriteNBTList(List<FluidTradeData> tradeList, CompoundTag compound)
	{
		WriteNBTList(tradeList, compound, TradeData.DEFAULT_KEY);
	}

	public static void WriteNBTList(List<FluidTradeData> tradeList, CompoundTag compound, String tag)
	{
		ListTag list = new ListTag();
		for (FluidTradeData fluidTradeData : tradeList) {
			list.add(fluidTradeData.getAsNBT());
			//LCTech.LOGGER.info("Wrote to NBT List: \n" + tradeList.get(i).getAsNBT().toString());
		}
		compound.put(tag, list);
	}

	public static List<FluidTradeData> LoadNBTList(CompoundTag compound, boolean validateRules)
	{
		return LoadNBTList(compound, TradeData.DEFAULT_KEY, validateRules);
	}

	public static List<FluidTradeData> LoadNBTList(CompoundTag compound, String tag, boolean validateRules)
	{

		if(!compound.contains(tag))
			return listOfSize(1, validateRules);

		ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);

		List<FluidTradeData> tradeData = new ArrayList<>();

		for(int i = 0; i < list.size(); i++)
		{
			tradeData.add(loadData(list.getCompound(i), validateRules));
		}

		return tradeData;
	}

	public static FluidTradeData loadData(CompoundTag compound, boolean validateRules) {
		FluidTradeData trade = new FluidTradeData(validateRules);
		trade.loadFromNBT(compound);
		return trade;
	}

	@Override
	public TradeComparisonResult compare(TradeData otherTrade) {
		TradeComparisonResult result = new TradeComparisonResult();
		if(otherTrade instanceof FluidTradeData otherFluidTrade)
		{
			//Flag as compatible
			result.setCompatible();
			//Compare product
			result.addProductResult(ProductComparisonResult.CompareFluid(this.productOfQuantity(), otherFluidTrade.productOfQuantity()));
			//Compare prices
			result.comparePrices(this.getCost(), otherTrade.getCost());
			//Compare types
			result.setTypeResult(this.tradeDirection == otherFluidTrade.tradeDirection);
		}
		//Return the comparison results
		return result;
	}

	@Override
	public boolean AcceptableDifferences(TradeComparisonResult result) {
		//Confirm the types match
		if(!result.TypeMatches() || !result.isCompatible())
			return false;
		//Comfirm the product is acceptable
		if(result.getProductResultCount() <= 0)
			return false;
		//Product result
		ProductComparisonResult productResult = result.getProductResult(0);
		if(productResult.SameProductType() && productResult.SameProductNBT())
		{
			if(this.isSale())
			{
				//Product should be greater than or equal to pass
				if(productResult.ProductQuantityDifference() < 0)
					return false;
			}
			else if(this.isPurchase())
			{
				//Purchase product should be less than or equal to pass
				if(productResult.ProductQuantityDifference() > 0)
					return false;
			}
		}
		else //Fluid & tag don't match. Failure.
			return false;
		//Product is acceptable, now check the price
		if(this.isSale() && result.isPriceExpensive())
			return false;
		if(this.isPurchase() && result.isPriceCheaper())
			return false;

		//Product, price, and types are all acceptable
		return true;
	}

	@Override
	public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) {
		List<Component> list = new ArrayList<>();
		//Price check
		if(!differences.PriceMatches())
		{
			//Price difference (intended - actual = difference)
			MoneyValue difference = differences.priceDifference();
			ChatFormatting moreColor = this.isSale() ? ChatFormatting.RED : ChatFormatting.GOLD;
			ChatFormatting lessColor = this.isSale() ? ChatFormatting.GOLD : ChatFormatting.RED;
			if(differences.isPriceExpensive()) //More expensive
				list.add(LCText.GUI_TRADE_DIFFERENCE_EXPENSIVE.get(difference.getText()).withStyle(moreColor));
			else //Cheaper
				list.add(LCText.GUI_TRADE_DIFFERENCE_CHEAPER.get(difference.getText()).withStyle(lessColor));
		}
		if(differences.getProductResultCount() > 0)
		{
			Component directionName = this.isSale() ? TechText.GUI_TRADE_DIFFERENCE_PRODUCT_SALE.get() : TechText.GUI_TRADE_DIFFERENCE_PRODUCT_PURCHASE.get();
			ProductComparisonResult productCheck = differences.getProductResult(0);
			if(!productCheck.SameProductType())
				list.add(TechText.GUI_TRADE_DIFFERENCE_FLUID_TYPE.get(directionName).withStyle(ChatFormatting.RED));
			else
			{
				if(!productCheck.SameProductNBT())
					list.add(TechText.GUI_TRADE_DIFFERENCE_FLUID_NBT.getWithStyle(ChatFormatting.RED));
				else if(!productCheck.SameProductQuantity())
				{
					int quantityDifference = productCheck.ProductQuantityDifference();
					ChatFormatting moreColor = this.isPurchase() ? ChatFormatting.RED : ChatFormatting.GOLD;
					ChatFormatting lessColor = this.isSale() ? ChatFormatting.RED : ChatFormatting.GOLD;
					if(quantityDifference > 0) //More fluids
						list.add(TechText.GUI_TRADE_DIFFERENCE_FLUID_MORE.get(directionName, FluidFormatUtil.formatFluidAmount(quantityDifference)).withStyle(moreColor));
					else //Less items
						list.add(TechText.GUI_TRADE_DIFFERENCE_FLUID_LESS.get(directionName, FluidFormatUtil.formatFluidAmount(-quantityDifference)).withStyle(lessColor));
				}
			}
		}

		return list;
	}

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new FluidTradeButtonRenderer(this); }

	@Override
	public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof FluidTraderData trader)
		{
			int tradeIndex = trader.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, LazyPacketData.simpleInt("TradeIndex", tradeIndex).setInt("StartingSlot", -1));
			}
			if(this.isPurchase())
			{
				if(this.onProductInteraction(tab, tradeIndex, trader, data, heldItem))
					tab.SendInputInteractionMessage(tradeIndex, index, data, heldItem);
			}
		}
	}

	@Override
	public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof FluidTraderData trader)
		{
			int tradeIndex = trader.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				if(this.onProductInteraction(tab, tradeIndex, trader, data, heldItem))
					tab.SendOutputInteractionMessage(tradeIndex, index, data, heldItem);
			}
			else if(this.isPurchase())
			{
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, LazyPacketData.simpleInt("TradeIndex", tradeIndex).setInt("StartingSlot", -1));
			}
		}
	}

	private boolean onProductInteraction(@Nonnull BasicTradeEditTab tab, int tradeIndex, @Nonnull FluidTraderData trader, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem)
	{
		//Set the fluid to the held fluid
		if(data.shiftHeld() || (heldItem.isEmpty() && this.product.isEmpty()))
		{
			//Open fluid edit
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, LazyPacketData.simpleInt("TradeIndex", tradeIndex).setInt("StartingSlot", 0));
			return false;
		}
		else
		{
			FluidStack heldFluid = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
			if(!heldFluid.isEmpty())
			{
				this.setProduct(heldFluid);
				trader.markTradesDirty();
				if(trader.getStorage().refactorTanks())
					trader.markStorageDirty();
                LCTech.LOGGER.debug("Set Fluid from held stack on the {}", DebugUtil.getSideText(tab.menu.isClient()));
			}
			else if(!this.product.isEmpty())
			{
				this.setProduct(FluidStack.EMPTY);
				trader.markTradesDirty();
				if(trader.getStorage().refactorTanks())
					trader.markStorageDirty();
                LCTech.LOGGER.debug("Cleared Fluid on the {}", DebugUtil.getSideText(tab.menu.isClient()));
			}
			else
                LCTech.LOGGER.debug("Doing nothing as both the held Fluid and the current Product are empty on the {}", DebugUtil.getSideText(tab.menu.isClient()));

			return tab.menu.isClient();
		}
	}

	@Override
	public void OnInteraction(@Nonnull BasicTradeEditTab tab, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) { }

}