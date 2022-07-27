package io.github.lightman314.lctech.blockentities;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.blockentities.handler.EnergyInterfaceHandler;
import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.menu.traderinterface.energy.EnergyStorageTab;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.upgrades.TechUpgradeTypes;
import io.github.lightman314.lctech.util.DirectionalUtil;
import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.CapabilityEnergy;

public class EnergyTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity {

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(TechUpgradeTypes.ENERGY_CAPACITY);
	
	EnergyInterfaceHandler energyHandler;
	public EnergyInterfaceHandler getEnergyHandler() { return this.energyHandler; }
	
	int energyStorage = 0;
	public int getStoredEnergy() { return this.energyStorage; }
	public void addStoredEnergy(int amount) { this.energyStorage += amount; this.setEnergyBufferDirty(); }
	public void drainStoredEnergy(int amount) { this.energyStorage -= amount; this.setEnergyBufferDirty(); }
	public int getMaxEnergy() {
		int defaultCapacity = IEnergyTrader.getDefaultMaxEnergy();
		int capacity = defaultCapacity;
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
						capacity += addAmount;
					}
				}	
			}
		}
		return capacity;
	}
	
	//Container upgradeInventory = new SimpleContainer(5);
	//public Container getUpgradeInventory() { return this.upgradeInventory; }
	
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
	protected TradeData deserializeTrade(CompoundTag compound) { return EnergyTradeData.loadData(compound); }
	
	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
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
	public void load(CompoundTag compound) {
		super.load(compound);
		if(compound.contains("Energy"))
			this.energyStorage = compound.getInt("Energy");
	}
	
	@Override
	public boolean validTraderType(UniversalTraderData trader) { return trader instanceof IEnergyTrader; }
	
	protected final IEnergyTrader getEnergyTrader() {
		UniversalTraderData trader = this.getTrader();
		if(trader instanceof IEnergyTrader)
			return (IEnergyTrader)trader;
		return null;
	}
	
	@Override
	protected void drainTick() {
		IEnergyTrader trader = this.getEnergyTrader();
		if(trader != null && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
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
	protected void restockTick() {
		IEnergyTrader trader = this.getEnergyTrader();
		if(trader != null && trader.hasPermission(this.getReferencedPlayer(), Permissions.INTERACTION_LINK))
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
	protected void tradeTick() {
		TradeData t = this.getTrueTrade();
		if(t instanceof EnergyTradeData)
		{
			EnergyTradeData trade = (EnergyTradeData)t;
			if(trade != null && trade.isValid())
			{
				if(trade.isSale())
				{
					//Confirm that we have enough space to store the purchased energy
					if(this.getMaxEnergy() - this.energyStorage >= trade.getAmount())
					{
						this.interactWithTrader();
						this.setEnergyBufferDirty();
					}
				}
				else if(trade.isPurchase())
				{
					//Confirm that we have enough of the energy in storage to sell the energy
					if(this.energyStorage >= trade.getAmount())
					{
						this.interactWithTrader();
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
				if(this.energyHandler.getOutputSides().get(direction) && this.energyStorage > 0)
				{
					Direction trueSide = this.getBlockState().getBlock() instanceof IRotatableBlock ? DirectionalUtil.getTrueSide(((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState()), direction) : direction;
					BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(trueSide));
					if(be != null)
					{
						be.getCapability(CapabilityEnergy.ENERGY, trueSide.getOpposite()).ifPresent(energyHandler ->{
							int extractedAmount = energyHandler.receiveEnergy(this.energyStorage, false);
							if(extractedAmount > 0) //Automatically marks the energy storage dirty
								this.drainStoredEnergy(extractedAmount);
						});
					}
				}
			}
		}
	}
	
	@Override
	public void initMenuTabs(TraderInterfaceMenu menu) {
		menu.setTab(TraderInterfaceTab.TAB_STORAGE, new EnergyStorageTab(menu));
	}
	
	@Override
	public boolean allowAdditionalUpgrade(UpgradeType type) { return ALLOWED_UPGRADES.contains(type); }
	
	@Override
	public void dumpContents(List<ItemStack> contents) { }
	
	@Override
	public MutableComponent getName() { return Component.translatable("block.lctech.energy_trader_interface"); }
	
}
