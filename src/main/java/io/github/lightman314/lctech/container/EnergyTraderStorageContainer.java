package io.github.lightman314.lctech.container;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.container.slots.BatteryInputSlot;
import io.github.lightman314.lctech.container.slots.UpgradeInputSlot;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.inventories.SuppliedInventory;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EnergyTraderStorageContainer extends Container implements ITraderStorageContainer {
	
	private final Supplier<IEnergyTrader> traderSource;
	public Supplier<IEnergyTrader> getTraderSource() { return this.traderSource; }
	public IEnergyTrader getTrader() { return this.traderSource == null ? null : this.traderSource.get(); }
	
	public final PlayerEntity player;
	
	IInventory batteryInventory = new Inventory(2);
	IInventory upgradeSlots;
	IInventory coinSlots = new Inventory(5);
	
	//World Constructor
	public EnergyTraderStorageContainer(int windowID, PlayerInventory inventory, BlockPos traderPosition)
	{
		this(ModContainers.ENERGY_TRADER_STORAGE, windowID, inventory, traderPosition);
	}
	
	protected EnergyTraderStorageContainer(ContainerType<?> type, int windowID, PlayerInventory inventory, BlockPos traderPosition)
	{
		this(type, windowID, inventory, () -> {
			TileEntity be = inventory.player.world.getTileEntity(traderPosition);
			if(be instanceof IEnergyTrader)
				return (IEnergyTrader)be;
			return null;
		});
	}
	
	protected EnergyTraderStorageContainer(ContainerType<?> type, int windowID, PlayerInventory inventory, @Nonnull Supplier<IEnergyTrader> traderSource) {
		super(type, windowID);
		
		this.player = inventory.player;
		this.traderSource = traderSource;
		
		
		
		this.getTrader().userOpen(this.player);
		
		//Upgrade Slots
		this.upgradeSlots = new SuppliedInventory(new SafeUpgradeSlotSupplier(this.traderSource));
		for(int i = 0; i < this.upgradeSlots.getSizeInventory(); ++i)
		{
			this.addSlot(new UpgradeInputSlot(this.upgradeSlots, i, 80 + 18 * i, 89, this.getTrader(), this::OnUpgradeSlotChanges));
		}
		
		//Coin Slots
		for(int i = 0; i < 5; ++i)
		{
			this.addSlot(new CoinSlot(this.coinSlots, i, 176 + 8, 109 + 18 * i));
		}
		
		//Battery Slot
		this.addSlot(new BatteryInputSlot(batteryInventory, 0, 8, 89));
		//Output
		this.addSlot(new OutputSlot(batteryInventory, 1, 44, 89));
		
		//Player Inventory
		for(int y = 0; y < 3; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 121 + y * 18));
			}
		}
		
		//Player Hotbar
		for(int x = 0; x < 9; ++x)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 179));
		}
		
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int index)
	{
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			clickedStack = slotStack.copy();
			if(index < this.coinSlots.getSizeInventory() + this.batteryInventory.getSizeInventory() + this.upgradeSlots.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.coinSlots.getSizeInventory() + this.batteryInventory.getSizeInventory() + this.upgradeSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.inventorySlots.size())
			{
				//Merge coins into coin -> upgrade slots
				if(!this.mergeItemStack(slotStack, 0, this.coinSlots.getSizeInventory() + this.batteryInventory.getSizeInventory() + this.upgradeSlots.getSizeInventory(),false))
				{
					return ItemStack.EMPTY;
				}
			}
			
			if(slotStack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		
		return clickedStack;
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity player) {
		return this.getTrader() != null;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity player)
	{
		super.onContainerClosed(player);
		this.clearContainer(player, player.world, this.coinSlots);
		this.clearContainer(player, player.world, this.batteryInventory);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(player);
		
		MinecraftForge.EVENT_BUS.unregister(this);
		
	}
	
	public boolean hasPermission(String permission)
	{
		if(this.getTrader() != null)
			return this.getTrader().hasPermission(this.player, permission);
		return false;
	}
	
	public int getPermissionLevel(String permission)
	{
		if(this.getTrader() != null)
			return this.getTrader().getPermissionLevel(this.player, permission);
		return 0;
	}
	
	private void OnUpgradeSlotChanges()
	{
		this.getTrader().reapplyUpgrades();
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void AddCoins()
	{
		if(this.getTrader() == null)
		{
			this.player.closeScreen();
			return;
		}
		
		if(!this.hasPermission(Permissions.STORE_COINS))
		{
			Settings.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
			return;
		}
		
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getTrader().addStoredMoney(addValue);
		this.coinSlots.clear();
		
	}
	
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.side.isServer() && event.phase == TickEvent.Phase.START && this.getTrader() != null)
		{
			this.HandleBatterySlot();
		}
	}
	
	private void HandleBatterySlot()
	{
		if(!this.batteryInventory.getStackInSlot(0).isEmpty() && this.batteryInventory.getStackInSlot(1).isEmpty())
		{
			//Try to fill the energy storage with the battery, or vice-versa
			ItemStack batteryStack = this.batteryInventory.getStackInSlot(0);
			ItemStack batteryOutput = this.getTrader().getEnergyHandler().batteryInteraction(batteryStack);
			if(batteryStack.getCount() > 1)
				batteryStack.shrink(1);
			else
				batteryStack = ItemStack.EMPTY;
			this.batteryInventory.setInventorySlotContents(0, batteryStack);
			this.batteryInventory.setInventorySlotContents(1, batteryOutput);
		}
	}
	
	@Override
	public void CollectCoinStorage()
	{
		if(this.getTrader().getStoredMoney().getRawValue() <= 0)
			return;
		
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		
		if(this.getTrader().getCoreSettings().hasBankAccount())
			return;
		
		//Get the coin count from the trader
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getTrader().getStoredMoney());
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); ++i)
			{
				ItemStack extraCoins = WalletItem.PickupCoin(wallet, coinList.get(i));
				if(!extraCoins.isEmpty())
					spareCoins.add(extraCoins);
			}
			coinList = spareCoins;
		}
		IInventory inventory = InventoryUtil.buildInventory(coinList);
		this.clearContainer(this.player, this.player.world, inventory);
		
		//Clear the coin storage
		this.getTrader().clearStoredMoney();
		
	}
	
	//Menu Combination Functions/Types
	public static class EnergyTraderStorageContainerUniversal extends EnergyTraderStorageContainer {

		public EnergyTraderStorageContainerUniversal(int windowID, PlayerInventory inventory, UUID traderID) {
			super(ModContainers.ENERGY_TRADER_STORAGE_UNIVERSAL, windowID, inventory, () -> {
				UniversalTraderData data = null;
				if(inventory.player.world.isRemote)
					data = ClientTradingOffice.getData(traderID);
				else
					data = TradingOffice.getData(traderID);
				if(data instanceof IEnergyTrader)
					return (IEnergyTrader)data;
				return null;
			});
		}
		
		@Override
		public boolean isUniversal() { return true; }

	}
	
	public boolean isUniversal() { return false; }
	
	private class SafeUpgradeSlotSupplier implements Supplier<IInventory>
	{
		private final Supplier<IEnergyTrader> traderSource;
		private final IEnergyTrader getTrader() { return this.traderSource.get(); }
		private final int upgradeSlotCount;
		SafeUpgradeSlotSupplier(Supplier<IEnergyTrader> traderSource) {
			this.traderSource = traderSource;
			this.upgradeSlotCount = this.getTrader().getUpgradeInventory().getSizeInventory();
		}
		@Override
		public IInventory get() {
			IEnergyTrader trader = this.getTrader();
			if(trader != null)
				return trader.getUpgradeInventory();
			return new Inventory(this.upgradeSlotCount);
		}
	}
	
}
