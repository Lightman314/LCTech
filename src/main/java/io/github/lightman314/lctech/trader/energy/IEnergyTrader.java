package io.github.lightman314.lctech.trader.energy;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.logger.EnergyShopLogger;
import io.github.lightman314.lctech.common.notifications.types.EnergyTradeNotification;
import io.github.lightman314.lctech.menu.traderstorage.AddRemoveTradeEditTab;
import io.github.lightman314.lctech.menu.traderstorage.energy.EnergyStorageTab;
import io.github.lightman314.lctech.menu.traderstorage.energy.EnergyTradeEditTab;
import io.github.lightman314.lctech.trader.ITradeCountTrader;
import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.upgrades.TechUpgradeTypes;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITradeSource;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.common.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType.IUpgradeable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public interface IEnergyTrader extends ITrader, ITradeCountTrader, IUpgradeable, ITradeRuleHandler, ITradeRuleMessageHandler, ILoggerSupport<EnergyShopLogger>, ITradeSource<EnergyTradeData> {

	public static final int DEFAULT_TRADE_LIMIT = 4;
	
	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.ENERGY_CAPACITY);
	
	public default boolean allowUpgrade(UpgradeType type) {
		return ALLOWED_UPGRADES.contains(type);
	}
	
	public static int getDefaultMaxEnergy() { return TechConfig.SERVER.energyTraderDefaultStorage.get(); }
	
	//Trade Count Limit
	public default int getTradeCountLimit() { return 4; }
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
	//public void sendPriceMessage(TradeEnergyPriceScreen.TradePriceData priceData);
	public void sendUpdateTradeRuleMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo);
	
	default PreTradeEvent runPreTradeEvent(Player player, int tradeIndex) { return this.runPreTradeEvent(PlayerReference.of(player), tradeIndex); }
	default PreTradeEvent runPreTradeEvent(PlayerReference player, int tradeIndex)
	{
		EnergyTradeData trade = this.getTrade(tradeIndex);
		PreTradeEvent event = new PreTradeEvent(player, trade, this);
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
		TradeCostEvent event = new TradeCostEvent(player, trade, this);
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
		PostTradeEvent event = new PostTradeEvent(player, trade, this, pricePaid);
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
	
	public default TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		
		EnergyTradeData trade = this.getTrade(tradeIndex);
		
		if(trade == null || !trade.isValid())
			return TradeResult.FAIL_INVALID_TRADE;
		
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		if(this.runPreTradeEvent(context.getPlayerReference(), tradeIndex).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;
		
		//Get the cost of the trade
		CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();
		
		//Abort if not enough stock
		if(!trade.hasStock(this, context.getPlayerReference()) && !this.getCoreSettings().isCreative())
			return TradeResult.FAIL_OUT_OF_STOCK;
		
		if(trade.isSale())
		{
			
			//Confirm that the energy can be output
			if(!context.canFitEnergy(trade.getAmount()) && !(this.canDrainExternally() && this.getEnergySettings().isPurchaseDrainMode()))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			
			//Process the trades payment
			if(!context.getPayment(price))
				return TradeResult.FAIL_CANNOT_AFFORD;
			
			//We have enough money, and the trade is valid. Execute the trade
			//Give the energy
			boolean drainStorage = true;
			if(context.canFitEnergy(trade.getAmount()))
				context.fillEnergy(trade.getAmount());
			else //If nowhere to give the energy, add to the pending drain
			{
				this.addPendingDrain(trade.getAmount());
				drainStorage = false;
			}
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.isCreative());
			this.markLoggerDirty();
			
			//Push the notification
			this.getCoreSettings().pushNotification(() -> new EnergyTradeNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//Ignore internal editing if this is creative
			if(!this.getCoreSettings().isCreative())
			{
				//Remove the purchased energy from storage
				if(drainStorage)
				{
					this.shrinkEnergy(trade.getAmount());
					this.markEnergyStorageDirty();
				}
				
				//Give the paid price to storage
				this.addStoredMoney(price);					
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), tradeIndex, price);
			
			return TradeResult.SUCCESS;
			
		}
		else if(trade.isPurchase())
		{
			//Abort if not enough energy to buy
			if(!context.hasEnergy(trade.getAmount()))
				return TradeResult.FAIL_CANNOT_AFFORD;
			
			//Abort if not enough space to put the purchased fluid
			if(!trade.hasSpace(this) && !this.getCoreSettings().isCreative())
				return TradeResult.FAIL_NO_INPUT_SPACE;
			
			//Give the payment to the player
			if(!context.givePayment(price))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			
			//We have enough money, and the trade is valid. Execute the trade
			//Collect the product
			if(!context.drainEnergy(trade.getAmount()))
			{
				//Failed somehow. Take the money back
				context.getPayment(price);
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.isCreative());
			this.markLoggerDirty();
			
			//Push the notification
			this.getCoreSettings().pushNotification(() -> new EnergyTradeNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//Ignore internal editing if this is creative
			if(!this.getCoreSettings().isCreative())
			{
				//Put the purchased fluid in storage
				this.addEnergy(trade.getAmount());
				this.markEnergyStorageDirty();
				//Remove the coins from storage
				this.removeStoredMoney(price);
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), tradeIndex, price);
			
			return TradeResult.SUCCESS;
			
		}
		
		return TradeResult.FAIL_INVALID_TRADE;
	}
	
	public default void addInteractionSlots(List<InteractionSlotData> interactionSlots) {
		interactionSlots.add(EnergyInteractionSlot.INSTANCE);
	}
	
	@Override
	public default void initStorageTabs(TraderStorageMenu menu) {
		//Override of basic tab
		menu.setTab(TraderStorageTab.TAB_TRADE_BASIC, new AddRemoveTradeEditTab(menu));
		//Storage tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new EnergyStorageTab(menu));
		//Energy Trade interaction tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new EnergyTradeEditTab(menu));
	}
	
}
