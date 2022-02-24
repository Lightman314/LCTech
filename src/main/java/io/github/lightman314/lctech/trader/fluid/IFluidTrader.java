package io.github.lightman314.lctech.trader.fluid;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.gui.screen.TradeFluidPriceScreen;
import io.github.lightman314.lctech.common.logger.FluidShopLogger;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.upgrades.UpgradeType;
import io.github.lightman314.lctech.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidTrader extends ITrader, IUpgradeable, ITradeRuleHandler, ILoggerSupport<FluidShopLogger>{
	
	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(UpgradeType.FLUID_CAPACITY);
	
	public default boolean allowUpgrade(UpgradeType type) {
		return ALLOWED_UPGRADES.contains(type);
	}
	
	public FluidTradeData getTrade(int tradeIndex);
	public List<FluidTradeData> getAllTrades();
	public void markTradesDirty();
	public Container getUpgradeInventory();
	public void reapplyUpgrades();
	public TradeFluidHandler getFluidHandler();
	public boolean drainCapable();
	public void openTradeMenu(Player player);
	public void openStorageMenu(Player player);
	public void openFluidEditMenu(Player player, int tradeIndex);
	public FluidTraderSettings getFluidSettings();
	public void markFluidSettingsDirty();
	//Client send messages
	public ITradeRuleScreenHandler getRuleScreenHandler();
	public void sendSetTradeFluidMessage(int tradeIndex, FluidStack newFluid);
	public void sendToggleIconMessage(int tradeIndex, int icon);
	public void sendPriceMessage(TradeFluidPriceScreen.TradePriceData priceData);
	public void sendUpdateTradeRuleMessage(int tradeIndex, List<TradeRule> newRules);
	
	default PreTradeEvent runPreTradeEvent(Player player, int tradeIndex) { return this.runPreTradeEvent(PlayerReference.of(player), tradeIndex); }
	default PreTradeEvent runPreTradeEvent(PlayerReference player, int tradeIndex)
	{
		FluidTradeData trade = this.getTrade(tradeIndex);
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
		FluidTradeData trade = this.getTrade(tradeIndex);
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
		FluidTradeData trade = this.getTrade(tradeIndex);
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
	
	public static Supplier<IFluidTrader> BlockEntitySource(Level level, BlockPos traderPos) {
		return () -> {
			BlockEntity be = level.getBlockEntity(traderPos);
			if(be instanceof IFluidTrader)
				return (IFluidTrader)be;
			return null;
		};
	}
	
	public static Supplier<IFluidTrader> UniversalSource(Level level, UUID traderID) {
		return () -> {
			UniversalTraderData data = level.isClientSide ? ClientTradingOffice.getData(traderID) : TradingOffice.getData(traderID);
			if(data instanceof IFluidTrader)
				return (IFluidTrader)data;
			return null;
		};
	}
	
}
