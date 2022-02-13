package io.github.lightman314.lctech.trader.energy;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.gui.screen.TradeEnergyPriceScreen;
import io.github.lightman314.lctech.common.logger.EnergyShopLogger;
import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.upgrades.UpgradeType;
import io.github.lightman314.lctech.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public interface IEnergyTrader extends ITrader, IUpgradeable, ITradeRuleHandler, ILoggerSupport<EnergyShopLogger> {

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(UpgradeType.ENERGY_CAPACITY);
	
	public default boolean allowUpgrade(UpgradeType type) {
		return ALLOWED_UPGRADES.contains(type);
	}

	public static final int DEFAULT_MAX_ENERGY = 100000;
	
	//Trade
	public EnergyTradeData getTrade(int tradeIndex);
	public List<EnergyTradeData> getAllTrades();
	public void markTradesDirty();
	//Energy Info
	public int getPendingDrain();
	public void addPendingDrain(int amount);
	public void shrinkPendingDrain(int amount);
	public int getAvailableEnergy();
	public default int getDrainableEnergy() {
		if(this.getEnergySettings().isAlwaysDrainMode())
		{
			//Cannot drain from creative traders if they're in "ALWAYS" drain mode
			return this.getCoreSettings().isCreative() ? 0 : this.getAvailableEnergy();
		}
		else if(this.getEnergySettings().isPurchaseDrainMode())
		{
			//Allow draining up to the purchasable amount when in purchase mode (confirm that we have the energy available if not in creative)
			return this.getCoreSettings().isCreative() ? this.getPendingDrain() : Math.min(this.getPendingDrain(), this.getTotalEnergy());
		}
		return 0;
	}
	public int getTotalEnergy();
	public int getMaxEnergy();
	public void shrinkEnergy(int amount);
	public void addEnergy(int amount);
	public void markEnergyStorageDirty();
	//Energy Settings
	public EnergyTraderSettings getEnergySettings();
	public void markEnergySettingsDirty();
	//Interaction
	public boolean canFillExternally();
	public boolean canDrainExternally();
	public TradeEnergyHandler getEnergyHandler();
	//Money Interactions
	public void addStoredMoney(CoinValue price);
	public void removeStoredMoney(CoinValue price);
	//Upgrade Interactions
	public Container getUpgradeInventory();
	public void reapplyUpgrades();
	public void markUpgradesDirty();
	//Open menu functions
	public void sendOpenTraderMessage();
	public void sendOpenStorageMessage();
	public void sendClearLogMessage();
	public ITradeRuleScreenHandler getRuleScreenHandler();
	public void sendPriceMessage(TradeEnergyPriceScreen.TradePriceData priceData);
	public void sendUpdateTradeRuleMessage(List<TradeRule> newRules);
	
	default PreTradeEvent runPreTradeEvent(Player player, int tradeIndex) { return this.runPreTradeEvent(PlayerReference.of(player), tradeIndex); }
	default PreTradeEvent runPreTradeEvent(PlayerReference player, int tradeIndex)
	{
		EnergyTradeData trade = this.getTrade(tradeIndex);
		PreTradeEvent event = new PreTradeEvent(player, trade, () -> this);
		trade.beforeTrade(event);
		if(this instanceof ITradeRuleHandler)
			((ITradeRuleHandler)this).beforeTrade(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	default TradeCostEvent runTradeCostEvent(Player player, int tradeIndex) { return this.runTradeCostEvent(PlayerReference.of(player), tradeIndex); }
	default TradeCostEvent runTradeCostEvent(PlayerReference player, int tradeIndex)
	{
		EnergyTradeData trade = this.getTrade(tradeIndex);
		TradeCostEvent event = new TradeCostEvent(player, trade, () -> this);
		trade.tradeCost(event);
		if(this instanceof ITradeRuleHandler)
			((ITradeRuleHandler)this).tradeCost(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	default void runPostTradeEvent(Player player, int tradeIndex, CoinValue pricePaid) { this.runPostTradeEvent(PlayerReference.of(player), tradeIndex, pricePaid); }
	default void runPostTradeEvent(PlayerReference player, int tradeIndex, CoinValue pricePaid)
	{
		EnergyTradeData trade = this.getTrade(tradeIndex);
		PostTradeEvent event = new PostTradeEvent(player, trade, () -> this, pricePaid);
		trade.afterTrade(event);
		if(event.isDirty())
		{
			this.markTradesDirty();
			event.clean();
		}
		if(this instanceof ITradeRuleHandler)
		{
			((ITradeRuleHandler)this).afterTrade(event);
			if(event.isDirty())
			{
				((ITradeRuleHandler)this).markRulesDirty();
				event.clean();
			}
		}
		MinecraftForge.EVENT_BUS.post(event);
	}
	
	public static List<Component> getEnergyHoverTooltip(IEnergyTrader trader)
	{
		List<Component> tooltip = Lists.newArrayList();
		tooltip.add(new TextComponent(EnergyUtil.formatEnergyAmount(trader.getTotalEnergy()) + "/" + EnergyUtil.formatEnergyAmount(trader.getMaxEnergy())).withStyle(ChatFormatting.AQUA));
		if(trader.getPendingDrain() > 0)
		{
			tooltip.add(new TranslatableComponent("gui.lctech.energytrade.pending_drain", EnergyUtil.formatEnergyAmount(trader.getPendingDrain())).withStyle(ChatFormatting.AQUA));
		}
		return tooltip;
	}
	
}
