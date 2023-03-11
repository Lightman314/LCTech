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
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.energy.CapabilityEnergy;

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
	
	public EnergyTraderInterfaceBlockEntity() {
		super(ModBlockEntities.TRADER_INTERFACE_ENERGY.get());
		this.energyHandler = this.addHandler(new EnergyInterfaceHandler(this));
	}
	
	@Override
	public List<InteractionType> getBlacklistedInteractions() { return Lists.newArrayList(InteractionType.RESTOCK_AND_DRAIN); }
	
	@Override
	public TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext) {
		return baseContext.withEnergyHandler(this.energyHandler.tradeHandler);
	}
	
	@Override
	protected TradeData deserializeTrade(CompoundNBT compound) { return EnergyTradeData.loadData(compound, false); }
	
	@Override
	@Nonnull
	public CompoundNBT save(@Nonnull CompoundNBT compound) {
		compound = super.save(compound);
		this.saveEnergyBuffer(compound);
		return compound;
	}
	
	protected final CompoundNBT saveEnergyBuffer(CompoundNBT compound) {
		compound.putInt("Energy", this.energyStorage);
		return compound;
	}
	
	public void setEnergyBufferDirty() {
		this.setChanged();
		if(!this.isClient())
			BlockEntityUtil.sendUpdatePacket(this, this.saveEnergyBuffer(new CompoundNBT()));
	}
	
	@Override
	public void load(@Nonnull BlockState state, CompoundNBT compound) {
		super.load(state, compound);
		if(compound.contains("Energy"))
			this.energyStorage = compound.getInt("Energy");
	}
	
	@Override
	public boolean validTraderType(TraderData trader) { return trader instanceof EnergyTraderData; }
	
	protected final EnergyTraderData getEnergyTrader() {
		TraderData trader = this.getTrader();
		if(trader instanceof EnergyTraderData)
			return (EnergyTraderData)trader;
		return null;
	}
	
	@Override
	protected void drainTick() {
		EnergyTraderData trader = this.getEnergyTrader();
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
		EnergyTraderData trader = this.getEnergyTrader();
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
	public void tick() {
		super.tick();
		//Push energy out
		if(this.isServer() && this.energyStorage > 0)
		{
			for(Direction direction : Direction.values())
			{
				if(this.energyHandler.getOutputSides().get(direction) && this.energyStorage > 0)
				{
					Direction trueSide = this.getBlockState().getBlock() instanceof IRotatableBlock ? IRotatableBlock.getActualSide(((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState()), direction) : direction;
					TileEntity be = this.level.getBlockEntity(this.worldPosition.relative(trueSide));
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
	protected void hopperTick() {
		AtomicBoolean markBufferDirty = new AtomicBoolean(false);
		for(Direction relativeSide : Direction.values())
		{
			if(this.energyHandler.getInputSides().get(relativeSide))
			{
				Direction actualSide = relativeSide;
				if(this.getBlockState().getBlock() instanceof IRotatableBlock)
				{
					IRotatableBlock b = (IRotatableBlock)this.getBlockState().getBlock();
					actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getBlockState()), relativeSide);
				}
				
				BlockPos queryPos = this.worldPosition.relative(actualSide);
				TileEntity be = this.level.getBlockEntity(queryPos);
				if(be != null)
				{
					be.getCapability(CapabilityEnergy.ENERGY, actualSide.getOpposite()).ifPresent(energyHandler -> {
						int extractedAmount = energyHandler.extractEnergy(this.getMaxEnergy() - this.energyStorage, false);
						if(extractedAmount > 0)
						{
							this.energyStorage += extractedAmount;
							markBufferDirty.set(true);
						}
					});
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
	
	@Override
	public IFormattableTextComponent getName() { return EasyText.translatable("block.lctech.energy_trader_interface"); }
	
}