package io.github.lightman314.lctech.common.blockentities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.blockentities.handler.FluidInterfaceHandler;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.ITraderFluidFilter;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lctech.common.menu.traderinterface.fluid.FluidStorageTab;
import io.github.lightman314.lctech.common.upgrades.TechUpgradeTypes;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.data.TradeReference;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public class FluidTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity implements ITraderFluidFilter {

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.FLUID_CAPACITY);

	private final TraderFluidStorage fluidBuffer = new TraderFluidStorage(this);
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
	public void setTargetsDirty() {
		super.setTargetsDirty();
		//Refactor the fluid buffers tanks whenever the selected trade/trader is updated.
		if(this.fluidBuffer.refactorTanks())
			this.setFluidBufferDirty();
	}

	@Override
	public TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext) {
		return baseContext.withFluidHandler(this.fluidBuffer);
	}

	public boolean allowInput(FluidStack fluid) {
		if(this.getInteractionType().trades())
		{
			//Check trade for purchase fluid to restock
			for(TradeReference tr : this.targets.getTradeReferences())
			{
				TradeData t = tr.getLocalTrade();
				if(t instanceof FluidTradeData trade && trade.isPurchase() && trade.getProduct().isFluidEqual(fluid))
					return true;
			}
		}
		else
		{
			//Scan all trades for sell fluids to restock
			for(TraderData trader : this.targets.getTraders())
			{
				if(trader instanceof FluidTraderData ft)
				{
					for(FluidTradeData trade : ft.getTradeData())
					{
						if(trade.isSale())
						{
							if(trade.getProduct().isFluidEqual(fluid))
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean allowOutput(FluidStack fluid) { return !this.allowInput(fluid); }

	@Override
	public List<FluidStack> getRelevantFluids() {
		if(this.getInteractionType().trades())
		{
			List<FluidStack> result = new ArrayList<>();
			for(TradeReference tr : this.targets.getTradeReferences())
			{
				TradeData t = tr.getLocalTrade();
				if(t instanceof FluidTradeData trade)
				{
					if(!trade.getProduct().isEmpty())
						FluidItemUtil.addFluidToRelevanceList(result,trade.getProduct());
				}
			}
			return result;
		}
		else
		{
			List<FluidStack> result = new ArrayList<>();
			for(TraderData t : this.targets.getTraders())
			{
				if(t instanceof FluidTraderData trader)
					FluidItemUtil.addFluidsToRelevanceList(result,trader.getRelevantFluids());
			}
			return result;
		}
	}

	@Override
	public int getTankCapacity() {
		int defaultCapacity = FluidTraderData.getDefaultTankCapacity();
		int tankCapacity = defaultCapacity;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < this.getUpgrades().getContainerSize(); i++)
		{
			ItemStack stack = this.getUpgrades().getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(this.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() == TechUpgradeTypes.FLUID_CAPACITY)
					{
						int addAmount = UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
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
	public FluidTradeData deserializeTrade(@Nonnull CompoundTag compound) { return FluidTradeData.loadData(compound, false); }

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
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

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		if(compound.contains("Storage"))
			this.fluidBuffer.load(compound, "Storage");
	}

	@Override
	public boolean validTraderType(TraderData trader) { return trader instanceof FluidTraderData; }

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
	protected void drainTick(@Nonnull TraderData t) {
		if(t instanceof FluidTraderData trader && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
		{
			TraderFluidStorage storage = trader.getStorage();
			boolean setChanged = false;
			for(FluidTradeData trade : trader.getTradeData())
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
	protected void restockTick(@Nonnull TraderData t) {
		if(t instanceof FluidTraderData trader && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
		{
			TraderFluidStorage storage = trader.getStorage();
			boolean setChanged = false;
			for(FluidTradeData trade : trader.getTradeData())
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
	protected void tradeTick(@Nonnull TradeReference tr) {
		TradeData t = tr.getTrueTrade();
		if(t instanceof FluidTradeData trade)
		{
			if(trade != null && trade.isValid())
			{
				if(trade.isSale())
				{
					//Confirm that we have enough space to store the purchased fluid
					if(this.fluidBuffer.getFillableAmount(trade.getProduct()) >= trade.getQuantity())
					{
						if(this.TryExecuteTrade(tr).isSuccess())
							this.setFluidBufferDirty();
					}
				}
				else if(trade.isPurchase())
				{
					//Confirm that we have enough of the fluid in storage to buy the fluid
					if(this.fluidBuffer.getActualFluidCount(trade.getProduct()) >= trade.getQuantity())
					{
						if(this.TryExecuteTrade(tr).isSuccess())
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
			if(this.fluidHandler.allowInputSide(relativeSide) || this.fluidHandler.allowOutputSide(relativeSide))
			{
				Direction actualSide = relativeSide;
				if(this.getBlockState().getBlock() instanceof IRotatableBlock b)
					actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);

				BlockPos queryPos = this.worldPosition.relative(actualSide);
				BlockEntity be = this.level.getBlockEntity(queryPos);
				IFluidHandler fluidHandler = be == null ? null : be.getCapability(ForgeCapabilities.FLUID_HANDLER, actualSide.getOpposite()).orElse(null);
				if(fluidHandler != null)
				{
					//Collect fluids from neighboring blocks
					if(this.fluidHandler.allowInputSide(relativeSide))
					{
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
								FluidStack result = fluidHandler.drain(drainStack, IFluidHandler.FluidAction.EXECUTE);
								this.fluidBuffer.forceFillTank(result);
								markBufferDirty.set(true);
							}
						}
					}
					//Output fluids to neighboring blocks
					if(this.fluidHandler.allowOutputSide(relativeSide))
					{
						List<FluidEntry> entries = this.fluidBuffer.getContents();
						boolean query = true;
						for(int i = 0; query && i < entries.size(); ++i)
						{
							FluidStack fluid = entries.get(i).getTankContents();
							if(this.allowOutput(fluid))
							{
								int fillAmount = fluidHandler.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
								if(fillAmount > 0)
								{
									query = false;
									if(!fluid.isEmpty())
										fluid.setAmount(fillAmount);
									this.fluidBuffer.drain(fluid);
									markBufferDirty.set(true);
								}
							}
						}
					}
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

}