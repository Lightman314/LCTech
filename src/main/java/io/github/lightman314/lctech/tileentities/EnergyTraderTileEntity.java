package io.github.lightman314.lctech.tileentities;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.gui.screen.TradeEnergyPriceScreen.TradePriceData;
import io.github.lightman314.lctech.common.logger.EnergyShopLogger;
import io.github.lightman314.lctech.container.EnergyTraderContainer;
import io.github.lightman314.lctech.container.EnergyTraderStorageContainer;
import io.github.lightman314.lctech.core.ModTileEntities;
import io.github.lightman314.lctech.items.UpgradeItem;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.energy_trader.MessageSetEnergyPrice;
import io.github.lightman314.lctech.network.messages.energy_trader.MessageSetEnergyTradeRules;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.energy.TradeEnergyHandler;
import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings;
import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings.EnergyHandlerSettings;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.tileentity.CashRegisterTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyTraderTileEntity extends TraderTileEntity implements IEnergyTrader {

	public static final int TRADE_LIMIT = 4;
	
	int tradeCount = 1;
	
	TradeEnergyHandler energyHandler = new TradeEnergyHandler(this);
	
	EnergyTraderSettings energySettings = new EnergyTraderSettings(this, this::markEnergySettingsDirty, this::sendSettingsUpdateToServer);
	
	EnergyShopLogger logger = new EnergyShopLogger();
	
	List<EnergyTradeData> trades = EnergyTradeData.listOfSize(1);
	
	//Energy Storage
	int energyStorage = 0;
	int pendingDrain = 0;
	
	IInventory upgradeInventory = new Inventory(5);
	public IInventory getUpgradeInventory() { return this.upgradeInventory; }
	
	List<TradeRule> tradeRules = Lists.newArrayList();
	
	/**
	 * Default Energy Trader Entity
	 */
	public EnergyTraderTileEntity() {
		this(ModTileEntities.ENERGY_TRADER);
	}
	
	/**
	 * Default Energy Trader Entity (for children)
	 */
	protected EnergyTraderTileEntity(TileEntityType<?> type) {
		super(type);
	}

	public int getTradeCount() {
		return MathUtil.clamp(this.tradeCount, 1, TRADE_LIMIT);
	}
	
	public int getTradeCountLimit() {
		return TRADE_LIMIT;
	}
	
	public EnergyTradeData getTrade(int tradeIndex)
	{
		if(tradeIndex >= 0 && tradeIndex < this.trades.size())
			return this.trades.get(tradeIndex);
		return new EnergyTradeData();
	}
	
	public List<EnergyTradeData> getAllTrades() { return this.trades; }
	
	public TradeEnergyHandler getEnergyHandler() { return this.energyHandler; }
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.pos, isAdd));
	}
	
	public void addTrade(PlayerEntity requestor)
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
	
	public void removeTrade(PlayerEntity requestor)
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
			CompoundNBT compound = this.writeTrades(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(this.energySettings); }
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		writeTrades(compound);
		writeEnergySettings(compound);
		writeUpgradeInventory(compound);
		writeRules(compound);
		writeLogger(compound);
		writeEnergyStorage(compound);
		
		return super.write(compound);
	}
	
	public CompoundNBT writeTrades(CompoundNBT compound)
	{
		compound.putInt("TradeCount", this.tradeCount);
		EnergyTradeData.WriteNBTList(this.trades, compound);
		return compound;
	}
	
	public CompoundNBT writeEnergySettings(CompoundNBT compound)
	{
		compound.put("EnergySettings", this.energySettings.save(new CompoundNBT()));
		return compound;
	}
	
	public CompoundNBT writeUpgradeInventory(CompoundNBT compound)
	{
		InventoryUtil.saveAllItems("UpgradeInventory", compound, this.upgradeInventory);
		return compound;
	}
	
	protected CompoundNBT writeRules(CompoundNBT compound)
	{
		TradeRule.writeRules(compound,  this.tradeRules);
		return compound;
	}
	
	protected CompoundNBT writeLogger(CompoundNBT compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	public CompoundNBT writeEnergyStorage(CompoundNBT compound)
	{
		compound.putInt("Battery", this.energyStorage);
		compound.putInt("PendingDrain", this.pendingDrain);
		return compound;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		super.read(state, compound);
		
		if(compound.contains("TradeCount", Constants.NBT.TAG_INT))
			this.tradeCount = compound.getInt("TradeCount");
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Constants.NBT.TAG_LIST))
			this.trades = EnergyTradeData.LoadNBTList(this.tradeCount, compound);
		
		if(compound.contains("UpgradeInventory", Constants.NBT.TAG_LIST))
			this.upgradeInventory = InventoryUtil.loadAllItems("UpgradeInventory", compound, 5);
		
		if(compound.contains("EnergySettings", Constants.NBT.TAG_COMPOUND))
			this.energySettings.load(compound.getCompound("EnergySettings"));
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Constants.NBT.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		this.logger.read(compound);
		
		if(compound.contains("Battery", Constants.NBT.TAG_INT))
			this.energyStorage = compound.getInt("Battery");
		
		if(compound.contains("PendingDrain"))
			this.pendingDrain = compound.getInt("PendingDrain");
		
	}
	
	@Override
	public INamedContainerProvider getTradeMenuProvider() {
		return new TraderProvider(this);
	}
	
	@Override
	public INamedContainerProvider getStorageMenuProvider() {
		return new StorageProvider(this);
	}
	
	@Override
	public INamedContainerProvider getCashRegisterTradeMenuProvider(CashRegisterTileEntity registerEntity) {
		return new CashRegisterProvider(this, registerEntity);
	}
	
	//Menu Providers Here
	private class TraderProvider implements INamedContainerProvider
	{
		final EnergyTraderTileEntity blockEntity;
		
		private TraderProvider(EnergyTraderTileEntity blockEntity)
		{
			this.blockEntity = blockEntity;
		}

		@Override
		public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
			return new EnergyTraderContainer(windowId, inventory, this.blockEntity.pos);
		}

		@Override
		public ITextComponent getDisplayName() {
			return this.blockEntity.getName();
		}
	}
	
	private class StorageProvider implements INamedContainerProvider
	{
		final EnergyTraderTileEntity blockEntity;
		
		private StorageProvider(EnergyTraderTileEntity blockEntity)
		{
			this.blockEntity = blockEntity;
		}

		@Override
		public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
			return new EnergyTraderStorageContainer(windowId, inventory, this.blockEntity.pos);
		}

		@Override
		public ITextComponent getDisplayName() {
			return this.blockEntity.getName();
		}
	}
	
	private class CashRegisterProvider implements INamedContainerProvider
	{
		final EnergyTraderTileEntity blockEntity;
		final CashRegisterTileEntity cashRegister;
		
		private CashRegisterProvider(EnergyTraderTileEntity blockEntity, CashRegisterTileEntity cashRegister)
		{
			this.blockEntity = blockEntity;
			this.cashRegister = cashRegister;
		}
		
		@Override
		public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
			return new EnergyTraderContainer.EnergyTraderContainerCR(windowId, inventory, this.blockEntity.pos, this.cashRegister);
		}
		
		@Override
		public ITextComponent getDisplayName() {
			return this.blockEntity.getName();
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
	public void markTradesDirty() {
		
		this.markDirty();
		
		if(this.isServer())
		{
			CompoundNBT compound = this.writeTrades(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
	public EnergyTraderSettings getEnergySettings() { return this.energySettings; }
	
	public void markEnergySettingsDirty()
	{
		this.markDirty();
		if(this.isServer())
		{
			CompoundNBT compound = this.writeEnergySettings(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
	@Override
	public void dumpContents(World world, BlockPos pos)
	{
		//Dump coin contents, etc.
		super.dumpContents(world, pos);
		
		//Dump the upgrade items if present
		for(int i = 0; i < this.upgradeInventory.getSizeInventory(); ++i)
		{
			if(!this.upgradeInventory.getStackInSlot(i).isEmpty())
				Block.spawnAsEntity(world,  pos,  this.upgradeInventory.getStackInSlot(i));
		}
		
	}
	
	@Override
	public void reapplyUpgrades()
	{
		this.markUpgradesDirty();
	}
	
	public void markUpgradesDirty()
	{
		this.markDirty();
		if(this.isServer())
		{
			CompoundNBT compound = this.writeUpgradeInventory(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
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
	public List<TradeRule> getRules() {
		return this.tradeRules;
	}
	
	@Override
	public void markRulesDirty() {
		this.markDirty();
		if(this.isServer())
		{
			CompoundNBT compound = this.writeRules(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
	@Override
	public void removeRule(TradeRule rule) {
		if(this.tradeRules.contains(rule))
		{
			this.tradeRules.remove(rule);
			this.markRulesDirty();
		}
	}
	
	@Override
	public void setRules(List<TradeRule> rules) {
		this.tradeRules = rules;
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
	public EnergyShopLogger getLogger() {
		return this.logger;
	}
	
	@Override
	public void markLoggerDirty() {
		this.markDirty();
		if(this.isServer())
		{
			CompoundNBT compound = this.writeLogger(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
	public ITradeRuleScreenHandler GetRuleScreenBackHandler() { return new TraderScreenHandler(this); }
	
	private static class TraderScreenHandler implements ITradeRuleScreenHandler
	{
		private EnergyTraderTileEntity blockEntity;
		
		public TraderScreenHandler(EnergyTraderTileEntity blockEntity) { this.blockEntity = blockEntity; }
		
		@Override
		public void reopenLastScreen() {
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.blockEntity.pos));
		}
		
		@Override
		public ITradeRuleHandler ruleHandler() {
			return this.blockEntity;
		}
		
		@Override
		public void updateServer(List<TradeRule> newRules) {
			LCTechPacketHandler.instance.sendToServer(new MessageSetEnergyTradeRules(this.blockEntity.pos, newRules));
		}
		
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap == CapabilityEnergy.ENERGY)
		{
			Direction relativeSide = side;
			if(this.getBlockState().getBlock() instanceof IRotatableBlock)
			{
				Direction facing = ((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState());
				relativeSide = IItemHandlerBlock.getRelativeSide(facing, side);
			}
			EnergyHandlerSettings handlerSetting = this.energySettings.getHandlerSettings(relativeSide);
			IEnergyStorage handler = this.energyHandler.getHandler(handlerSetting);
			if(handler != null)
				return CapabilityEnergy.ENERGY.orEmpty(cap, LazyOptional.of(() -> handler));
			return LazyOptional.empty();
		}
		return super.getCapability(cap, side);
	}
	
	//Energy Data
	
	@Override
	public int getPendingDrain() {
		return this.pendingDrain;
	}

	@Override
	public void addPendingDrain(int amount) {
		this.pendingDrain += amount;
	}

	@Override
	public void shrinkPendingDrain(int amount) {
		this.pendingDrain -= amount;
		if(this.pendingDrain < 0)
			this.pendingDrain = 0;
	}

	@Override
	public int getAvailableEnergy() {
		return this.energyStorage - this.pendingDrain;
	}

	@Override
	public int getTotalEnergy() {
		return this.energyStorage;
	}

	@Override
	public int getMaxEnergy() {
		//Calculate based on the current upgrades
		int maxEnergy = DEFAULT_MAX_ENERGY;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < this.upgradeInventory.getSizeInventory(); ++i)
		{
			ItemStack stack = this.upgradeInventory.getStackInSlot(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
				if(this.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					{
						int addAmount = upgradeItem.getDefaultUpgradeData().getIntValue(CapacityUpgrade.CAPACITY);
						if(addAmount > DEFAULT_MAX_ENERGY && !baseStorageCompensation)
						{
							addAmount -= DEFAULT_MAX_ENERGY;
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
	public void shrinkEnergy(int amount) {
		this.energyStorage -= amount;
	}

	@Override
	public void addEnergy(int amount) {
		this.energyStorage += amount;
	}

	@Override
	public void markEnergyStorageDirty() {
		this.markDirty();
		if(this.isServer())
		{
			CompoundNBT compound = this.writeEnergyStorage(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}

	@Override
	public boolean canFillExternally() {
		//Return true if any input sides are enabled
		for(Direction side : Direction.values())
		{
			if(this.energySettings.getInputSides().get(side))
				return true;
		}
		return false;
	}

	@Override
	public boolean canDrainExternally() {
		//Return true if any output sides are enabled
		for(Direction side : Direction.values())
		{
			if(this.energySettings.getOutputSides().get(side))
				return true;
		}
		return false;
	}
	
	public void sendOpenTraderMessage()
	{
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.pos));
	}
	
	public void sendOpenStorageMessage()
	{
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.pos));
	}

	@Override
	public void sendClearLogMessage() {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearLogger(this.pos));
	}

	@Override
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new TradeRuleScreenHandler(this); }
	
	private static class TradeRuleScreenHandler implements ITradeRuleScreenHandler
	{
		private EnergyTraderTileEntity blockEntity;
		
		public TradeRuleScreenHandler(EnergyTraderTileEntity blockEntity) { this.blockEntity = blockEntity; }

		@Override
		public void reopenLastScreen() {
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.blockEntity.pos));
		}

		@Override
		public ITradeRuleHandler ruleHandler() {
			return this.blockEntity;
		}

		@Override
		public void updateServer(List<TradeRule> newRules) {
			LCTechPacketHandler.instance.sendToServer(new MessageSetEnergyTradeRules(this.blockEntity.pos, newRules));
		}
		
	}

	@Override
	public void sendPriceMessage(TradePriceData priceData) {
		if(this.isClient())
			LCTechPacketHandler.instance.sendToServer(new MessageSetEnergyPrice(this.pos, priceData.tradeIndex, priceData.cost, priceData.type, priceData.amount));
	}

	@Override
	public void sendUpdateTradeRuleMessage(List<TradeRule> newRules) {
		if(this.isClient())
			LCTechPacketHandler.instance.sendToServer(new MessageSetEnergyTradeRules(this.pos, newRules));
	}

	@Override
	public void tick() {
		
	}
	
}
