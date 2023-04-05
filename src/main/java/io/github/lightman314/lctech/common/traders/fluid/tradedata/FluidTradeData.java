package io.github.lightman314.lctech.common.traders.fluid.tradedata;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.client.FluidTradeButtonRenderer;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidTradeData extends TradeData {

	FluidStack product = FluidStack.EMPTY;
	public FluidStack getProduct() { return this.product.copy(); }
	public void setProduct(FluidStack newProduct) {
		this.product = newProduct.copy();
		if(this.product.getFluid() != Fluids.EMPTY)
			this.product.setAmount(FluidAttributes.BUCKET_VOLUME);
	}
	public FluidStack productOfQuantity()
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
	public int getMaxBucketQuantity() { return Math.max(1, TechConfig.SERVER.fluidTradeMaxQuantity.get()); }

	TradeDirection tradeDirection = TradeDirection.SALE;
	public TradeDirection getTradeDirection() { return this.tradeDirection; }
	public void setTradeDirection(TradeDirection type) { this.tradeDirection = type; }
	public boolean isSale() { return this.tradeDirection == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeDirection == TradeDirection.PURCHASE; }

	public ItemStack getFilledBucket() { return FluidUtil.getFilledBucket(this.product);}

	public FluidTradeData(boolean validateRules) { super(validateRules); }

	public boolean hasStock(FluidTraderData trader) { return this.getStock(trader) > 0; }
	public boolean hasStock(FluidTraderData trader, Player player) { return this.getStock(trader, player) > 0; }
	public boolean hasStock(FluidTraderData trader, PlayerReference player) { return this.getStock(trader, player) > 0; }
	public int getStock(FluidTraderData trader) { return this.getStock(trader, (PlayerReference)null); }
	public int getStock(FluidTraderData trader, Player player) { return this.getStock(trader, PlayerReference.of(player)); }
	public int getStock(FluidTraderData trader, PlayerReference player)
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
			if(this.cost.isFree())
				return 1;
			if(cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			CoinValue price = player == null ? this.cost : trader.runTradeCostEvent(player, this).getCostResult();
			return (int)(coinValue/price.getRawValue());
		}
		return 0;
	}

	public int getStock(TradeContext context) {
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
			if(this.cost.isFree())
				return 1;
			if(cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			CoinValue price = this.getCost(context);
			return (int)(coinValue/price.getRawValue());
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
			LCTech.LOGGER.error("Could not load '" + name + "' as a TradeType.");
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
			result.setPriceResult(this.getCost().getRawValue() - otherTrade.getCost().getRawValue());
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
				if(productResult.ProductQuantityDifference() > 0)
					return false;
			}
			else if(this.isPurchase())
			{
				//Purchase product should be less than or equal to pass
				if(productResult.ProductQuantityDifference() < 0)
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
			long difference = differences.priceDifference();
			if(difference < 0) //More expensive
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.expensive", MoneyUtil.getStringOfValue(-difference)).withStyle(ChatFormatting.RED));
			else //Cheaper
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.cheaper", MoneyUtil.getStringOfValue(difference)).withStyle(ChatFormatting.RED));
		}
		if(differences.getProductResultCount() > 0)
		{
			Component directionName = this.isSale() ? EasyText.translatable("gui.lctech.interface.difference.product.sale") : EasyText.translatable("gui.lctech.interface.difference.product.purchase");
			ProductComparisonResult productCheck = differences.getProductResult(0);
			if(!productCheck.SameProductType())
				list.add(EasyText.translatable("gui.lctech.interface.fluid.difference.fluidtype", directionName).withStyle(ChatFormatting.RED));
			else
			{
				if(!productCheck.SameProductNBT())
					list.add(EasyText.translatable("gui.lctech.interface.fluid.difference.fluidnbt"));
				else if(!productCheck.SameProductQuantity())
				{
					int quantityDifference = productCheck.ProductQuantityDifference();
					if(quantityDifference < 0) //More items
						list.add(EasyText.translatable("gui.lctech.interface.fluid.difference.quantity.more", directionName, FluidFormatUtil.formatFluidAmount(-quantityDifference)).withStyle(ChatFormatting.RED));
					else //Less items
						list.add(EasyText.translatable("gui.lctech.interface.fluid.difference.quantity.less", directionName, FluidFormatUtil.formatFluidAmount(quantityDifference)).withStyle(ChatFormatting.RED));
				}
			}
		}

		return list;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new FluidTradeButtonRenderer(this); }

	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof FluidTraderData trader)
		{
			int tradeIndex = trader.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				CompoundTag extraData = new CompoundTag();
				extraData.putInt("TradeIndex", tradeIndex);
				extraData.putInt("StartingSlot", -1);
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			}
			if(this.isPurchase())
			{
				//Set the fluid to the held fluid
				if(heldItem.isEmpty() && this.product.isEmpty())
				{
					//Open fluid edit
					CompoundTag extraData = new CompoundTag();
					extraData.putInt("TradeIndex", tradeIndex);
					extraData.putInt("StartingSlot", 0);
					tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
				}
				else
				{
					FluidStack heldFluid = FluidUtil.getFluidContained(heldItem).orElse(null);
					if(heldFluid != null)
					{
						this.setProduct(heldFluid);
						trader.markTradesDirty();
						if(trader.getStorage().refactorTanks())
							trader.markStorageDirty();
					}
					if(tab.menu.isClient())
						tab.sendInputInteractionMessage(tradeIndex, index, button, heldItem);
				}
			}
		}
	}

	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof FluidTraderData trader)
		{
			int tradeIndex = trader.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			if(this.isSale())
			{
				//Set the fluid to the held fluid
				if(heldItem.isEmpty() && this.product.isEmpty())
				{
					//Open fluid edit
					CompoundTag extraData = new CompoundTag();
					extraData.putInt("TradeIndex", tradeIndex);
					extraData.putInt("StartingSlot", 0);
					tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
				}
				else
				{
					FluidStack heldFluid = FluidUtil.getFluidContained(heldItem).orElse(null);
					if(heldFluid != null)
					{
						this.setProduct(heldFluid);
						trader.markTradesDirty();
						if(trader.getStorage().refactorTanks())
							trader.markStorageDirty();
					}
					if(tab.menu.isClient())
						tab.sendOutputInteractionMessage(tradeIndex, index, button, heldItem);
				}
			}
			else if(this.isPurchase())
			{
				CompoundTag extraData = new CompoundTag();
				extraData.putInt("TradeIndex", tradeIndex);
				extraData.putInt("StartingSlot", -1);
				tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			}
		}
	}

	@Override
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof FluidTraderData trader)
		{
			int tradeIndex = trader.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			extraData.putInt("StartingSlot", -1);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}

}