package io.github.lightman314.lctech.trader.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.logger.FluidShopLogger;
import io.github.lightman314.lctech.common.notifications.types.FluidTradeNotification;
import io.github.lightman314.lctech.menu.traderstorage.fluid.FluidStorageTab;
import io.github.lightman314.lctech.menu.traderstorage.fluid.FluidTradeEditTab;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage.ITraderFluidFilter;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.upgrades.TechUpgradeTypes;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.items.UpgradeItem;
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
import io.github.lightman314.lightmanscurrency.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType.IUpgradeable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidTrader extends ITrader, IUpgradeable, ITradeRuleHandler, ITradeRuleMessageHandler, ILoggerSupport<FluidShopLogger>, ITraderFluidFilter, ITradeSource<FluidTradeData>{
	
	public static int getDefaultTankCapacity() { return TechConfig.SERVER.fluidTraderDefaultStorage.get() * FluidAttributes.BUCKET_VOLUME; }
	
	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.FLUID_CAPACITY);
	
	public default boolean allowUpgrade(UpgradeType type) {
		return ALLOWED_UPGRADES.contains(type);
	}
	
	public FluidTradeData getTrade(int tradeIndex);
	public List<FluidTradeData> getAllTrades();
	public void markTradesDirty();
	public Container getUpgradeInventory();
	public void markUpgradesDirty();
	public TraderFluidStorage getStorage();
	public void markStorageDirty();
	public TradeFluidHandler getFluidHandler();
	public boolean drainCapable();
	public void openTradeMenu(Player player);
	public void openStorageMenu(Player player);
	//public void openFluidEditMenu(Player player, int tradeIndex);
	public FluidTraderSettings getFluidSettings();
	public void markFluidSettingsDirty();
	public default int getTankCapacity() {
		int defaultCapacity = getDefaultTankCapacity();
		int tankCapacity = defaultCapacity;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < this.getUpgradeInventory().getContainerSize(); i++)
		{
			ItemStack stack = this.getUpgradeInventory().getItem(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
				if(this.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					{
						int addAmount = upgradeItem.getDefaultUpgradeData().getIntValue(CapacityUpgrade.CAPACITY);
						if(addAmount > defaultCapacity && !baseStorageCompensation)
						{
							addAmount -= defaultCapacity;
							baseStorageCompensation = true;
						}
						tankCapacity += addAmount;
					}
				}	
			}
		}
		return tankCapacity;
	}
	public default List<FluidStack> getRelevantFluids() {
		List<FluidStack> result = new ArrayList<>();
		for(FluidTradeData trade : this.getAllTrades())
		{
			FluidStack product = trade.getProduct();
			if(!product.isEmpty() && !IsInList(result, product))
				result.add(product);
		}
		return result;
	}
	
	public static boolean IsInList(List<FluidStack> list, FluidStack fluid) {
		if(fluid.isEmpty())
			return true;
		for(FluidStack query : list)
		{
			if(query.isFluidEqual(fluid))
				return true;
		}
		return false;
	}
	
	//Client send messages
	//public ITradeRuleScreenHandler getRuleScreenHandler();
	//public void sendSetTradeFluidMessage(int tradeIndex, FluidStack newFluid);
	//public void sendToggleIconMessage(int tradeIndex, int icon);
	//public void sendPriceMessage(TradeFluidPriceScreen.TradePriceData priceData);
	public void sendUpdateTradeRuleMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo);
	
	default PreTradeEvent runPreTradeEvent(Player player, int tradeIndex) { return this.runPreTradeEvent(PlayerReference.of(player), tradeIndex); }
	default PreTradeEvent runPreTradeEvent(PlayerReference player, int tradeIndex)
	{
		FluidTradeData trade = this.getTrade(tradeIndex);
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
		FluidTradeData trade = this.getTrade(tradeIndex);
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
		FluidTradeData trade = this.getTrade(tradeIndex);
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
	
	public default TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		
		FluidTradeData trade = this.getTrade(tradeIndex);
		if(trade == null || !trade.isValid())
			return TradeResult.FAIL_INVALID_TRADE;
		
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;
		
		//Get the cost of the trade
		CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();
		
		//Abort if not enough stock
		if(!trade.hasStock(this, context.getPlayerReference()) && !this.getCoreSettings().isCreative())
			return TradeResult.FAIL_OUT_OF_STOCK;
		
		if(trade.isSale())
		{
			
			FluidEntry tankEntry = this.getStorage().getTank(trade.getProduct());
			
			//Abort if the purchased fluid cannot be given
			if(!context.canFitFluid(trade.productOfQuantity()) && !(this.drainCapable() && this.getFluidSettings().hasOutputSide() && tankEntry != null && tankEntry.drainable))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			
			//Process the trades payment
			if(!context.getPayment(price))
				return TradeResult.FAIL_CANNOT_AFFORD;
			
			//We have enough money, and the trade is valid. Execute the trade
			//Give the product
			boolean drainTank = true;
			if(context.canFitFluid(trade.productOfQuantity()))
				context.fillFluid(trade.productOfQuantity());
			else //If nowhere to put the product, add to the pending drain.
			{
				drainTank = false;
				tankEntry.addPendingDrain(trade.getQuantity());
				this.markStorageDirty();
			}
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.getCoreSettings().isCreative());
			this.markLoggerDirty();
			
			//Post the notification
			this.getCoreSettings().pushNotification(() -> new FluidTradeNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//Ignore internal editing if this is creative
			if(!this.getCoreSettings().isCreative())
			{
				//Remove the purchased fluid from storage
				if(drainTank)
				{
					this.getStorage().drain(trade.productOfQuantity());
					this.markStorageDirty();
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
			
			//Abort if not enough fluid to buy
			if(!context.hasFluid(trade.productOfQuantity()))
				return TradeResult.FAIL_CANNOT_AFFORD;
			
			//Abort if not enough space to put the purchased fluid
			if(!trade.hasSpace(this) && !this.getCoreSettings().isCreative())
				return TradeResult.FAIL_NO_INPUT_SPACE;
			
			//Give the payment to the player
			if(!context.givePayment(price))
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			
			//We have enough money, and the trade is valid. Execute the trade
			//Collect the product
			if(!context.drainFluid(trade.productOfQuantity()))
			{
				//Failed somehow. Take the money back
				context.getPayment(price);
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.isCreative());
			this.markLoggerDirty();
			
			//Post the notification
			this.getCoreSettings().pushNotification(() -> new FluidTradeNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//Ignore internal editing if this is creative
			if(!this.getCoreSettings().isCreative())
			{
				//Put the purchased fluid in storage
				this.getStorage().forceFillTank(trade.productOfQuantity());
				this.markStorageDirty();
				//Remove the coins from storage
				this.removeStoredMoney(price);
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), tradeIndex, price);
			
			return TradeResult.SUCCESS;
			
		}
		
		return TradeResult.FAIL_INVALID_TRADE;
	}
	
	@Override
	public default void addInteractionSlots(List<InteractionSlotData> interactionSlots) {
		interactionSlots.add(FluidInteractionSlot.INSTANCE);
	}
	
	@Override
	public default void initStorageTabs(TraderStorageMenu menu) {
		//Storage tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new FluidStorageTab(menu));
		//Fluid Trade interaction tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new FluidTradeEditTab(menu));
	}
	
}
