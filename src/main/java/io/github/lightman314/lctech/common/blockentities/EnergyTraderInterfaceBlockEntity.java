package io.github.lightman314.lctech.common.blockentities;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.common.blockentities.handler.EnergyInterfaceHandler;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.common.core.ModBlockEntities;
import io.github.lightman314.lctech.common.menu.traderinterface.energy.EnergyStorageTab;
import io.github.lightman314.lctech.common.upgrades.TechUpgradeTypes;
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
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

public class EnergyTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity {

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.ENERGY_CAPACITY);
	
	EnergyInterfaceHandler energyHandler;
	public EnergyInterfaceHandler getEnergyHandler() { return this.energyHandler; }
	
	int energyStorage = 0;
	public int getStoredEnergy() { return this.energyStorage; }
	public void addStoredEnergy(int amount) { this.energyStorage += amount; this.setEnergyBufferDirty(); }
	public void drainStoredEnergy(int amount) { this.energyStorage -= amount; this.setEnergyBufferDirty(); }
	public int getMaxEnergy() {
		int defaultCapacity = EnergyTraderData.getDefaultMaxEnergy();
		int capacity = defaultCapacity;
		boolean baseStorageCompensation = false;
		for(int i = 0; i < this.getUpgrades().getContainerSize(); i++)
		{
			ItemStack stack = this.getUpgrades().getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(this.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					{
						int addAmount = UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
						if(addAmount > defaultCapacity && !baseStorageCompensation)
						{
							addAmount -= defaultCapacity;
							baseStorageCompensation = true;
						}
						capacity += addAmount;
					}
				}	
			}
		}
		return capacity;
	}
	
	public EnergyTraderInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRADER_INTERFACE_ENERGY.get(), pos, state);
		this.energyHandler = this.addHandler(new EnergyInterfaceHandler(this));
	}
	
	@Override
	public List<InteractionType> getBlacklistedInteractions() { return Lists.newArrayList(InteractionType.RESTOCK_AND_DRAIN); }
	
	@Override
	public TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext) {
		return baseContext.withEnergyHandler(this.energyHandler.tradeHandler);
	}
	
	@Override
	public TradeData deserializeTrade(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { return EnergyTradeData.loadData(compound, false, lookup); }
	
	@Override
	public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		super.saveAdditional(compound,lookup);
		this.saveEnergyBuffer(compound);
	}
	
	protected final CompoundTag saveEnergyBuffer(CompoundTag compound) {
		compound.putInt("Energy", this.energyStorage);
		return compound;
	}
	
	public void setEnergyBufferDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveEnergyBuffer(new CompoundTag()));
	}
	
	@Override
	public void loadAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		super.loadAdditional(compound,lookup);
		if(compound.contains("Energy"))
			this.energyStorage = compound.getInt("Energy");
	}
	
	@Override
	public boolean validTraderType(TraderData trader) { return trader instanceof EnergyTraderData; }
	
	@Override
	protected void drainTick(@Nonnull TraderData t) {
		if(t instanceof EnergyTraderData trader && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
		{
			int drainableAmount = Math.min(this.getMaxEnergy() - this.getStoredEnergy(), trader.getAvailableEnergy());
			if(drainableAmount > 0)
			{
				drainableAmount = Math.min(drainableAmount, TechConfig.SERVER.energyRestockSpeed.get());
				trader.shrinkEnergy(drainableAmount);
				trader.markEnergyStorageDirty();
				this.addStoredEnergy(drainableAmount);
			}
		}
	}
	
	@Override
	protected void restockTick(@Nonnull TraderData t) {
		if(t instanceof EnergyTraderData trader && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
		{
			int restockableAmount = Math.min(this.getStoredEnergy(), trader.getMaxEnergy() - trader.getTotalEnergy());
			if(restockableAmount > 0)
			{
				restockableAmount = Math.min(restockableAmount, TechConfig.SERVER.energyRestockSpeed.get());
				trader.addEnergy(restockableAmount);
				trader.markEnergyStorageDirty();
				this.drainStoredEnergy(restockableAmount);
			}
		}
	}
	
	@Override
	protected void tradeTick(@Nonnull TradeReference tr) {
		TradeData t = tr.getTrueTrade();
		if(t instanceof EnergyTradeData trade)
		{
			if(trade != null && trade.isValid())
			{
				if(trade.isSale())
				{
					//Confirm that we have enough space to store the purchased energy
					if(this.getMaxEnergy() - this.energyStorage >= trade.getAmount())
					{
						if(this.TryExecuteTrade(tr).isSuccess())
							this.setEnergyBufferDirty();
					}
				}
				else if(trade.isPurchase())
				{
					//Confirm that we have enough of the energy in storage to sell the energy
					if(this.energyStorage >= trade.getAmount())
					{
						if(this.TryExecuteTrade(tr).isSuccess())
							this.setEnergyBufferDirty();
					}
				}
			}
		}
	}
	
	@Override
	public void serverTick() {
		super.serverTick();
		//Push energy out
		if(this.energyStorage > 0)
		{
			for(Direction direction : Direction.values())
			{
				if(this.energyHandler.allowOutputSide(direction) && this.energyStorage > 0)
				{
					Direction trueSide = this.getBlockState().getBlock() instanceof IRotatableBlock ? IRotatableBlock.getActualSide(((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState()), direction) : direction;
					IEnergyStorage energyHandler = this.level.getCapability(Capabilities.EnergyStorage.BLOCK, this.worldPosition.relative(trueSide), trueSide.getOpposite());
					if(energyHandler != null)
					{
						int extractedAmount = energyHandler.receiveEnergy(this.energyStorage, false);
						if(extractedAmount > 0) //Automatically marks the energy storage dirty
							this.drainStoredEnergy(extractedAmount);
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
			if(this.energyHandler.allowInputSide(relativeSide))
			{
				Direction actualSide = relativeSide;
				if(this.getBlockState().getBlock() instanceof IRotatableBlock b)
					actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);
				
				BlockPos queryPos = this.worldPosition.relative(actualSide);
				IEnergyStorage energyHandler = this.level.getCapability(Capabilities.EnergyStorage.BLOCK, queryPos, actualSide.getOpposite());
				if(energyHandler != null)
				{
					int extractedAmount = energyHandler.extractEnergy(this.getMaxEnergy() - this.energyStorage, false);
					if(extractedAmount > 0)
					{
						this.energyStorage += extractedAmount;
						markBufferDirty.set(true);
					}
				}
			}
		}
		if(markBufferDirty.get())
			this.setEnergyBufferDirty();
	}
	
	@Override
	public void initMenuTabs(TraderInterfaceMenu menu) {
		menu.setTab(TraderInterfaceTab.TAB_STORAGE, new EnergyStorageTab(menu));
	}
	
	@Override
	public boolean allowAdditionalUpgrade(UpgradeType type) { return ALLOWED_UPGRADES.contains(type); }
	
	@Override
	public void getAdditionalContents(List<ItemStack> contents) {}
	
}
