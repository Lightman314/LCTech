package io.github.lightman314.lctech.common.universaldata;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.logger.FluidShopLogger;
import io.github.lightman314.lctech.container.UniversalFluidEditContainer;
import io.github.lightman314.lctech.container.UniversalFluidTraderContainer;
import io.github.lightman314.lctech.container.UniversalFluidTraderStorageContainer;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.MessageSetFluidTradeRules2;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.fluid.TradeFluidHandler;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageAddOrRemoveTrade2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

public class UniversalFluidTraderData extends UniversalTraderData implements IFluidTrader, ILoggerSupport<FluidShopLogger>{

	public static final int TRADELIMIT = FluidTraderTileEntity.TRADE_LIMIT;
	
	public final static ResourceLocation TYPE = new ResourceLocation(LCTech.MODID,"fluid_trader");
	
	public static final int VERSION = 0;
	
	public final TradeFluidHandler fluidHandler = new TradeFluidHandler(this);
	
	private FluidTraderSettings fluidSettings = new FluidTraderSettings(this, this::markFluidSettingsDirty, this::sendSettingsUpdateToServer);
	
	int tradeCount = 1;
	List<FluidTradeData> trades = null;
	
	IInventory upgradeInventory = new Inventory(5);
	public IInventory getUpgradeInventory() { return this.upgradeInventory; }
	public void reapplyUpgrades() { this.trades.forEach(trade -> trade.applyUpgrades(this, this.upgradeInventory)); }
	
	private final FluidShopLogger logger = new FluidShopLogger();
	
	List<TradeRule> tradeRules = Lists.newArrayList();
	
	public UniversalFluidTraderData() {}
	
	public UniversalFluidTraderData(PlayerReference owner, BlockPos pos, RegistryKey<World> world, UUID traderID, int tradeCount) {
		super(owner, pos, world, traderID);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, TRADELIMIT);
		this.trades = FluidTradeData.listOfSize(tradeCount);
	}
	
	@Override
	public void read(CompoundNBT compound)
	{
		if(compound.contains("TradeLimit", Constants.NBT.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, TRADELIMIT);
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Constants.NBT.TAG_LIST))
			this.trades = FluidTradeData.LoadNBTList(this.tradeCount, compound);
		
		if(compound.contains("UpgradeInventory", Constants.NBT.TAG_LIST))
			this.upgradeInventory = InventoryUtil.loadAllItems("UpgradeInventory", compound, 5);
		
		this.logger.read(compound);
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Constants.NBT.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		if(compound.contains("FluidSettings", Constants.NBT.TAG_COMPOUND))
			this.fluidSettings.load(compound.getCompound("FluidSettings"));
		
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		this.writeTrades(compound);
		this.writeUpgrades(compound);
		this.writeLogger(compound);
		this.writeRules(compound);
		this.writeFluidSettings(compound);
		
		return super.write(compound);
	}
	
	protected final CompoundNBT writeTrades(CompoundNBT compound)
	{
		compound.putInt("TradeLimit", this.trades.size());
		FluidTradeData.WriteNBTList(this.trades, compound);
		return compound;
	}
	
	protected final CompoundNBT writeUpgrades(CompoundNBT compound)
	{
		InventoryUtil.saveAllItems("UpgradeInventory", compound, this.upgradeInventory);
		return compound;
	}
	
	protected final CompoundNBT writeLogger(CompoundNBT compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	protected final CompoundNBT writeRules(CompoundNBT compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	protected final CompoundNBT writeFluidSettings(CompoundNBT compound)
	{
		compound.put("FluidSettings", this.fluidSettings.save(new CompoundNBT()));
		return compound;
	}
	
	@Override
	public int getTradeCount() {
		return this.tradeCount;
	}
	
	public int getTradeCountLimit() { return TRADELIMIT; }

	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade2(this.getTraderID(), isAdd));
	}
	
	public void addTrade(PlayerEntity requestor) {
		if(this.tradeCount >= TRADELIMIT)
			return;
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "add trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.tradeCount + 1);
		forceReOpen();
	}
	
	public void removeTrade(PlayerEntity requestor) {
		if(this.tradeCount <= 1)
			return;
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "remove trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.tradeCount - 1);
		forceReOpen();
	}
	
	private void forceReOpen() {
		UniversalContainer.onForceReopen(this.getTraderID());
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, TRADELIMIT);
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
	public ITextComponent getDefaultName() {
		return new TranslationTextComponent("gui.lctech.universaltrader.fluid");
	}
	
	@Override
	protected INamedContainerProvider getTradeMenuProvider() {
		return new TraderProvider(this.getTraderID());
	}
	
	@Override
	protected INamedContainerProvider getStorageMenuProvider() {
		return new StorageProvider(this.getTraderID());
	}
	
	protected INamedContainerProvider getFluidEditMenuProvider(int tradeIndex) { return new FluidEditProvider(this.getTraderID(), tradeIndex); }
	
	@Override
	public void openFluidEditMenu(PlayerEntity player, int tradeIndex) {
		INamedContainerProvider provider = getFluidEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LCTech.LOGGER.error("No fluid edit container provider was given for the trader of type " + this.getTraderType().toString());
			return;
		}
		if(!(player instanceof ServerPlayerEntity))
		{
			LCTech.LOGGER.error("Player is not a server player entity. Cannot open the storage menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayerEntity)player, provider, new TradeIndexDataWriter(this.getTraderID(), tradeIndex));
	}
	
	private static class TraderProvider implements INamedContainerProvider {
		final UUID traderID;
		private TraderProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public Container createMenu(int menuID, PlayerInventory inventory, PlayerEntity player) {
			return new UniversalFluidTraderContainer(menuID, inventory, this.traderID);
		}
		@Override
		public ITextComponent getDisplayName() { return new StringTextComponent(""); }
	}
	
	private static class StorageProvider implements INamedContainerProvider {
		final UUID traderID;
		private StorageProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public Container createMenu(int menuID, PlayerInventory inventory, PlayerEntity player) {
			return new UniversalFluidTraderStorageContainer(menuID, inventory, this.traderID);
		}
		@Override
		public ITextComponent getDisplayName() { return new StringTextComponent(""); }
	}
	
	private static class FluidEditProvider implements INamedContainerProvider {
		final UUID traderID;
		final int tradeIndex;
		private FluidEditProvider(UUID traderID, int tradeIndex) { this.traderID = traderID; this.tradeIndex = tradeIndex; }
		
		private UniversalFluidTraderData getData() {
			UniversalTraderData data = TradingOffice.getData(this.traderID);
			if(data instanceof UniversalFluidTraderData)
				return (UniversalFluidTraderData)data;
			return null;
		}
		@Override
		public Container createMenu(int menuID, PlayerInventory inventory, PlayerEntity player) {
			return new UniversalFluidEditContainer(menuID, inventory, () -> getData(), this.tradeIndex);
		}
		@Override
		public ITextComponent getDisplayName() { return new StringTextComponent(""); }
	}
	
	
	
	@Override
	public ResourceLocation IconLocation() { return new ResourceLocation(LCTech.MODID, "textures/gui/universal_trader_icons.png"); }

	@Override
	public int IconPositionX() { return 0; }

	@Override
	public int IconPositionY() { return 0; }
	
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
	public void setRules(List<TradeRule> newRules) { this.tradeRules = newRules; }
	
	@Override
	public void addRule(TradeRule newRule) {
		if(newRule == null)
			return;
		//Confirm a lack of duplicate rules
		for(int i = 0; i < this.tradeRules.size(); i++)
		{
			if(newRule.type == this.tradeRules.get(i).type)
				return;
		}
		this.tradeRules.add(newRule);
	}

	@Override
	public void removeRule(TradeRule rule) { if(this.tradeRules.contains(rule)) this.tradeRules.remove(rule); }

	@Override
	public void clearRules() { this.tradeRules.clear(); }

	@Override
	public void markRulesDirty() { this.markDirty(this::writeRules); }
	
	public ITradeRuleScreenHandler GetRuleScreenHandler() { return new TradeRuleScreenHandler(this.getTraderID()); }
	
	private static class TradeRuleScreenHandler implements ITradeRuleScreenHandler{
		private final UUID traderID;
		public TradeRuleScreenHandler(UUID traderID) { this.traderID = traderID; }
		
		private UniversalFluidTraderData getData() {
			UniversalTraderData data = ClientTradingOffice.getData(this.traderID);
			if(data instanceof UniversalFluidTraderData)
				return (UniversalFluidTraderData)data;
			return null;
		}
		
		@Override
		public ITradeRuleHandler ruleHandler() { return this.getData(); }
		
		@Override
		public void reopenLastScreen() { LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.traderID)); }
		
		@Override
		public void updateServer(List<TradeRule> newRules) {
			LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeRules2(this.traderID, newRules));
		}
		
	}

}
