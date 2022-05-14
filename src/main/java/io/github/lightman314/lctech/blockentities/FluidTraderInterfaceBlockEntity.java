package io.github.lightman314.lctech.blockentities;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.blockentities.handler.FluidInterfaceHandler;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.menu.traderinterface.fluid.FluidStorageTab;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage.ITraderFluidFilter;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.upgrades.TechUpgradeTypes;
import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class FluidTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity implements ITraderFluidFilter, IUpgradeable {

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.FLUID_CAPACITY);
	
	public boolean allowUpgrade(UpgradeType type) { return ALLOWED_UPGRADES.contains(type); }
	
	private TraderFluidStorage fluidBuffer = new TraderFluidStorage(this);
	public TraderFluidStorage getFluidBuffer() { return this.fluidBuffer; }
	
	FluidInterfaceHandler fluidHandler;
	public FluidInterfaceHandler getFluidHandler() { return this.fluidHandler; }
	
	Container upgradeInventory = new SimpleContainer(5);
	public Container getUpgradeInventory() { return this.upgradeInventory; }
	
	private int refactorTimer = 0;
	
	public FluidTraderInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRADER_INTERFACE_FLUID, pos, state);
		this.fluidHandler = this.addHandler(new FluidInterfaceHandler(this));
	}
	
	@Override
	public void setInteractionDirty() {
		super.setInteractionDirty();
		//Refactor the fluid buffers tanks whenever the interaction mode is updated.
		if(this.fluidBuffer.refactorTanks())
			this.setFluidBufferDirty();
	}
	
	@Override
	public void setTradeReferenceDirty() {
		super.setTradeReferenceDirty();
		//Refactor the fluid buffers tanks whenever the selected trade/trader is updated.
		if(this.fluidBuffer.refactorTanks())
			this.setFluidBufferDirty();
	}
	
	@Override
	public TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext) {
		return baseContext.withFluidHandler(this.fluidBuffer);
	}
	
	public boolean allowInput(FluidStack fluid) {
		if(this.getInteractionType().trades)
		{
			//Check trade for purchase fluid to restock
			TradeData t = this.getReferencedTrade();
			if(t instanceof FluidTradeData)
			{
				FluidTradeData trade = (FluidTradeData)t;
				if(trade.isPurchase() && trade.getProduct().isFluidEqual(fluid))
					return true;
			}
			return false;
		}
		else
		{
			//Scan all trades for sell fluids to restock
			UniversalTraderData trader = this.getTrader();
			if(trader instanceof IFluidTrader)
			{
				for(FluidTradeData trade : ((IFluidTrader) trader).getAllTrades())
				{
					if(trade.isSale())
					{
						if(trade.getProduct().isFluidEqual(fluid))
							return true;
					}
				}
			}
			return false;
		}
	}
	
	public boolean allowOutput(FluidStack fluid) { return !this.allowInput(fluid); }

	@Override
	public List<FluidStack> getRelevantFluids() {
		if(this.getInteractionType().trades)
		{
			TradeData t = this.getReferencedTrade();
			List<FluidStack> result = new ArrayList<>();
			if(t instanceof FluidTradeData)
			{
				FluidTradeData trade = (FluidTradeData)t;
				if(!trade.getProduct().isEmpty())
					result.add(trade.getProduct());
			}
			return result;
		}
		else
		{
			UniversalTraderData trader = this.getTrader();
			if(trader instanceof IFluidTrader)
				return ((IFluidTrader)trader).getRelevantFluids();
		}
		return new ArrayList<>();
	}
	
	@Override
	public int getTankCapacity() {
		int defaultCapacity = IFluidTrader.getDefaultTankCapacity();
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
	
	@Override
	protected FluidTradeData deserializeTrade(CompoundTag compound) { return FluidTradeData.loadData(compound); }
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		this.saveFluidBuffer(compound);
		this.saveUpgradeSlots(compound);
	}
	
	protected final CompoundTag saveFluidBuffer(CompoundTag compound) {
		this.fluidBuffer.save(compound, "Storage");
		return compound;
	}
	
	protected final CompoundTag saveUpgradeSlots(CompoundTag compound) {
		InventoryUtil.saveAllItems("Upgrades", compound, this.upgradeInventory);
		return compound;
	}
	
	public void setFluidBufferDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveFluidBuffer(new CompoundTag()));
	}
	
	public void setUpgradesDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveUpgradeSlots(new CompoundTag()));
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		if(compound.contains("Upgrades",Tag.TAG_LIST))
			this.upgradeInventory = InventoryUtil.loadAllItems("Upgrades", compound, 5);
		if(compound.contains("Storage"))
			this.fluidBuffer.load(compound, "Storage");
	}
	
	@Override
	public boolean validTraderType(UniversalTraderData trader) { return trader instanceof IFluidTrader; }
	
	protected final IFluidTrader getFluidTrader() {
		UniversalTraderData trader = this.getTrader();
		if(trader instanceof IFluidTrader)
			return (IFluidTrader)trader;
		return null;
	}
	
	@Override
	public void serverTick() { 
		this.refactorTimer--;
		if(this.refactorTimer <= 0)
		{
			this.refactorTimer = 20;
			if(this.fluidBuffer.refactorTanks())
				this.setFluidBufferDirty();
		}
		super.serverTick();
	}
	
	@Override
	protected void drainTick() {
		IFluidTrader trader = this.getFluidTrader();
		if(trader != null && trader.hasPermission(this.getOwner(), Permissions.INTERACTION_LINK))
		{
			TraderFluidStorage storage = trader.getStorage();
			boolean setChanged = false;
			for(FluidTradeData trade : trader.getAllTrades())
			{
				if(trade.isValid() && trade.isPurchase())
				{
					//Attempt to drain the fluid
					FluidStack drainFluid = trade.getProduct();
					int drainableAmount = Math.min(storage.getActualFluidCount(drainFluid), this.fluidBuffer.getFillableAmount(drainFluid));
					if(drainableAmount > 0)
					{
						FluidStack movingStack = drainFluid.copy();
						movingStack.setAmount(Math.min(TechConfig.SERVER.fluidRestockSpeed.get(),drainableAmount));
						storage.drain(movingStack);
						
						this.fluidBuffer.forceFillTank(movingStack);
						setChanged = true;
					}
				}
			}
			if(setChanged)
			{
				trader.markStorageDirty();
				this.setFluidBufferDirty();
			}
		}
	}
	
	@Override
	protected void restockTick() {
		IFluidTrader trader = this.getFluidTrader();
		if(trader != null && trader.hasPermission(this.getOwner(), Permissions.INTERACTION_LINK))
		{
			TraderFluidStorage storage = trader.getStorage();
			boolean setChanged = false;
			for(FluidTradeData trade : trader.getAllTrades())
			{
				if(trade.isValid() && trade.isSale())
				{
					//Attempt to drain the fluid
					FluidStack fillFluid = trade.getProduct();
					int drainableAmount = Math.min(storage.getFillableAmount(fillFluid), this.fluidBuffer.getActualFluidCount(fillFluid));
					if(drainableAmount > 0)
					{
						FluidStack movingStack = fillFluid.copy();
						movingStack.setAmount(Math.min(TechConfig.SERVER.fluidRestockSpeed.get(),drainableAmount));
						storage.forceFillTank(movingStack);
						
						this.fluidBuffer.drain(movingStack);
						setChanged = true;
					}
				}
			}
			if(setChanged)
			{
				trader.markStorageDirty();
				this.setFluidBufferDirty();
			}
		}
	}
	
	@Override
	protected void tradeTick() {
		TradeData t = this.getTrueTrade();
		if(t instanceof FluidTradeData)
		{
			FluidTradeData trade = (FluidTradeData)t;
			if(trade != null && trade.isValid())
			{
				if(trade.isSale())
				{
					//Confirm that we have enough space to store the purchased fluid
					if(this.fluidBuffer.getFillableAmount(trade.getProduct()) >= trade.getQuantity())
					{
						this.interactWithTrader();
						this.setFluidBufferDirty();
					}
				}
				else if(trade.isPurchase())
				{
					//Confirm that we have enough of the fluid in storage to buy the fluid
					if(this.fluidBuffer.getActualFluidCount(trade.getProduct()) >= trade.getQuantity())
					{
						this.interactWithTrader();
						this.setFluidBufferDirty();
					}
				}
			}
		}
	}
	
	@Override
	public void dumpContents(Level level, BlockPos pos) {
		//Dump the extra fluids if present
		this.fluidBuffer.getContents().forEach(entry ->{
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
	public void initMenuTabs(TraderInterfaceMenu menu) {
		menu.setTab(TraderInterfaceTab.TAB_STORAGE, new FluidStorageTab(menu));
	}
}
