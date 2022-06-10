package io.github.lightman314.lctech.common.universaldata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blocks.FluidTraderServerBlock;
import io.github.lightman314.lctech.common.logger.FluidShopLogger;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.fluid.TradeFluidHandler;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageAddOrRemoveTrade2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageUpdateTradeRule2;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class UniversalFluidTraderData extends UniversalTraderData implements IFluidTrader, ILoggerSupport<FluidShopLogger>{
	
	public final static ResourceLocation TYPE = new ResourceLocation(LCTech.MODID,"fluid_trader");
	
	public static final int VERSION = 0;
	
	public final TradeFluidHandler fluidHandler = new TradeFluidHandler(this);
	
	FluidTraderSettings fluidSettings = new FluidTraderSettings(this, this::markFluidSettingsDirty, this::sendSettingsUpdateToServer);
	
	TraderFluidStorage storage = new TraderFluidStorage(this);
	public TraderFluidStorage getStorage() { return this.storage; }
	public void markStorageDirty() { this.markDirty(this::writeStorage); }
	
	int tradeCount = 1;
	List<FluidTradeData> trades = null;
	
	Container upgradeInventory = new SimpleContainer(5);
	public Container getUpgradeInventory() { return this.upgradeInventory; }
	public void markUpgradesDirty() { this.markDirty(this::writeUpgrades); }
	
	
	private final FluidShopLogger logger = new FluidShopLogger();
	
	List<TradeRule> tradeRules = Lists.newArrayList();
	
	public UniversalFluidTraderData() {}
	
	public UniversalFluidTraderData(PlayerReference owner, BlockPos pos, ResourceKey<Level> world, UUID traderID, int tradeCount) {
		super(owner, pos, world, traderID);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, ITrader.GLOBAL_TRADE_LIMIT);
		this.trades = FluidTradeData.listOfSize(tradeCount);
	}
	
	@Override
	protected ItemLike getCategoryItem() {
		int tradeCount = this.isCreative() ? ITrader.GLOBAL_TRADE_LIMIT : this.getTradeCount();
		if(tradeCount <= FluidTraderServerBlock.SMALL_SERVER_COUNT)
			return ModBlocks.FLUID_SERVER_SML.get();
		else if(tradeCount <= FluidTraderServerBlock.MEDIUM_SERVER_COUNT)
			return ModBlocks.FLUID_SERVER_MED.get();
		else if(tradeCount <= FluidTraderServerBlock.LARGE_SERVER_COUNT)
			return ModBlocks.FLUID_SERVER_LRG.get();
		return ModBlocks.FLUID_SERVER_XLRG.get();
	}
	
	@Override
	public void read(CompoundTag compound)
	{
		if(compound.contains("TradeLimit", Tag.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, ITrader.GLOBAL_TRADE_LIMIT);
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.trades = FluidTradeData.LoadNBTList(this.tradeCount, compound);
		
		if(compound.contains("FluidStorage"))
			this.storage.load(compound, "FluidStorage");
		else if(compound.contains(ItemTradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.storage.loadFromTrades(compound.getList(ItemTradeData.DEFAULT_KEY, Tag.TAG_LIST));
		
		if(compound.contains("UpgradeInventory", Tag.TAG_LIST))
			this.upgradeInventory = InventoryUtil.loadAllItems("UpgradeInventory", compound, 5);
		
		this.logger.read(compound);
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Tag.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		if(compound.contains("FluidSettings", Tag.TAG_COMPOUND))
			this.fluidSettings.load(compound.getCompound("FluidSettings"));
		
		super.read(compound);
		
		//this.reapplyUpgrades(false);
		
	}
	
	@Override
	public CompoundTag write(CompoundTag compound)
	{
		this.writeTrades(compound);
        this.writeStorage(compound);
		this.writeUpgrades(compound);
		this.writeLogger(compound);
		this.writeRules(compound);
		this.writeFluidSettings(compound);
		
		return super.write(compound);
	}
	
	protected final CompoundTag writeTrades(CompoundTag compound)
	{
		compound.putInt("TradeLimit", this.trades.size());
		FluidTradeData.WriteNBTList(this.trades, compound);
		return compound;
	}
	
	protected final CompoundTag writeStorage(CompoundTag compound)
	{
		this.storage.save(compound, "FluidStorage");
		return compound;
	}
	
	protected final CompoundTag writeUpgrades(CompoundTag compound)
	{
		InventoryUtil.saveAllItems("UpgradeInventory", compound, this.upgradeInventory);
		return compound;
	}
	
	protected final CompoundTag writeLogger(CompoundTag compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	protected final CompoundTag writeRules(CompoundTag compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	protected final CompoundTag writeFluidSettings(CompoundTag compound)
	{
		compound.put("FluidSettings", this.fluidSettings.save(new CompoundTag()));
		return compound;
	}
	
	@Override
	public int getTradeCount() {
		return this.tradeCount;
	}

	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade2(this.getTraderID(), isAdd));
	}
	
	public void addTrade(Player requestor) {
		if(this.tradeCount >= ITrader.GLOBAL_TRADE_LIMIT)
			return;
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "add trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.tradeCount + 1);
		//this.forceReopen();
	}
	
	public void removeTrade(Player requestor) {
		if(this.tradeCount <= 1)
			return;
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "remove trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.tradeCount - 1);
		//this.forceReopen();
	}
	
	/*@Override
	protected void forceReopen(List<Player> users)
	{
		for(Player player : users)
		{
			if(player.containerMenu instanceof FluidTraderMenu)
				this.openTradeMenu(player);
			else if(player.containerMenu instanceof FluidTraderStorageMenu)
				this.openStorageMenu(player);
		}
	}*/
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, ITrader.GLOBAL_TRADE_LIMIT);
		List<FluidTradeData> oldTrades = this.trades;
		this.trades = FluidTradeData.listOfSize(this.tradeCount);
		//Write the old trade data into the array
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); i++)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		this.markTradesDirty();
	}
	
	public FluidTradeData getTrade(int tradeIndex) {
		if(tradeIndex >= 0 && tradeIndex < this.tradeCount)
			return this.trades.get(tradeIndex);
		return new FluidTradeData();
	}
	
	@Override
	public int getTradeStock(int index) {
		return this.getTrade(index).getStock(this);
	}
	
	public List<FluidTradeData> getAllTrades() {
		return this.trades;
	}
	
	public List<FluidTradeData> getTradeInfo() { return this.trades; }
	
	public void markTradesDirty() { this.markDirty(this::writeTrades); }

	public FluidTraderSettings getFluidSettings()
	{
		return this.fluidSettings;
	}
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(); }
	
	public void markFluidSettingsDirty()
	{
		this.markDirty(this::writeFluidSettings);
	}
	
	public TradeFluidHandler getFluidHandler() { return this.fluidHandler; }
	
	public FluidShopLogger getLogger() { return this.logger; }
	
	public void clearLogger() { this.logger.clear(); this.markLoggerDirty(); }
	
	public void markLoggerDirty() { this.markDirty(this::writeLogger); }
	
	@Override
	public ResourceLocation getTraderType() { return TYPE; }
	
	@Override
	public MutableComponent getDefaultName() {
		return Component.translatable("gui.lctech.universaltrader.fluid");
	}
	
	/*@Override
	protected MenuProvider getTradeMenuProvider() {
		return new TraderProvider(this.getTraderID());
	}
	
	@Override
	protected MenuProvider getStorageMenuProvider() {
		return new StorageProvider(this.getTraderID());
	}
	
	protected MenuProvider getFluidEditMenuProvider(int tradeIndex) { return new FluidEditProvider(this.getTraderID(), tradeIndex); }
	
	@Override
	public void openFluidEditMenu(Player player, int tradeIndex) {
		MenuProvider provider = getFluidEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LCTech.LOGGER.error("No fluid edit container provider was given for the trader of type " + this.getTraderType().toString());
			return;
		}
		if(!(player instanceof ServerPlayer))
		{
			LCTech.LOGGER.error("Player is not a server player entity. Cannot open the storage menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayer)player, provider, new TradeIndexDataWriter(this.getTraderID(), tradeIndex));
	}
	
	private static class TraderProvider implements MenuProvider {
		final UUID traderID;
		private TraderProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new FluidTraderMenu.FluidTraderMenuUniversal(menuID, inventory, this.traderID);
		}
		@Override
		public Component getDisplayName() { return new TextComponent(""); }
	}
	
	private static class StorageProvider implements MenuProvider {
		final UUID traderID;
		private StorageProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new FluidTraderStorageMenu.FluidTraderStorageMenuUniversal(menuID, inventory, this.traderID);
		}
		@Override
		public Component getDisplayName() { return new TextComponent(""); }
	}
	
	private static class FluidEditProvider implements MenuProvider {
		final UUID traderID;
		final int tradeIndex;
		private FluidEditProvider(UUID traderID, int tradeIndex) { this.traderID = traderID; this.tradeIndex = tradeIndex; }
		
		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new FluidEditMenu.UniversalFluidEditMenu(menuID, inventory, traderID, this.tradeIndex);
		}
		@Override
		public Component getDisplayName() { return new TextComponent(""); }
	}*/
	
	@Override
	public IconData getIcon() { return IconData.of(new ResourceLocation(LCTech.MODID, "textures/gui/universal_trader_icons.png"), 0, 0); }
	
	@Override
	public int GetCurrentVersion() { return VERSION; }

	@Override
	protected void onVersionUpdate(int oldVersion) {
		//No old versions to update
	}

	@Override
	public boolean drainCapable() {
		return false;
	}
	
	@Override
	public void beforeTrade(PreTradeEvent event) { this.tradeRules.forEach(rule -> rule.beforeTrade(event)); }

	@Override
	public void tradeCost(TradeCostEvent event) { this.tradeRules.forEach(rule -> rule.tradeCost(event)); }
	
	@Override
	public void afterTrade(PostTradeEvent event) { this.tradeRules.forEach(rule -> rule.afterTrade(event)); }
	
	@Override
	public List<TradeRule> getRules() { return this.tradeRules; }
	
	@Override
	public void clearRules() { this.tradeRules.clear(); }

	@Override
	public void markRulesDirty() { this.markDirty(this::writeRules); }
	
	public ITradeRuleScreenHandler getRuleScreenHandler(int tradeIndex) { return new TradeRuleScreenHandler(this, tradeIndex); }
	
	private static class TradeRuleScreenHandler implements ITradeRuleScreenHandler{
		private final UniversalFluidTraderData trader;
		private final int tradeIndex;
		public TradeRuleScreenHandler(UniversalFluidTraderData trader, int tradeIndex) { this.trader = trader; this.tradeIndex = tradeIndex; }
		
		@Override
		public ITradeRuleHandler ruleHandler() {
			if(this.tradeIndex < 0)
				return this.trader;
			return this.trader.getTrade(this.tradeIndex);
		}
		
		@Override
		public void reopenLastScreen() { LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.trader.getTraderID())); }
		
		@Override
		public void updateServer(ResourceLocation type, CompoundTag updateInfo) {
			this.trader.sendUpdateTradeRuleMessage(this.tradeIndex, type, updateInfo);
		}
		
	}
	
	@Override
	public void sendUpdateTradeRuleMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule2(this.getTraderID(), tradeIndex, type, updateInfo));
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
	public void loadFromJson(JsonObject json) throws Exception {
		super.loadFromJson(json);
		
		if(!json.has("Trades"))
			throw new Exception("Fluid Trader must have a trade list.");
		JsonArray tradeList = json.get("Trades").getAsJsonArray();
		
		this.trades = new ArrayList<>();
		for(int i = 0; i < tradeList.size() && this.trades.size() < ITrader.GLOBAL_TRADE_LIMIT; ++i)
		{
			try {
				
				JsonObject tradeData = tradeList.get(i).getAsJsonObject();
				
				FluidTradeData newTrade = new FluidTradeData();
				
				//Product
				JsonObject product = tradeData.get("Product").getAsJsonObject();
				newTrade.setProduct(FluidItemUtil.parseFluidStack(product));
				//Trade Type
				if(tradeData.has("TradeType"))
					newTrade.setTradeDirection(FluidTradeData.loadTradeType(tradeData.get("TradeType").getAsString()));
				//Price
				newTrade.setCost(CoinValue.Parse(tradeData.get("Price")));
				//Tank
				/*if(newTrade.isSale())
				{
					FluidStack fluid = newTrade.getProduct();
					if(!fluid.isEmpty())
						fluid.setAmount(newTrade.getTankCapacity());
					newTrade.setTankContents(fluid);
				}*/
				//Quantity
				if(tradeData.has("Quantity"))
					newTrade.setBucketQuantity(tradeData.get("Quantity").getAsInt());
				//Trade Rules
				if(tradeData.has("TradeRules"))
				{
					newTrade.setRules(TradeRule.Parse(tradeData.get("TradeRules").getAsJsonArray()));
				}
				
				this.trades.add(newTrade);
				
			} catch(Exception e) { LCTech.LOGGER.error("Error parsing fluid trade at index " + i, e); }
		}
		
		if(this.trades.size() <= 0)
			throw new Exception("Trader has no valid trades!");
		
		this.tradeCount = this.trades.size();
		
		if(json.has("TradeRules"))
		{
			this.tradeRules = TradeRule.Parse(json.get("TradeRules").getAsJsonArray());
		}
		
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		super.saveToJson(json);
		
		JsonArray trades = new JsonArray();
		for(FluidTradeData trade : this.trades)
		{
			if(trade.isValid())
			{
				JsonObject tradeData = new JsonObject();
				
				tradeData.addProperty("TradeType", trade.getTradeDirection().name());
				tradeData.add("Price", trade.getCost().toJson());
				tradeData.add("Product", FluidItemUtil.convertFluidStack(trade.getProduct()));
				tradeData.addProperty("Quantity", trade.getBucketQuantity());
				
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
	
	@Override
	public void receiveTradeRuleMessage(Player player, int index, ResourceLocation ruleType, CompoundTag updateInfo) {
		if(!this.hasPermission(player, Permissions.EDIT_TRADE_RULES))
		{
			Settings.PermissionWarning(player, "edit trade rule", Permissions.EDIT_TRADE_RULES);
			return;
		}
		if(index >= 0)
		{
			this.getTrade(index).updateRule(ruleType, updateInfo);
			this.markTradesDirty();
		}
		else
		{
			this.updateRule(ruleType, updateInfo);
			this.markRulesDirty();
		}
	}

}
