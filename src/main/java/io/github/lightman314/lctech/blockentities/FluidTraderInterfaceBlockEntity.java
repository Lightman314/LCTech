package io.github.lightman314.lctech.blockentities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.blockentities.handler.FluidInterfaceHandler;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.ITraderFluidFilter;
import io.github.lightman314.lctech.common.traders.tradedata.fluid.FluidTradeData;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.items.FluidShardItem;
import io.github.lightman314.lctech.menu.traderinterface.fluid.FluidStorageTab;
import io.github.lightman314.lctech.upgrades.TechUpgradeTypes;
import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity implements ITraderFluidFilter {

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.FLUID_CAPACITY);
	
	private TraderFluidStorage fluidBuffer = new TraderFluidStorage(this);
	public TraderFluidStorage getFluidBuffer() { return this.fluidBuffer; }
	
	FluidInterfaceHandler fluidHandler;
	public FluidInterfaceHandler getFluidHandler() { return this.fluidHandler; }
	
	private int refactorTimer = 0;
	
	public FluidTraderInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRADER_INTERFACE_FLUID.get(), pos, state);
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
			TraderData trader = this.getTrader();
			if(trader instanceof FluidTraderData)
			{
				for(FluidTradeData trade : ((FluidTraderData) trader).getAllTrades())
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
			TraderData trader = this.getTrader();
			if(trader instanceof FluidTraderData)
				return ((FluidTraderData)trader).getRelevantFluids();
		}
		return new ArrayList<>();
	}
	
	@Override
	public int getTankCapacity() {
		int defaultCapacity = FluidTraderData.getDefaultTankCapacity();
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
	protected FluidTradeData deserializeTrade(CompoundTag compound) { return FluidTradeData.loadData(compound, false); }
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		this.saveFluidBuffer(compound);
	}
	
	protected final CompoundTag saveFluidBuffer(CompoundTag compound) {
		this.fluidBuffer.save(compound, "Storage");
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
		if(compound.contains("Storage"))
			this.fluidBuffer.load(compound, "Storage");
	}
	
	@Override
	public boolean validTraderType(TraderData trader) { return trader instanceof FluidTraderData; }
	
	protected final FluidTraderData getFluidTrader() {
		TraderData trader = this.getTrader();
		if(trader instanceof FluidTraderData)
			return (FluidTraderData)trader;
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
		FluidTraderData trader = this.getFluidTrader();
		if(trader != null && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
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
		FluidTraderData trader = this.getFluidTrader();
		if(trader != null && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
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
	protected void hopperTick() {
		AtomicBoolean markBufferDirty = new AtomicBoolean(false);
		for(Direction relativeSide : Direction.values())
		{
			if(this.fluidHandler.getInputSides().get(relativeSide))
			{
				Direction actualSide = relativeSide;
				if(this.getBlockState().getBlock() instanceof IRotatableBlock)
				{
					IRotatableBlock b = (IRotatableBlock)this.getBlockState().getBlock();
					actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);
				}
				
				BlockPos queryPos = this.worldPosition.relative(actualSide);
				BlockEntity be = this.level.getBlockEntity(queryPos);
				if(be != null)
				{
					be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, actualSide.getOpposite()).ifPresent(fluidHandler -> {
						boolean query = true;
						for(int i = 0; query && i < fluidHandler.getTanks(); ++i)
						{
							FluidStack stack = fluidHandler.getFluidInTank(i);
							int fillableAmount = this.fluidBuffer.getFillableAmount(stack);
							if(fillableAmount > 0)
							{
								query = false;
								FluidStack drainStack = stack.copy();
								drainStack.setAmount(fillableAmount);
								FluidStack result = fluidHandler.drain(drainStack, FluidAction.EXECUTE);
								this.fluidBuffer.forceFillTank(result);
								markBufferDirty.set(true);
							}
						}
					});
				}
			}
		}
		if(markBufferDirty.get())
			this.setFluidBufferDirty();
	}
	
	@Override
	public void initMenuTabs(TraderInterfaceMenu menu) {
		menu.setTab(TraderInterfaceTab.TAB_STORAGE, new FluidStorageTab(menu));
	}
	
	@Override
	public boolean allowAdditionalUpgrade(UpgradeType type) { return ALLOWED_UPGRADES.contains(type); }

	@Override
	public void getAdditionalContents(List<ItemStack> contents) {
		this.fluidBuffer.getContents().forEach(entry ->{
			if(!entry.getTankContents().isEmpty())
				contents.add(FluidShardItem.GetFluidShard(entry.getTankContents()));
		});
	}

	@Override
	public MutableComponent getName() { return new TranslatableComponent("block.lctech.fluid_trader_interface"); }
	
}