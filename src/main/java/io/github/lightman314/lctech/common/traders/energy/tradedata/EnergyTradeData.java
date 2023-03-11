package io.github.lightman314.lctech.common.traders.energy.tradedata;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.client.EnergyTradeButtonRenderer;
import io.github.lightman314.lctech.common.util.EnergyUtil;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

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
	public boolean hasStock(EnergyTraderData trader, PlayerEntity player) { return this.getStock(trader, player) > 0; }
	public boolean hasStock(EnergyTraderData trader, PlayerReference player) { return this.getStock(trader, player) > 0; }
	public int getStock(EnergyTraderData trader) { return this.getStock(trader, (PlayerReference)null); }
	public int getStock(EnergyTraderData trader, PlayerEntity player) { return this.getStock(trader, PlayerReference.of(player)); }
	public int getStock(EnergyTraderData trader, PlayerReference player) {
		if(this.amount <= 0)
			return 0;

		if(this.isSale())
		{
			return trader.getAvailableEnergy() / this.amount;
		}
		else if(this.isPurchase())
		{
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
		if(this.amount <= 0)
			return 0;

		if(!context.hasTrader() || !(context.getTrader() instanceof EnergyTraderData))
			return 0;

		EnergyTraderData trader = (EnergyTraderData)context.getTrader();

		if(trader.isCreative())
			return 1;

		if(this.isSale())
		{
			return trader.getAvailableEnergy() / this.amount;
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
	public CompoundNBT getAsNBT()
	{
		CompoundNBT compound = super.getAsNBT();

		compound.putInt("Amount", this.amount);
		compound.putString("TradeType", this.tradeDirection.name());

		return compound;
	}

	@Override
	public void loadFromNBT(CompoundNBT compound)
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

	public static void WriteNBTList(List<EnergyTradeData> tradeList, CompoundNBT compound)
	{
		WriteNBTList(tradeList, compound, TradeData.DEFAULT_KEY);
	}

	public static void WriteNBTList(List<EnergyTradeData> tradeList, CompoundNBT compound, String tag)
	{
		ListNBT list = new ListNBT();
		for (EnergyTradeData energyTradeData : tradeList)
			list.add(energyTradeData.getAsNBT());
		compound.put(tag, list);
	}

	public static List<EnergyTradeData> LoadNBTList(CompoundNBT compound, boolean validateRules)
	{
		return LoadNBTList(compound, TradeData.DEFAULT_KEY, validateRules);
	}

	public static List<EnergyTradeData> LoadNBTList(CompoundNBT compound, String tag, boolean validateRules)
	{

		if(!compound.contains(tag))
			return listOfSize(1, validateRules);

		List<EnergyTradeData> tradeData = new ArrayList<>();

		ListNBT list = compound.getList(tag, Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < list.size(); ++i)
			tradeData.add(loadData(list.getCompound(i), validateRules));
		return tradeData;
	}

	public static EnergyTradeData loadData(CompoundNBT compound, boolean validateRules) {
		EnergyTradeData trade = new EnergyTradeData(validateRules);
		trade.loadFromNBT(compound);
		return trade;
	}

	@Override
	public TradeComparisonResult compare(TradeData otherTrade) {
		TradeComparisonResult result = new TradeComparisonResult();
		if(otherTrade instanceof EnergyTradeData)
		{
			EnergyTradeData otherEnergyTrade = (EnergyTradeData)otherTrade;
			//Flag as compatible
			result.setCompatible();
			//Compare product
			result.addProductResult(ProductComparisonResult.CompareEnergy(this.getAmount(), otherEnergyTrade.getAmount()));
			//Compare prices
			result.setPriceResult(this.getCost().getRawValue() - otherTrade.getCost().getRawValue());
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
	public List<ITextComponent> GetDifferenceWarnings(TradeComparisonResult differences) {
		List<ITextComponent> list = new ArrayList<>();
		//Price check
		if(!differences.PriceMatches())
		{
			//Price difference (intended - actual = difference)
			long difference = differences.priceDifference();
			if(difference < 0) //More expensive
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.expensive", MoneyUtil.getStringOfValue(-difference)).withStyle(TextFormatting.RED));
			else //Cheaper
				list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.cheaper", MoneyUtil.getStringOfValue(difference)).withStyle(TextFormatting.RED));
		}
		if(differences.getProductResultCount() > 0)
		{
			ITextComponent directionName = this.isSale() ? EasyText.translatable("gui.lctech.interface.difference.product.sale") : EasyText.translatable("gui.lctech.interface.difference.product.purchase");
			ProductComparisonResult productCheck = differences.getProductResult(0);
			if(!productCheck.SameProductType())
				list.add(EasyText.translatable("gui.lctech.interface.fluid.difference.fluidtype", directionName).withStyle(TextFormatting.RED));
			if(!productCheck.SameProductQuantity())
			{
				int quantityDifference = productCheck.ProductQuantityDifference();
				if(quantityDifference < 0) //More items
					list.add(EasyText.translatable("gui.lctech.interface.energy.difference.quantity.more", directionName, EnergyUtil.formatEnergyAmount(-quantityDifference)).withStyle(TextFormatting.RED));
				else //Less items
					list.add(EasyText.translatable("gui.lctech.interface.energy.difference.quantity.less", directionName, EnergyUtil.formatEnergyAmount(quantityDifference)).withStyle(TextFormatting.RED));
			}
		}

		return list;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRenderManager<?> getButtonRenderer() { return new EnergyTradeButtonRenderer(this); }

	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof EnergyTraderData)
		{
			EnergyTraderData trader = (EnergyTraderData)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? -1 : 0;
			CompoundNBT extraData = new CompoundNBT();
			extraData.putInt("TradeIndex", tradeIndex);
			extraData.putInt("StartingSlot", openSlot);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}

	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof EnergyTraderData)
		{
			EnergyTraderData trader = (EnergyTraderData)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? 0 : -1;
			CompoundNBT extraData = new CompoundNBT();
			extraData.putInt("TradeIndex", tradeIndex);
			extraData.putInt("StartingSlot", openSlot);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}

	@Override
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int mouseX, int mouseY, int button, ItemStack heldItem) {

	}
	
}