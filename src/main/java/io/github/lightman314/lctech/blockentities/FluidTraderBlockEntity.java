package io.github.lightman314.lctech.blockentities;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.logger.FluidShopLogger;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.fluid.TradeFluidHandler;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageUpdateTradeRule;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidTraderBlockEntity extends TraderBlockEntity implements IFluidTrader, ILoggerSupport<FluidShopLogger>{
	
	int tradeCount = 1;
	
	final TradeFluidHandler fluidHandler = new TradeFluidHandler(this);
	
	FluidTraderSettings fluidSettings = new FluidTraderSettings(this, this::markFluidSettingsDirty, this::sendSettingsUpdateToServer);
	
	FluidShopLogger logger = new FluidShopLogger();
	
	List<FluidTradeData> trades;
	
	TraderFluidStorage storage = new TraderFluidStorage(this);
	
	Container upgradeInventory = new SimpleContainer(5);
	public Container getUpgradeInventory() { return this.upgradeInventory; }
	
	public void markUpgradesDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.writeUpgradeInventory(new CompoundTag()));
	}
	
	List<TradeRule> tradeRules = Lists.newArrayList();
	
	/**
	 * Default Fluid Trader Entity:
	 * Trade Count: 1
	 */
	public FluidTraderBlockEntity(BlockPos pos, BlockState state)
	{
		this(ModBlockEntities.FLUID_TRADER.get(), pos, state);
	}
	
	/**
	 * Default Fluid Trader Entity (for children):
	 * Trade Count: 1
	 */
	protected FluidTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		this(type, pos, state, 1);
	}
	
	/**
	 * Default Fluid Trader Entity:
	 * Trade Count: @param tradeCount (up to 8)
	 */
	public FluidTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		this(ModBlockEntities.FLUID_TRADER.get(), pos, state, tradeCount);
	}
	
	/**
	 * Default Fluid Trader Entity (for children):
	 * Trade Count: @param tradeCount (up to 8)
	 */
	protected FluidTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount)
	{
		super(type, pos, state);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, ITrader.GLOBAL_TRADE_LIMIT);
		this.trades = FluidTradeData.listOfSize(this.tradeCount);
	}
	
	public TraderFluidStorage getStorage() { return this.storage; }
	
	public void markStorageDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.writeStorage(new CompoundTag()));
	}
	
	public int getTradeCount()
	{
		return MathUtil.clamp(this.tradeCount, 1, ITrader.GLOBAL_TRADE_LIMIT);
	}
	
	public FluidTradeData getTrade(int tradeIndex)
	{
		if(tradeIndex >= 0 && tradeIndex < this.trades.size())
			return this.trades.get(tradeIndex);
		return new FluidTradeData();
	}
	
	public List<FluidTradeData> getAllTrades()
	{
		return this.trades;
	}
	
	public List<FluidTradeData> getTradeInfo() { return this.trades; }
	
	public TradeFluidHandler getFluidHandler() { return this.fluidHandler; }
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.worldPosition, isAdd));
	}
	
	public void addTrade(Player requestor) {
		if(this.level.isClientSide)
			return;
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
		if(this.level.isClientSide)
			return;
		if(this.tradeCount <= 1)
			return;
		
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "remove trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.tradeCount - 1);
	}
	
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
		this.storage.clearInvalidTanks();
		//Send an update to the client
		this.markTradesDirty();
	}
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(this.fluidSettings); }
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		
		writeTrades(compound);
		writeStorage(compound);
		writeFluidSettings(compound);
		writeUpgradeInventory(compound);
		writeRules(compound);
		writeLogger(compound);
		
		super.saveAdditional(compound);
		
	}
	
	public CompoundTag writeTrades(CompoundTag compound)
	{
		compound.putInt("TradeCount", this.tradeCount);
		FluidTradeData.WriteNBTList(this.trades, compound);
		return compound;
	}
	
	public CompoundTag writeStorage(CompoundTag compound) {
		this.storage.save(compound, "FluidStorage");
		return compound;
	}
	
	public CompoundTag writeFluidSettings(CompoundTag compound)
	{
		compound.put("FluidSettings", this.fluidSettings.save(new CompoundTag()));
		return compound;
	}
	
	public CompoundTag writeUpgradeInventory(CompoundTag compound)
	{
		InventoryUtil.saveAllItems("UpgradeInventory", compound, this.upgradeInventory);
		return compound;
	}
	
	protected CompoundTag writeRules(CompoundTag compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	protected CompoundTag writeLogger(CompoundTag compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		
		super.load(compound);
		
		if(compound.contains("TradeCount", Tag.TAG_INT))
			this.tradeCount = compound.getInt("TradeCount");
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.trades = FluidTradeData.LoadNBTList(this.tradeCount, compound);
		
		if(compound.contains("UpgradeInventory",Tag.TAG_LIST))
			this.upgradeInventory = InventoryUtil.loadAllItems("UpgradeInventory", compound, 5);
		
		if(compound.contains("FluidStorage"))
			this.storage.load(compound, "FluidStorage");
		else if(compound.contains(ItemTradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.storage.loadFromTrades(compound.getList(ItemTradeData.DEFAULT_KEY, Tag.TAG_COMPOUND));
			
		if(compound.contains("FluidSettings", Tag.TAG_COMPOUND))
			this.fluidSettings.load(compound.getCompound("FluidSettings"));
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Tag.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		this.logger.read(compound);
		
	}
	
	@Override
	public boolean drainCapable() { return true; }

	/*@Override
	public MenuProvider getTradeMenuProvider() {
		return new TraderProvider(this);
	}
	
	@Override
	public MenuProvider getStorageMenuProvider() {
		return new TraderStorageProvider(this);
	}
	
	@Override
	public MenuProvider getCashRegisterTradeMenuProvider(CashRegisterBlockEntity registerEntity) {
		return new TradeCRContainerProvider(this, registerEntity);
	}
	
	protected MenuProvider getFluidEditMenuProvider(int tradeIndex) { return new FluidEditContainerProvider(this, tradeIndex); }
	
	@Override
	public void openFluidEditMenu(Player player, int tradeIndex) {
		MenuProvider provider = getFluidEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LCTech.LOGGER.error("No fluid edit container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}
		if(!(player instanceof ServerPlayer))
		{
			LCTech.LOGGER.error("Player is not a server player entity. Cannot open the storage menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayer)player, provider, new TradeIndexDataWriter(this.worldPosition, tradeIndex));
	}
	
	private class TraderProvider implements MenuProvider
	{

		final FluidTraderBlockEntity tileEntity;
		
		private TraderProvider(FluidTraderBlockEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			return new FluidTraderMenu(windowId, inventory, this.tileEntity.worldPosition);
		}

		@Override
		public Component getDisplayName() {
			return this.tileEntity.getName();
		}
		
	}
	
	private class TraderStorageProvider implements MenuProvider
	{

		final FluidTraderBlockEntity tileEntity;
		
		private TraderStorageProvider(FluidTraderBlockEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
			return new FluidTraderStorageMenu(windowId, inventory, this.tileEntity.worldPosition);
		}

		@Override
		public Component getDisplayName() {
			return this.tileEntity.getName();
		}
		
	}*/
	
	/*private class TradeCRContainerProvider implements MenuProvider
	{
		FluidTraderBlockEntity blockEntity;
		CashRegisterBlockEntity registerEntity;
		
		public TradeCRContainerProvider(FluidTraderBlockEntity blockEntity, CashRegisterBlockEntity registerEntity) {
			this.blockEntity = blockEntity;
			this.registerEntity = registerEntity;
		}
		
		public Component getDisplayName() { return blockEntity.getName(); }
		
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
		{
			return new FluidTraderMenuCR(id, inventory, this.blockEntity.worldPosition, registerEntity);
		}
		
	}
	
	private class FluidEditContainerProvider implements MenuProvider
	{
		FluidTraderBlockEntity blockEntity;
		int tradeIndex;
		
		public FluidEditContainerProvider(FluidTraderBlockEntity blockEntity, int tradeIndex) {
			this.blockEntity = blockEntity;
			this.tradeIndex = tradeIndex;
		}
		
		public Component getDisplayName() { return this.blockEntity.getName(); }
		
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
			return new FluidEditMenu(id, inventory, this.blockEntity.worldPosition, tradeIndex);
		}
		
	}*/
	
	@Override
	public void serverTick() {
		//Recalculate the drainable tank to fix the issue with the create pump...
		//Like seriously, I had to stay up til 2 in the morning for this bs...
		//this.fluidHandler.resetDrainableTank();
	}

	@Override
	public int GetCurrentVersion()
	{
		return 0;
	}
	
	@Override
	protected void onVersionUpdate(int oldVersion)
	{
		//Nothing yet, as there's been no version updates for this trader
	}

	@Override
	public int getTradeStock(int tradeIndex) {
		return this.getTrade(tradeIndex).getStock(this);
	}

	@Override
	public void markTradesDirty() {
		
		this.setChanged();
		
		if(!level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeTrades(new CompoundTag()));
		}
		
	}

	public FluidTraderSettings getFluidSettings()
	{
		return this.fluidSettings;
	}
	
	public void markFluidSettingsDirty()
	{
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeFluidSettings(new CompoundTag()));
		}
		this.setChanged();
		this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
	}
	
	@Override
	public void dumpContents(Level level, BlockPos pos)
	{
		//super.dumpContents dumps the coins automatically
		super.dumpContents(level, pos);
		//Dump the extra fluids if present
		this.storage.getContents().forEach(entry ->{
			if(!entry.getTankContents().isEmpty())
				Block.popResource(level, pos, FluidShardItem.GetFluidShard(entry.getTankContents()));
		});
		//Dump the upgrade items if present
		for(int i = 0; i < this.upgradeInventory.getContainerSize(); i++)
		{
			if(!this.upgradeInventory.getItem(i).isEmpty())
				Block.popResource(level, pos, this.upgradeInventory.getItem(i));
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
	public FluidShopLogger getLogger() {
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
	
	public ITradeRuleScreenHandler getRuleScreenHandler(int tradeIndex) { return new TraderScreenHandler(this, tradeIndex); }
	
	private static class TraderScreenHandler implements ITradeRuleScreenHandler
	{
		private FluidTraderBlockEntity blockEntity;
		private final int tradeIndex;
		
		public TraderScreenHandler(FluidTraderBlockEntity blockEntity, int tradeIndex) { this.blockEntity = blockEntity; this.tradeIndex = tradeIndex; }

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
	
	@OnlyIn(Dist.CLIENT)
	public int getTradeRenderLimit()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IFluidTraderBlock)
			return ((IFluidTraderBlock)block).getTradeRenderLimit();
		return 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	public FluidRenderData getRenderPosition(int index)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IFluidTraderBlock)
			return ((IFluidTraderBlock)block).getRenderPosition(this.getBlockState(), index);
		return null;
	}
	
	@Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
		//Return the fluid handler capability
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			Direction relativeSide = side;
			if(this.getBlockState().getBlock() instanceof IRotatableBlock)
			{
				Direction facing = ((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState());
				relativeSide = IItemHandlerBlock.getRelativeSide(facing, side);
			}
			IFluidHandler handler = this.fluidHandler.getExternalHandler(relativeSide);
			if(handler != null)
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> handler));
			return LazyOptional.empty();
		}
		
		//Otherwise return none
		return super.getCapability(cap, side);
    }
	
	@Override
	public void sendUpdateTradeRuleMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule(this.worldPosition, tradeIndex, type, updateInfo));
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
