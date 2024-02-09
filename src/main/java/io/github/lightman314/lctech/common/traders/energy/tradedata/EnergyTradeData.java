package io.github.lightman314.lctech.common.traders.energy.tradedata;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.client.EnergyTradeButtonRenderer;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyTradeData extends TradeData {

	int amount = 0;
	public int getAmount() { return this.amount; }
	public void setAmount(int newAmount) { this.amount = Math.max(0, newAmount); }

	TradeDirection tradeDirection = TradeDirection.SALE;
	public TradeDirection getTradeDirection() { return this.tradeDirection; }
	public void setTradeDirection(TradeDirection direction) { this.tradeDirection = direction; }
	public boolean isSale() { return this.tradeDirection == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeDirection == TradeDirection.PURCHASE; }

	public EnergyTradeData(boolean validateRules) { super(validateRules); }

	public boolean hasStock(EnergyTraderData trader) { return this.getStock(trader) > 0; }
	public boolean hasStock(TradeContext context) { return this.getStock(context) > 0; }
	public int getStock(EnergyTraderData trader) {
		if(this.amount <= 0)
			return 0;

		if(this.isSale())
		{
			return trader.getAvailableEnergy() / this.amount;
		}
		else if(this.isPurchase())
		{
			return this.stockCountOfCost(trader);
		}
		return 0;
	}
	public int getStock(TradeContext context) {
		if(this.amount <= 0)
			return 0;

		if(!context.hasTrader() || !(context.getTrader() instanceof EnergyTraderData trader))
			return 0;

		if(trader.isCreative())
			return 1;

		if(this.isSale())
		{
			return trader.getAvailableEnergy() / this.amount;
		}
		else if(this.isPurchase())
		{
			return this.stockCountOfCost(context);
		}
		return 0;
	}

	public boolean canAfford(TradeContext context) {
		if(this.isSale())
			return context.hasFunds(this.getCost(context));
		if(this.isPurchase())
			return context.hasEnergy(this.amount);
		return false;
	}

	public boolean hasSpace(EnergyTraderData trader)
	{
		if(this.isPurchase())
			return trader.getMaxEnergy() - trader.getTotalEnergy() >= this.amount;
		return true;
	}

	@Override
	public boolean isValid() {
		return super.isValid() && this.amount > 0;
	}

	@Override
	public CompoundTag getAsNBT()
	{
		CompoundTag compound = super.getAsNBT();

		compound.putInt("Amount", this.amount);
		compound.putString("TradeType", this.tradeDirection.name());

		return compound;
	}

	@Override
	public void loadFromNBT(CompoundTag compound)
	{
		super.loadFromNBT(compound);

		//Load the amount
		this.amount = compound.getInt("Amount");
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

	public static List<EnergyTradeData> listOfSize(int tradeCount, boolean validateRules)
	{
		List<EnergyTradeData> list = new ArrayList<>();
		while(list.size() < tradeCount)
			list.add(new EnergyTradeData(validateRules));
		return list;
	}

	public static void WriteNBTList(List<EnergyTradeData> tradeList, CompoundTag compound)
	{
		WriteNBTList(tradeList, compound, TradeData.DEFAULT_KEY);
	}

	public static void WriteNBTList(List<EnergyTradeData> tradeList, CompoundTag compound, String tag)
	{
		ListTag list = new ListTag();
		for (EnergyTradeData energyTradeData : tradeList)
			list.add(energyTradeData.getAsNBT());
		compound.put(tag, list);
	}

	public static List<EnergyTradeData> LoadNBTList(CompoundTag compound, boolean validateRules)
	{
		return LoadNBTList(compound, TradeData.DEFAULT_KEY, validateRules);
	}

	public static List<EnergyTradeData> LoadNBTList(CompoundTag compound, String tag, boolean validateRules)
	{

		if(!compound.contains(tag))
			return listOfSize(1, validateRules);

		List<EnergyTradeData> tradeData = new ArrayList<>();

		ListTag list = compound.getList(tag,  Tag.TAG_COMPOUND);
		for(int i = 0; i < list.size(); ++i)
			tradeData.add(loadData(list.getCompound(i), validateRules));
		return tradeData;
	}

	public static EnergyTradeData loadData(CompoundTag compound, boolean validateRules) {
		EnergyTradeData trade = new EnergyTradeData(validateRules);
		trade.loadFromNBT(compound);
		return trade;
	}

	@Override
	public TradeComparisonResult compare(TradeData otherTrade) {
		TradeComparisonResult result = new TradeComparisonResult();
		if(otherTrade instanceof EnergyTradeData otherEnergyTrade)
		{
			//Flag as compatible
			result.setCompatible();
			//Compare product
			result.addProductResult(ProductComparisonResult.CompareEnergy(this.getAmount(), otherEnergyTrade.getAmount()));
			//Compare prices
			result.comparePrices(this.getCost(), otherTrade.getCost());
			//Compare types
			result.setTypeResult(this.tradeDirection == otherEnergyTrade.tradeDirection);
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
		else //Somehow it's a different kind of energy? Well if they are flagged as not a match, I guess they don't match.
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
			if(differences.isPriceExpensive()) //More expensive
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.expensive", difference.getText()).withStyle(ChatFormatting.RED));
			else //Cheaper
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.cheaper", difference.getText()).withStyle(ChatFormatting.RED));
		}
		if(differences.getProductResultCount() > 0)
		{
			Component directionName = this.isSale() ? EasyText.translatable("gui.lctech.interface.difference.product.sale") : EasyText.translatable("gui.lctech.interface.difference.product.purchase");
			ProductComparisonResult productCheck = differences.getProductResult(0);
			if(!productCheck.SameProductType())
				list.add(EasyText.translatable("gui.lctech.interface.fluid.difference.fluidtype", directionName).withStyle(ChatFormatting.RED));
			if(!productCheck.SameProductQuantity())
			{
				int quantityDifference = productCheck.ProductQuantityDifference();
				if(quantityDifference < 0) //More items
					list.add(EasyText.translatable("gui.lctech.interface.energy.difference.quantity.more", directionName, EnergyUtil.formatEnergyAmount(-quantityDifference)).withStyle(ChatFormatting.RED));
				else //Less items
					list.add(EasyText.translatable("gui.lctech.interface.energy.difference.quantity.less", directionName, EnergyUtil.formatEnergyAmount(quantityDifference)).withStyle(ChatFormatting.RED));
			}
		}

		return list;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new EnergyTradeButtonRenderer(this); }

	@Override
	public void OnInputDisplayInteraction(BasicTradeEditTab tab, @Nullable Consumer<LazyPacketData.Builder> clientMessage, int index, int button, @Nonnull ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof EnergyTraderData trader)
		{
			int tradeIndex = trader.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? -1 : 0;
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, LazyPacketData.simpleInt("TradeIndex", tradeIndex).setInt("StartingSlot", openSlot));
		}
	}

	@Override
	public void OnOutputDisplayInteraction(BasicTradeEditTab tab, @Nullable Consumer<LazyPacketData.Builder> clientMessage, int index, int button, @Nonnull ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof EnergyTraderData trader)
		{
			int tradeIndex = trader.getTradeData().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? 0 : -1;
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, LazyPacketData.simpleInt("TradeIndex", tradeIndex).setInt("StartingSlot", openSlot));
		}
	}

	@Override
	public void OnInteraction(@Nonnull BasicTradeEditTab tab, @Nullable Consumer<LazyPacketData.Builder> clientMessage, int mouseX, int mouseY, int button, @Nonnull ItemStack heldItem) {

	}

}