package io.github.lightman314.lctech.tileentities;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blocks.IFluidTraderBlock;
import io.github.lightman314.lctech.client.gui.screen.TradeFluidPriceScreen.TradePriceData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.common.logger.FluidShopLogger;
import io.github.lightman314.lctech.container.FluidEditContainer;
import io.github.lightman314.lctech.container.FluidTraderContainer;
import io.github.lightman314.lctech.container.FluidTraderContainer.FluidTraderContainerCR;
import io.github.lightman314.lctech.container.FluidTraderStorageContainer;
import io.github.lightman314.lctech.core.ModTileEntities;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageSetFluidPrice;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageSetFluidTradeProduct;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageToggleFluidIcon;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.fluid.TradeFluidHandler;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageUpdateTradeRule;
import io.github.lightman314.lightmanscurrency.tileentity.CashRegisterTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;

public class FluidTraderTileEntity extends TraderTileEntity implements IFluidTrader {
	
	public static final int TRADE_LIMIT = 8;
	
	int tradeCount = 1;
	
	final TradeFluidHandler fluidHandler = new TradeFluidHandler(this);
	
	FluidTraderSettings fluidSettings = new FluidTraderSettings(this, this::markFluidSettingsDirty, this::sendSettingsUpdateToServer);
	
	FluidShopLogger logger = new FluidShopLogger();
	
	List<FluidTradeData> trades;
	
	IInventory upgradeInventory = new Inventory(5);
	public IInventory getUpgradeInventory() { return this.upgradeInventory; }
	public void reapplyUpgrades() { this.reapplyUpgrades(true); }
	private void reapplyUpgrades(boolean markDirty) {
		this.trades.forEach(trade -> trade.applyUpgrades(this, this.upgradeInventory));
		if(markDirty)
			this.markTradesDirty();
	}
	
	List<TradeRule> tradeRules = Lists.newArrayList();
	
	/**
	 * Default Fluid Trader Entity:
	 * Trade Count: 1
	 */
	public FluidTraderTileEntity()
	{
		super(ModTileEntities.FLUID_TRADER);
		this.trades = FluidTradeData.listOfSize(this.tradeCount);
	}
	
	/**
	 * Default Fluid Trader Entity (for children):
	 * Trade Count: 1
	 */
	protected FluidTraderTileEntity(TileEntityType<?> type)
	{
		super(type);
		this.trades = FluidTradeData.listOfSize(this.tradeCount);
	}
	
	/**
	 * Default Fluid Trader Entity (for children):
	 * Trade Count: @param tradeCount (up to 8)
	 */
	protected FluidTraderTileEntity(TileEntityType<?> type, int tradeCount)
	{
		super(type);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, TRADE_LIMIT);
		this.trades = FluidTradeData.listOfSize(this.tradeCount);
	}
	
	/**
	 * Default Fluid Trader Entity:
	 * Trade Count: @param tradeCount (up to 8)
	 */
	public FluidTraderTileEntity(int tradeCount)
	{
		this();
		this.tradeCount = MathUtil.clamp(tradeCount, 1, TRADE_LIMIT);
		this.trades = FluidTradeData.listOfSize(this.tradeCount);
	}
	
	public int getTradeCount()
	{
		return MathUtil.clamp(this.tradeCount, 1, TRADE_LIMIT);
	}
	
	public int getTradeCountLimit() { return TRADE_LIMIT; }
	
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
	
	public TradeFluidHandler getFluidHandler() { return this.fluidHandler; }
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.pos, isAdd));
	}
	
	public void addTrade(PlayerEntity requestor) {
		if(this.world.isRemote)
			return;
		if(this.tradeCount >= TRADE_LIMIT)
			return;
		
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "add trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.tradeCount + 1);
		this.forceReopen();
	}
	
	public void removeTrade(PlayerEntity requestor) {
		if(this.world.isRemote)
			return;
		if(this.tradeCount <= 1)
			return;
		
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "remove trade slot", Permissions.ADMIN_MODE);
			return;
		}
		this.overrideTradeCount(this.tradeCount - 1);
		this.forceReopen();
	}

	@Override
	protected void forceReopen(List<PlayerEntity> users) {
		for(PlayerEntity player : users)
		{
			if(player.openContainer instanceof FluidTraderStorageContainer)
				this.openStorageMenu(player);
			else if(player.openContainer instanceof FluidTraderContainerCR)
				this.openCashRegisterTradeMenu(player, ((FluidTraderContainerCR)player.openContainer).getCashRegister());
			else if(player.openContainer instanceof FluidTraderContainer)
				this.openTradeMenu(player);
		}
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, TRADE_LIMIT);
		List<FluidTradeData> oldTrades = this.trades;
		this.trades = FluidTradeData.listOfSize(this.tradeCount);
		//Write the old trade data into the array
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); i++)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		//Send an update to the client
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeTrades(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(this.fluidSettings); }
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		
		writeTrades(compound);
		writeFluidSettings(compound);
		writeUpgradeInventory(compound);
		writeRules(compound);
		writeLogger(compound);
		
		compound = super.write(compound);
		
		return compound;
	}
	
	public CompoundNBT writeTrades(CompoundNBT compound)
	{
		compound.putInt("TradeCount", this.tradeCount);
		FluidTradeData.WriteNBTList(this.trades, compound);
		return compound;
	}
	
	public CompoundNBT writeFluidSettings(CompoundNBT compound)
	{
		compound.put("FluidSettings", this.fluidSettings.save(new CompoundNBT()));
		return compound;
	}
	
	public CompoundNBT writeUpgradeInventory(CompoundNBT compound)
	{
		InventoryUtil.saveAllItems("UpgradeInventory", compound, this.upgradeInventory);
		return compound;
	}
	
	protected CompoundNBT writeRules(CompoundNBT compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	protected CompoundNBT writeLogger(CompoundNBT compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		
		super.read(state, compound);
		
		if(compound.contains("TradeCount", Constants.NBT.TAG_INT))
			this.tradeCount = compound.getInt("TradeCount");
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Constants.NBT.TAG_LIST))
			this.trades = FluidTradeData.LoadNBTList(this.tradeCount, compound);
		
		if(compound.contains("UpgradeInventory",Constants.NBT.TAG_LIST))
			this.upgradeInventory = InventoryUtil.loadAllItems("UpgradeInventory", compound, 5);
		
		if(compound.contains("FluidSettings",Constants.NBT.TAG_COMPOUND))
			this.fluidSettings.load(compound.getCompound("FluidSettings"));
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Constants.NBT.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		this.logger.read(compound);
		
		this.reapplyUpgrades(false);
		
	}
	
	@Override
	public boolean drainCapable() { return true; }

	@Override
	public INamedContainerProvider getTradeMenuProvider() {
		return new TraderProvider(this);
	}
	
	@Override
	public INamedContainerProvider getStorageMenuProvider() {
		return new TraderStorageProvider(this);
	}
	
	@Override
	public INamedContainerProvider getCashRegisterTradeMenuProvider(CashRegisterTileEntity registerEntity) {
		return new TradeCRContainerProvider(this, registerEntity);
	}
	
	protected INamedContainerProvider getFluidEditMenuProvider(int tradeIndex) { return new FluidEditContainerProvider(this, tradeIndex); }
	
	@Override
	public void openFluidEditMenu(PlayerEntity player, int tradeIndex) {
		INamedContainerProvider provider = getFluidEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LCTech.LOGGER.error("No fluid edit container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}
		if(!(player instanceof ServerPlayerEntity))
		{
			LCTech.LOGGER.error("Player is not a server player entity. Cannot open the storage menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayerEntity)player, provider, new TradeIndexDataWriter(this.pos, tradeIndex));
	}
	
	private class TraderProvider implements INamedContainerProvider
	{

		final FluidTraderTileEntity tileEntity;
		
		private TraderProvider(FluidTraderTileEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		@Override
		public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
			return new FluidTraderContainer(windowId, inventory, this.tileEntity.pos);
		}

		@Override
		public ITextComponent getDisplayName() {
			return this.tileEntity.getName();
		}
		
	}
	
	private class TraderStorageProvider implements INamedContainerProvider
	{

		final FluidTraderTileEntity tileEntity;
		
		private TraderStorageProvider(FluidTraderTileEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		@Override
		public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
			return new FluidTraderStorageContainer(windowId, inventory, this.tileEntity.pos);
		}

		@Override
		public ITextComponent getDisplayName() {
			return this.tileEntity.getName();
		}
		
	}
	
	private class TradeCRContainerProvider implements INamedContainerProvider
	{
		FluidTraderTileEntity tileEntity;
		CashRegisterTileEntity registerEntity;
		
		public TradeCRContainerProvider(FluidTraderTileEntity tileEntity, CashRegisterTileEntity registerEntity) {
			this.tileEntity = tileEntity;
			this.registerEntity = registerEntity;
		}
		
		public ITextComponent getDisplayName() { return tileEntity.getName(); }
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
		{
			return new FluidTraderContainerCR(id, inventory, tileEntity.pos, registerEntity);
		}
		
	}
	
	private class FluidEditContainerProvider implements INamedContainerProvider
	{
		FluidTraderTileEntity tileEntity;
		int tradeIndex;
		
		public FluidEditContainerProvider(FluidTraderTileEntity tileEntity, int tradeIndex) {
			this.tileEntity = tileEntity;
			this.tradeIndex = tradeIndex;
		}
		
		public ITextComponent getDisplayName() { return this.tileEntity.getName(); }
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
			return new FluidEditContainer(id, inventory, tileEntity.pos, tradeIndex);
		}
		
	}
	
	@Override
	public void tick() {
		//Recalculate the drainable tank to fix the issue with the create pump...
		//Like seriously, I had to stay up til 2 in the morning for this bs...
		this.fluidHandler.resetDrainableTank();
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
		
		this.markDirty();
		
		if(!world.isRemote)
		{
			CompoundNBT compound = this.writeTrades(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
		
	}

	public FluidTraderSettings getFluidSettings()
	{
		return this.fluidSettings;
	}
	
	public void markFluidSettingsDirty()
	{
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeFluidSettings(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
		this.markDirty();
		this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockState().getBlock());
	}
	
	@Override
	public void dumpContents(World world, BlockPos pos)
	{
		//super.dumpContents dumps the coins automatically
		super.dumpContents(world, pos);
		//Dump the extra fluids if present
		this.trades.forEach(trade ->{
			if(!trade.getTankContents().isEmpty())
				Block.spawnAsEntity(world, pos, FluidShardItem.GetFluidShard(trade.getTankContents()));
		});
		//Dump the upgrade items if present
		for(int i = 0; i < this.upgradeInventory.getSizeInventory(); i++)
		{
			if(!this.upgradeInventory.getStackInSlot(i).isEmpty())
				Block.spawnAsEntity(world, pos, this.upgradeInventory.getStackInSlot(i));
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
		this.markDirty();
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeRules(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, compound);
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
		this.markDirty();
		if(!this.world.isRemote)
		{
			CompoundNBT compound = writeLogger(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, this.superWrite(compound));
		}
	}
	
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new TraderScreenHandler(this); }
	
	private static class TraderScreenHandler implements ITradeRuleScreenHandler
	{
		private FluidTraderTileEntity tileEntity;
		
		public TraderScreenHandler(FluidTraderTileEntity tileEntity) { this.tileEntity = tileEntity; }

		@Override
		public void reopenLastScreen() {
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.tileEntity.pos));
		}

		@Override
		public ITradeRuleHandler ruleHandler() {
			return this.tileEntity;
		}

		@Override
		public void updateServer(ResourceLocation type, CompoundNBT updateInfo) {
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule(this.tileEntity.pos, type, updateInfo));
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
			IFluidHandler handler = this.fluidHandler.getFluidHandler(relativeSide);
			if(handler != null)
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> handler));
			return LazyOptional.empty();
		}
		
		//Otherwise return default
		return super.getCapability(cap, side);
    }
	
	@Override
	public void sendSetTradeFluidMessage(int tradeIndex, FluidStack newFluid) {
		if(this.isClient())
			LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeProduct(this.pos, tradeIndex, newFluid));
	}

	@Override
	public void sendToggleIconMessage(int tradeIndex, int icon) {
		if(this.isClient())
			LCTechPacketHandler.instance.sendToServer(new MessageToggleFluidIcon(this.pos, tradeIndex, icon));
	}

	@Override
	public void sendPriceMessage(TradePriceData priceData) {
		if(this.isClient())
			LCTechPacketHandler.instance.sendToServer(new MessageSetFluidPrice(this.pos, priceData.tradeIndex, priceData.cost, priceData.type, priceData.quantity, priceData.canDrain, priceData.canFill));
	}

	@Override
	public void sendUpdateTradeRuleMessage(int tradeIndex, ResourceLocation type, CompoundNBT updateInfo) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule(this.pos, tradeIndex, type, updateInfo));
	}
	
	@Override
	public void receiveTradeRuleMessage(PlayerEntity player, int index, ResourceLocation ruleType, CompoundNBT updateInfo) {
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
