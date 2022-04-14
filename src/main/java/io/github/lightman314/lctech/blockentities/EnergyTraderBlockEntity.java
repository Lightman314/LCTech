package io.github.lightman314.lctech.blockentities;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.logger.EnergyShopLogger;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.energy.TradeEnergyHandler;
import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.DirectionalUtil;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageUpdateTradeRule;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyTraderBlockEntity extends TraderBlockEntity implements IEnergyTrader, ILoggerSupport<EnergyShopLogger>{

	public static final int TRADE_LIMIT = 4;
	
	int tradeCount = 1;
	
	TradeEnergyHandler energyHandler = new TradeEnergyHandler(this);
	
	EnergyTraderSettings energySettings = new EnergyTraderSettings(this, this::markEnergySettingsDirty, this::sendSettingsUpdateToServer);
	
	EnergyShopLogger logger = new EnergyShopLogger();
	
	List<EnergyTradeData> trades = EnergyTradeData.listOfSize(1);
	
	//Energy Storage
	int energyStorage = 0;
	int pendingDrain = 0;
	
	Container upgradeInventory = new SimpleContainer(5);
	public Container getUpgradeInventory() { return this.upgradeInventory; }
	
	List<TradeRule> tradeRules = Lists.newArrayList();
	
	/**
	 * Default Energy Trader Entity
	 */
	public EnergyTraderBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.ENERGY_TRADER, pos, state);
	}
	
	/**
	 * Default Energy Trader Entity (for children)
	 */
	protected EnergyTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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
	
	public List<EnergyTradeData> getTradeInfo() { return this.trades; }
	
	public TradeEnergyHandler getEnergyHandler() { return this.energyHandler; }
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.worldPosition, isAdd));
	}
	
	public void addTrade(Player requestor)
	{
		if(this.level.isClientSide)
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
		if(this.level.isClientSide)
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
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeTrades(new CompoundTag()));
		}
	}
	
	/*@Override
	public void forceReopen(List<Player> users) { }*/
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(this.energySettings); }
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		writeTrades(compound);
		writeEnergySettings(compound);
		writeUpgradeInventory(compound);
		writeRules(compound);
		writeLogger(compound);
		writeEnergyStorage(compound);
		
		super.saveAdditional(compound);
	}
	
	public CompoundTag writeTrades(CompoundTag compound)
	{
		compound.putInt("TradeCount", this.tradeCount);
		EnergyTradeData.WriteNBTList(this.trades, compound);
		return compound;
	}
	
	public CompoundTag writeEnergySettings(CompoundTag compound)
	{
		compound.put("EnergySettings", this.energySettings.save(new CompoundTag()));
		return compound;
	}
	
	public CompoundTag writeUpgradeInventory(CompoundTag compound)
	{
		InventoryUtil.saveAllItems("UpgradeInventory", compound, this.upgradeInventory);
		return compound;
	}
	
	protected CompoundTag writeRules(CompoundTag compound)
	{
		TradeRule.writeRules(compound,  this.tradeRules);
		return compound;
	}
	
	protected CompoundTag writeLogger(CompoundTag compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	public CompoundTag writeEnergyStorage(CompoundTag compound)
	{
		compound.putInt("Battery", this.energyStorage);
		compound.putInt("PendingDrain", this.pendingDrain);
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
		
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
		
		if(compound.contains("PendingDrain"))
			this.pendingDrain = compound.getInt("PendingDrain");
		
	}
	
	/*@Override
	public MenuProvider getTradeMenuProvider() {
		return new TraderProvider(this);
	}
	
	@Override
	public MenuProvider getStorageMenuProvider() {
		return new StorageProvider(this);
	}
	
	@Override
	public MenuProvider getCashRegisterTradeMenuProvider(CashRegisterBlockEntity registerEntity) {
		return new CashRegisterProvider(this, registerEntity);
	}
	
	//Menu Providers Here
	private class TraderProvider implements MenuProvider
	{
		final EnergyTraderBlockEntity blockEntity;
		
		private TraderProvider(EnergyTraderBlockEntity blockEntity)
		{
			this.blockEntity = blockEntity;
		}

		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			return new EnergyTraderMenu(windowId, inventory, this.blockEntity.worldPosition);
		}

		@Override
		public Component getDisplayName() {
			return this.blockEntity.getName();
		}
	}
	
	private class StorageProvider implements MenuProvider
	{
		final EnergyTraderBlockEntity blockEntity;
		
		private StorageProvider(EnergyTraderBlockEntity blockEntity)
		{
			this.blockEntity = blockEntity;
		}

		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			return new EnergyTraderStorageMenu(windowId, inventory, this.blockEntity.worldPosition);
		}

		@Override
		public Component getDisplayName() {
			return this.blockEntity.getName();
		}
	}
	
	private class CashRegisterProvider implements MenuProvider
	{
		final EnergyTraderBlockEntity blockEntity;
		final CashRegisterBlockEntity cashRegister;
		
		private CashRegisterProvider(EnergyTraderBlockEntity blockEntity, CashRegisterBlockEntity cashRegister)
		{
			this.blockEntity = blockEntity;
			this.cashRegister = cashRegister;
		}
		
		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			return new EnergyTraderMenu.EnergyTraderMenuCR(windowId, inventory, this.blockEntity.worldPosition, this.cashRegister);
		}
		
		@Override
		public Component getDisplayName() {
			return this.blockEntity.getName();
		}
		
	}*/
	
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
		
		this.setChanged();
		
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeTrades(new CompoundTag()));
		}
	}
	
	public EnergyTraderSettings getEnergySettings() { return this.energySettings; }
	
	public void markEnergySettingsDirty()
	{
		this.setChanged();
		this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeEnergySettings(new CompoundTag()));
		}
	}
	
	@Override
	public void dumpContents(Level level, BlockPos pos)
	{
		//Dump coin contents, etc.
		super.dumpContents(level, pos);
		
		//Dump the upgrade items if present
		for(int i = 0; i < this.upgradeInventory.getContainerSize(); ++i)
		{
			if(!this.upgradeInventory.getItem(i).isEmpty())
				Block.popResource(level,  pos,  this.upgradeInventory.getItem(i));
		}
		
	}
	
	@Override
	public void reapplyUpgrades()
	{
		this.markUpgradesDirty();
	}
	
	public void markUpgradesDirty()
	{
		this.setChanged();
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeUpgradeInventory(new CompoundTag()));
		}
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
		this.setChanged();
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeRules(new CompoundTag()));
		}
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
		this.setChanged();
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, writeLogger(new CompoundTag()));
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
			IEnergyStorage handler = this.energyHandler.getExternalHandler(relativeSide);
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
	public void shrinkEnergy(int amount) {
		this.energyStorage -= amount;
	}

	@Override
	public void addEnergy(int amount) {
		this.energyStorage += amount;
	}

	@Override
	public void markEnergyStorageDirty() {
		this.setChanged();
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeEnergyStorage(new CompoundTag()));
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
		if(this.level.isClientSide)
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.worldPosition));
	}
	
	public void sendOpenStorageMessage()
	{
		if(this.level.isClientSide)
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.worldPosition));
	}

	@Override
	public void sendClearLogMessage() {
		if(this.level.isClientSide)
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearLogger(this.worldPosition));
	}

	@Override
	public ITradeRuleScreenHandler getRuleScreenHandler(int tradeIndex) { return new TradeRuleScreenHandler(this, tradeIndex); }
	
	private static class TradeRuleScreenHandler implements ITradeRuleScreenHandler
	{
		private EnergyTraderBlockEntity blockEntity;
		private final int tradeIndex;
		
		public TradeRuleScreenHandler(EnergyTraderBlockEntity blockEntity, int tradeIndex) { this.blockEntity = blockEntity; this.tradeIndex = tradeIndex; }

		@Override
		public void reopenLastScreen() {
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.blockEntity.worldPosition));
		}

		@Override
		public ITradeRuleHandler ruleHandler() {
			if(this.tradeIndex < 0)
				return this.blockEntity;
			return this.blockEntity.getTrade(this.tradeIndex);
		}

		@Override
		public void updateServer(ResourceLocation type, CompoundTag updateInfo) {
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule(this.blockEntity.worldPosition, this.tradeIndex, type, updateInfo));
		}
		
	}

	/*@Override
	public void sendPriceMessage(TradePriceData priceData) {
		if(this.level.isClientSide)
			LCTechPacketHandler.instance.sendToServer(new MessageSetEnergyPrice(this.worldPosition, priceData.tradeIndex, priceData.cost, priceData.type, priceData.amount));
	}*/

	@Override
	public void sendUpdateTradeRuleMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo) {
		if(this.level.isClientSide)
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule(this.worldPosition, tradeIndex, type, updateInfo));
	}
	
	@Override
	public void serverTick() {
		super.serverTick();
		if(this.canDrainExternally() && this.getDrainableEnergy() > 0)
		{
			for(Direction direction : Direction.values())
			{
				if(this.energySettings.getOutputSides().get(direction) && this.getDrainableEnergy() > 0)
				{
					Direction trueSide = this.getBlockState().getBlock() instanceof IRotatableBlock ? DirectionalUtil.getTrueSide(((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState()), direction) : direction;
					
					BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(trueSide));
					if(be != null)
					{
						be.getCapability(CapabilityEnergy.ENERGY, trueSide.getOpposite()).ifPresent(energyHandler ->{
							int extractedAmount = energyHandler.receiveEnergy(this.getDrainableEnergy(), false);
							if(extractedAmount > 0)
							{
								this.shrinkPendingDrain(extractedAmount);
								this.shrinkEnergy(extractedAmount);
								this.markEnergyStorageDirty();
							}
						});
					}
				}
			}
		}
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
