package io.github.lightman314.lctech.trader.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy.EnergyStorageClientTab;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil.TextFormatting;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeComparisonResult.ProductComparisonResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyTradeData extends TradeData {
	
	int amount = 0;
	public int getAmount() { return this.amount; }
	public void setAmount(int newAmount) { this.amount = Math.max(0, newAmount); }
	
	TradeDirection tradeDirection = TradeDirection.SALE;
	public TradeDirection getTradeDirection() { return this.tradeDirection; }
	public void setTradeDirection(TradeDirection direction) { this.tradeDirection = direction; }
	public boolean isSale() { return this.tradeDirection == TradeDirection.SALE; }
	public boolean isPurchase() { return this.tradeDirection == TradeDirection.PURCHASE; }
	
	public EnergyTradeData() { }
	
	public boolean hasStock(IEnergyTrader trader) { return this.getStock(trader) > 0; }
	public boolean hasStock(IEnergyTrader trader, Player player) { return this.getStock(trader, player) > 0; }
	public boolean hasStock(IEnergyTrader trader, PlayerReference player) { return this.getStock(trader, player) > 0; }
	public int getStock(IEnergyTrader trader) { return this.getStock(trader, (PlayerReference)null); }
	public int getStock(IEnergyTrader trader, Player player) { return this.getStock(trader, PlayerReference.of(player)); }
	public int getStock(IEnergyTrader trader, PlayerReference player) {
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
			CoinValue price = player == null ? this.cost : trader.runTradeCostEvent(player, trader.getAllTrades().indexOf(this)).getCostResult();
			return (int)(coinValue/price.getRawValue());
		}
		return 0;
	}
	public int getStock(TradeContext context) {
		if(this.amount <= 0)
			return 0;
		
		if(!context.hasTrader() || !(context.getTrader() instanceof IEnergyTrader))
			return 0;
		
		IEnergyTrader trader = (IEnergyTrader)context.getTrader();
		if(trader.getCoreSettings().isCreative())
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
	
	public boolean hasSpace(IEnergyTrader trader)
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
	
	public static List<EnergyTradeData> listOfSize(int tradeCount)
	{
		List<EnergyTradeData> list = new ArrayList<>();
		while(list.size() < tradeCount)
		{
			list.add(new EnergyTradeData());
		}
		return list;
	}
	
	public static CompoundTag WriteNBTList(List<EnergyTradeData> tradeList, CompoundTag compound)
	{
		return WriteNBTList(tradeList, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static CompoundTag WriteNBTList(List<EnergyTradeData> tradeList, CompoundTag compound, String tag)
	{
		ListTag list = new ListTag();
		for(int i = 0; i < tradeList.size(); ++i)
		{
			list.add(tradeList.get(i).getAsNBT());
		}
		compound.put(tag, list);
		return compound;
	}
	
	public static List<EnergyTradeData> LoadNBTList(int tradeCount, CompoundTag compound)
	{
		return LoadNBTList(tradeCount, compound, ItemTradeData.DEFAULT_KEY);
	}
	
	public static List<EnergyTradeData> LoadNBTList(int tradeCount, CompoundTag compound, String tag)
	{
		List<EnergyTradeData> tradeData = listOfSize(tradeCount);
		
		if(!compound.contains(tag))
			return tradeData;
		
		ListTag list = compound.getList(tag,  Tag.TAG_COMPOUND);
		for(int i = 0; i < list.size() && i < tradeCount; ++i)
		{
			tradeData.get(i).loadFromNBT(list.getCompound(i));
		}
		
		return tradeData;
	}
	
	public static EnergyTradeData loadData(CompoundTag compound) {
		EnergyTradeData trade = new EnergyTradeData();
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
	public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) {
		List<Component> list = new ArrayList<>();
		//Price check
		if(!differences.PriceMatches())
		{
			//Price difference (intended - actual = difference)
			long difference = differences.priceDifference();
			if(difference < 0) //More expensive
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.difference.expensive", MoneyUtil.getStringOfValue(-difference)).withStyle(ChatFormatting.RED));
			else //Cheaper
				list.add(new TranslatableComponent("gui.lightmanscurrency.interface.difference.cheaper", MoneyUtil.getStringOfValue(difference)).withStyle(ChatFormatting.RED));
		}
		if(differences.getProductResultCount() > 0)
		{
			Component directionName = this.isSale() ? new TranslatableComponent("gui.lctech.interface.difference.product.sale") : new TranslatableComponent("gui.lctech.interface.difference.product.purchase");
			ProductComparisonResult productCheck = differences.getProductResult(0);
			if(!productCheck.SameProductType())
				list.add(new TranslatableComponent("gui.lctech.interface.fluid.difference.fluidtype", directionName).withStyle(ChatFormatting.RED));
			if(!productCheck.SameProductQuantity())
			{
				int quantityDifference = productCheck.ProductQuantityDifference();
				if(quantityDifference < 0) //More items
					list.add(new TranslatableComponent("gui.lctech.interface.energy.difference.quantity.more", directionName, EnergyUtil.formatEnergyAmount(-quantityDifference)).withStyle(ChatFormatting.RED));
				else //Less items
					list.add(new TranslatableComponent("gui.lctech.interface.energy.difference.quantity.less", directionName, EnergyUtil.formatEnergyAmount(quantityDifference)).withStyle(ChatFormatting.RED));
			}
		}
		
		return list;
	}
	
	@Override
	public List<DisplayEntry> getInputDisplays(TradeContext context) {
		if(this.isSale())
			return this.getCostEntry(context);
		else
			return this.getProductEntry();
	}
	
	@Override
	public List<DisplayEntry> getOutputDisplays(TradeContext context) {
		if(this.isSale())
			return this.getProductEntry();
		else
			return this.getCostEntry(context);
	}
	
	private List<DisplayEntry> getCostEntry(TradeContext context) {
		return Lists.newArrayList(DisplayEntry.of(this.getCost(context)));
	}
	
	private List<DisplayEntry> getProductEntry() { return Lists.newArrayList(DisplayEntry.of(new TextComponent(EnergyUtil.formatEnergyAmount(this.amount)), TextFormatting.create().centered().middle())); }
	
	@Override
	public DisplayData inputDisplayArea(TradeContext context) {
		if(this.isSale())
			return new DisplayData(1, 1, this.tradeButtonWidth(context) - 2, 16);
		else
			return new DisplayData(1, 1, this.tradeButtonWidth(context) - 2, 10);
	}
	
	@Override
	public DisplayData outputDisplayArea(TradeContext context) {
		if(this.isSale())
			return new DisplayData(1, 24, this.tradeButtonWidth(context) - 2, 10);
		else
			return new DisplayData(1, 18, this.tradeButtonWidth(context) - 2, 16);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderAdditional(AbstractWidget button, PoseStack pose, int mouseX, int mouseY, TradeContext context) {
		//Manually render the arrow
		RenderSystem.setShaderTexture(0, EnergyStorageClientTab.GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		Pair<Integer,Integer> position = this.alertPosition(context);
		button.blit(pose, button.x + position.getFirst(), button.y + position.getSecond(), 54, 0, 22, 18);
		
		//Manually render the drainable icon
		if(this.allowsDrainage(context))
		{
			button.blit(pose, button.x + this.tradeButtonWidth(context) - 10, button.y + position.getSecond() + 5, 36, 18, 8, 8);
		}
	}
	
	@Override
	public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) {
		if(this.allowsDrainage(context))
		{
			Pair<Integer,Integer> arrowPos = this.alertPosition(context);
			int width = this.tradeButtonWidth(context);
			if(mouseX >= width - 10 && mouseX < width - 2 && mouseY >= arrowPos.getSecond() + 5 && mouseY < arrowPos.getSecond() + 13)
				return Lists.newArrayList(new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drainable"));
		}
		return null;
	}
	
	private boolean allowsDrainage(TradeContext context) {
		if(context.isStorageMode || !this.isSale())
			return false;
		if(context.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)context.getTrader();
			return trader.canDrainExternally() && trader.getEnergySettings().isPurchaseDrainMode();
		}
		return false;
	}
	
	@Override
	public boolean hasArrow(TradeContext context) { return false; }
	
	@Override
	public Pair<Integer, Integer> arrowPosition(TradeContext context) { return alertPosition(context); }
	
	@Override
	public Pair<Integer, Integer> alertPosition(TradeContext context) {
		return Pair.of(26, this.isSale() ? 13 : 7);
	}
	
	@Override
	public List<AlertData> getAlertData(TradeContext context) {
		if(context.isStorageMode)
			return null;
		List<AlertData> alerts = new ArrayList<>();
		if(context.hasTrader() && context.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)context.getTrader();
			if(!trader.isCreative())
			{
				if(this.getStock(context) <= 0)
					alerts.add(AlertData.warn(new TranslatableComponent("tooltip.lightmanscurrency.outofstock")));
				if(!this.hasSpace(trader))
					alerts.add(AlertData.warn(new TranslatableComponent("tooltip.lightmanscurrency.outofspace")));
			}
			if(!this.canAfford(context))
				alerts.add(AlertData.warn(new TranslatableComponent("tooltip.lightmanscurrency.cannotafford")));
		}
		if(this.isSale() && !(context.canFitEnergy(this.amount) || this.allowsDrainage(context)))
			alerts.add(AlertData.warn(new TranslatableComponent("tooltip.lightmanscurrency.nooutputcontainer")));
		
		this.addTradeRuleAlertData(alerts, context);
		return alerts;
	}
	
	
	
	@Override
	public int tradeButtonHeight(TradeContext context) {
		return 36;
	}
	
	@Override
	public int tradeButtonWidth(TradeContext context) {
		return 70;
	}
	
	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? -1 : 0;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			extraData.putInt("StartingSlot", openSlot);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}
	
	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int index, int button, ItemStack heldItem) {
		if(tab.menu.getTrader() instanceof IEnergyTrader)
		{
			IEnergyTrader trader = (IEnergyTrader)tab.menu.getTrader();
			int tradeIndex = trader.getAllTrades().indexOf(this);
			if(tradeIndex < 0)
				return;
			int openSlot = this.isSale() ? 0 : -1;
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			extraData.putInt("StartingSlot", openSlot);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
		}
	}
	
	@Override
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientMessage, int mouseX, int mouseY, int button, ItemStack heldItem) {
		
	}
	

}
