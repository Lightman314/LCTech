package io.github.lightman314.lctech.common.universaldata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.EnergyTraderBlockEntity;
import io.github.lightman314.lctech.client.gui.screen.TradeEnergyPriceScreen.TradePriceData;
import io.github.lightman314.lctech.common.logger.EnergyShopLogger;
import io.github.lightman314.lctech.items.UpgradeItem;
import io.github.lightman314.lctech.menu.EnergyTraderMenu;
import io.github.lightman314.lctech.menu.EnergyTraderStorageMenu;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.universal_energy_trader.MessageSetEnergyPrice2;
import io.github.lightman314.lctech.network.messages.universal_energy_trader.MessageSetEnergyTradeRules2;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.energy.TradeEnergyHandler;
import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearUniversalLogger;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageAddOrRemoveTrade2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;

public class UniversalEnergyTraderData extends UniversalTraderData implements IEnergyTrader{

	public static final int TRADE_LIMIT = EnergyTraderBlockEntity.TRADE_LIMIT;
	
	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID,"energy_trader");
	
	TradeEnergyHandler energyHandler = new TradeEnergyHandler(this);
	
	public IEnergyStorage getEnergyHandler(Direction relativeSide)
	{
		return this.energyHandler.getExternalHandler(relativeSide);
	}
	
	EnergyShopLogger logger = new EnergyShopLogger();
	
	EnergyTraderSettings energySettings = new EnergyTraderSettings(this, this::markEnergySettingsDirty, this::sendSettingsUpdateToServer);
	
	int tradeCount = 1;
	List<EnergyTradeData> trades = EnergyTradeData.listOfSize(this.tradeCount);
	
	//Energy Storage
	int energyStorage = 0;
	
	Container upgradeInventory = new SimpleContainer(5);
	public Container getUpgradeInventory() { return this.upgradeInventory; }
	
	List<TradeRule> tradeRules = Lists.newArrayList();
	
	public UniversalEnergyTraderData() {}
	
	public UniversalEnergyTraderData(PlayerReference owner, BlockPos pos, ResourceKey<Level> level, UUID traderID)
	{
		super(owner, pos, level, traderID);
	}
	
	@Override
	public void read(CompoundTag compound)
	{
		
		if(compound.contains("TradeCount", Tag.TAG_INT))
			this.tradeCount = compound.getInt("TradeCount");
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.trades = EnergyTradeData.LoadNBTList(this.tradeCount, compound);
		
		if(compound.contains("UpgradeInventory", Tag.TAG_LIST))
			this.upgradeInventory = InventoryUtil.loadAllItems("UpgradeInventory", compound, 5);
		
		if(compound.contains("EnergySettings", Tag.TAG_COMPOUND))
			this.energySettings.load(compound.getCompound("EnergySettings"));
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Tag.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		this.logger.read(compound);
		
		if(compound.contains("Battery", Tag.TAG_INT))
			this.energyStorage = compound.getInt("Battery");
		
		super.read(compound);
	}
	
	@Override
	public CompoundTag write(CompoundTag compound)
	{
		
		writeTrades(compound);
		writeEnergySettings(compound);
		writeUpgradeInventory(compound);
		writeRules(compound);
		writeLogger(compound);
		writeEnergyStorage(compound);
		
		return super.write(compound);
	}
	
	protected final CompoundTag writeTrades(CompoundTag compound)
	{
		compound.putInt("TradeCount", this.tradeCount);
		EnergyTradeData.WriteNBTList(this.trades, compound);
		return compound;
	}
	
	protected final  CompoundTag writeEnergySettings(CompoundTag compound)
	{
		compound.put("EnergySettings", this.energySettings.save(new CompoundTag()));
		return compound;
	}
	
	protected final  CompoundTag writeUpgradeInventory(CompoundTag compound)
	{
		InventoryUtil.saveAllItems("UpgradeInventory", compound, this.upgradeInventory);
		return compound;
	}
	
	protected final CompoundTag writeRules(CompoundTag compound)
	{
		TradeRule.writeRules(compound,  this.tradeRules);
		return compound;
	}
	
	protected final  CompoundTag writeLogger(CompoundTag compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	protected final  CompoundTag writeEnergyStorage(CompoundTag compound)
	{
		compound.putInt("Battery", this.energyStorage);
		return compound;
	}
	
	public int getTradeCount() { return this.tradeCount; }
	
	public int getTradeCountLimit() { return TRADE_LIMIT; }
	
	public EnergyTradeData getTrade(int tradeIndex) {
		if(tradeIndex >= 0 && tradeIndex < this.trades.size())
			return this.trades.get(tradeIndex);
		return new EnergyTradeData();
	}
	
	public List<EnergyTradeData> getAllTrades() { return this.trades; }
	
	public TradeEnergyHandler getEnergyHandler() { return this.energyHandler; }
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade2(this.getTraderID(), isAdd));
	}
	
	public void addTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(this.tradeCount >= TRADE_LIMIT)
			return;
		
		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Settings.PermissionWarning(requestor, "add trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.tradeCount + 1);
	}
	
	public void removeTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(this.tradeCount <= 1)
			return;
		
		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Settings.PermissionWarning(requestor, "remove trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.tradeCount - 1);
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, TRADE_LIMIT);
		List<EnergyTradeData> oldTrades = this.trades;
		this.trades = EnergyTradeData.listOfSize(this.tradeCount);
		//Write the old trade data into the array
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); ++i)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		//Send an update to the client
		if(this.isServer())
		{
			this.markDirty(this::writeTrades);
		}
	}
	
	protected void forceReopen(List<Player> users) { }
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(); }
	
	@Override
	public MenuProvider getTradeMenuProvider() {
		return new TraderProvider(this);
	}
	
	@Override
	public MenuProvider getStorageMenuProvider() {
		return new StorageProvider(this);
	}
	
	private class TraderProvider implements MenuProvider
	{
		final UniversalEnergyTraderData trader;
		
		private TraderProvider(UniversalEnergyTraderData trader) { this.trader = trader; }

		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			return new EnergyTraderMenu.EnergyTraderMenuUniversal(windowId, inventory, this.trader.getTraderID());
		}

		@Override
		public Component getDisplayName() {
			return this.trader.getName();
		}
		
	}
	
	private class StorageProvider implements MenuProvider
	{
		final UniversalEnergyTraderData trader;
		
		private StorageProvider(UniversalEnergyTraderData trader) { this.trader = trader; }

		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			return new EnergyTraderStorageMenu.EnergyTraderStorageMenuUniversal(windowId, inventory, this.trader.getTraderID());
		}

		@Override
		public Component getDisplayName() {
			return this.trader.getName();
		}
	}
	
	@Override
	public int GetCurrentVersion()
	{
		return 0;
	}
	
	@Override
	protected void onVersionUpdate(int oldVersion)
	{
		
	}
	
	@Override
	public int getTradeStock(int tradeIndex) {
		return this.getTrade(tradeIndex).getStock(this);
	}
	
	@Override
	public void markTradesDirty() { this.markDirty(this::writeTrades); }
	
	public EnergyTraderSettings getEnergySettings() { return this.energySettings; }
	
	public void markEnergySettingsDirty() { this.markDirty(this::writeEnergySettings); }
	
	@Override
	public void reapplyUpgrades() { this.markUpgradesDirty(); }
	
	public void markUpgradesDirty() { this.markDirty(this::writeUpgradeInventory); }
	
	@Override
	public void addRule(TradeRule rule)
	{
		this.tradeRules.add(rule);
		this.markRulesDirty();
	}
	
	@Override
	public void afterTrade(PostTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.afterTrade(event));
	}
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.beforeTrade(event));
	}
	
	@Override
	public void clearRules() {
		this.tradeRules.clear();
		this.markRulesDirty();
	}
	
	@Override
	public List<TradeRule> getRules() { return this.tradeRules; }
	
	@Override
	public void markRulesDirty() { this.markDirty(this::writeRules); }
	
	@Override
	public void removeRule(TradeRule rule)
	{
		if(this.tradeRules.contains(rule))
		{
			this.tradeRules.remove(rule);
			this.markRulesDirty();
		}
	}
	
	@Override
	public void setRules(List<TradeRule> rules)
	{
		this.tradeRules = rules;
		this.markRulesDirty();
	}
	
	@Override
	public void tradeCost(TradeCostEvent event) {
		this.tradeRules.forEach(rule -> rule.tradeCost(event));
	}
	
	@Override
	public void clearLogger() {
		this.logger.clear();
		this.markLoggerDirty();
	}
	
	@Override
	public EnergyShopLogger getLogger() { return this.logger; }
	
	@Override
	public void markLoggerDirty() { this.markDirty(this::writeLogger); }
	
	@Override
	public int getPendingDrain() { return 0; }
	
	@Override
	public void addPendingDrain(int amount) { }
	
	@Override
	public void shrinkPendingDrain(int amount) { }
	
	@Override
	public int getAvailableEnergy() { return this.getTotalEnergy(); }
	
	@Override
	public int getTotalEnergy() { return this.energyStorage; }
	
	@Override
	public int getMaxEnergy() {
		//Calculate based on the current upgrades
		int defaultCapacity = IEnergyTrader.getDefaultMaxEnergy();
		int maxEnergy = defaultCapacity;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < this.upgradeInventory.getContainerSize(); ++i)
		{
			ItemStack stack = this.upgradeInventory.getItem(i);
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
						maxEnergy += addAmount;
					}
				}
			}
		}
		return maxEnergy;
	}
	
	@Override
	public void shrinkEnergy(int amount) { this.energyStorage -= amount; }
	
	@Override
	public void addEnergy(int amount) { this.energyStorage += amount; }
	
	@Override
	public void markEnergyStorageDirty() { this.markDirty(this::writeEnergyStorage); }
	
	@Override
	public boolean canFillExternally() { return false; }
	
	@Override
	public boolean canDrainExternally() { return false; }
	
	public void sendOpenTraderMessage()
	{
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.getTraderID()));
	}
	
	public void sendOpenStorageMessage()
	{
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.getTraderID()));
	}
	
	@Override
	public void sendClearLogMessage()
	{
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearUniversalLogger(this.getTraderID()));
	}
	
	@Override
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new TradeRuleScreenHandler(this); }
	
	private static class TradeRuleScreenHandler implements ITradeRuleScreenHandler
	{
		private UniversalEnergyTraderData trader;
		
		public TradeRuleScreenHandler(UniversalEnergyTraderData trader) { this.trader = trader; }

		@Override
		public void reopenLastScreen() {
			this.trader.sendOpenStorageMessage();
		}

		@Override
		public ITradeRuleHandler ruleHandler() {
			return this.trader;
		}

		@Override
		public void updateServer(List<TradeRule> newRules) {
			this.trader.sendUpdateTradeRuleMessage(newRules);
		}
		
	}
	
	@Override
	public void sendPriceMessage(TradePriceData priceData) {
		if(this.isClient())
			LCTechPacketHandler.instance.sendToServer(new MessageSetEnergyPrice2(this.getTraderID(), priceData.tradeIndex, priceData.cost, priceData.type, priceData.amount));
	}
	
	@Override
	public void sendUpdateTradeRuleMessage(List<TradeRule> newRules) {
		if(this.isClient())
			LCTechPacketHandler.instance.sendToServer(new MessageSetEnergyTradeRules2(this.getTraderID(), newRules));
	}

	@Override
	public IconData getIcon() { return IconData.of(new ResourceLocation(LCTech.MODID, "textures/gui/universal_trader_icons.png"), 16, 0); }

	@Override
	public ResourceLocation getTraderType() {
		return TYPE;
	}
	
	@Override
	public Component getDefaultName() {
		return new TranslatableComponent("gui.lctech.universaltrader.energy");
	}

	@Override
	public CompoundTag getPersistentData() {
		CompoundTag compound = new CompoundTag();
		ITradeRuleHandler.savePersistentRuleData(compound, this, this.trades);
		this.logger.write(compound);
		return compound;
	}

	@Override
	public void loadPersistentData(CompoundTag compound) {
		ITradeRuleHandler.readPersistentRuleData(compound, this, this.trades);
		this.logger.read(compound);
	}
	
	@Override
	public void loadFromJson(JsonObject json) throws Exception{
		super.loadFromJson(json);
		
		if(!json.has("Trades"))
			throw new Exception("Energy Trader must have a trade list.");
		
		JsonArray tradeList = json.get("Trades").getAsJsonArray();
		this.trades = new ArrayList<>();
		for(int i = 0; i < tradeList.size() && this.trades.size() < TRADE_LIMIT; ++i)
		{
			try {
				
				JsonObject tradeData = tradeList.get(i).getAsJsonObject();
				EnergyTradeData newTrade = new EnergyTradeData();
				//Trade Type
				if(tradeData.has("TradeType"))
					newTrade.setTradeDirection(EnergyTradeData.loadTradeType(tradeData.get("TradeType").getAsString()));
				//Quantity
				newTrade.setAmount(tradeData.get("Quantity").getAsInt());
				//Price
				newTrade.setCost(CoinValue.Parse(tradeData.get("Price")));
				//Trade Rules
				if(tradeData.has("TradeRules"))
				{
					newTrade.setRules(TradeRule.Parse(tradeData.get("TradeRules").getAsJsonArray()));
				}
				
				this.trades.add(newTrade);
				
			} catch(Exception e) { LCTech.LOGGER.error("Error parsing energy trade at index " + i, e); }
		}
		
		if(this.trades.size() <= 0)
			throw new Exception("Trader has no valid trades!");
		
		this.tradeCount = this.trades.size();
		
		this.energyStorage = this.getMaxEnergy();
		
		if(json.has("TradeRules"))
		{
			this.tradeRules = TradeRule.Parse(json.get("TradeRules").getAsJsonArray());
		}
		
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		super.saveToJson(json);
		
		JsonArray trades = new JsonArray();
		for(EnergyTradeData trade : this.trades)
		{
			if(trade.isValid())
			{
				JsonObject tradeData = new JsonObject();
				tradeData.addProperty("TradeType", trade.getTradeDirection().name());
				tradeData.add("Price", trade.getCost().toJson());
				tradeData.addProperty("Quantity", trade.getAmount());
				
				if(trade.getRules().size() > 0)
					tradeData.add("TradeRules", TradeRule.saveRulesToJson(trade.getRules()));
				
				trades.add(tradeData);
			}
		}
		json.add("Trades", trades);
		
		if(this.tradeRules.size() > 0)
			json.add("TradeRules", TradeRule.saveRulesToJson(this.tradeRules));
		
		return json;
	}
	
	
	
	
}
