package io.github.lightman314.lctech.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.menu.slots.UpgradeInputSlot;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EnergyTraderStorageMenu extends AbstractContainerMenu implements ITraderStorageMenu {
	
	private final Supplier<IEnergyTrader> traderSource;
	public Supplier<IEnergyTrader> getTraderSource() { return this.traderSource; }
	public IEnergyTrader getTrader() { return this.traderSource == null ? null : this.traderSource.get(); }
	
	public final Player player;
	
	Container batteryInventory = new SimpleContainer(2);
	Container upgradeSlots;
	Container coinSlots = new SimpleContainer(5);
	
	//World Constructor
	public EnergyTraderStorageMenu(int windowID, Inventory inventory, BlockPos traderPosition)
	{
		this(ModMenus.ENERGY_TRADER_STORAGE, windowID, inventory, traderPosition);
	}
	
	protected EnergyTraderStorageMenu(MenuType<?> type, int windowID, Inventory inventory, BlockPos traderPosition)
	{
		this(type, windowID, inventory, () -> {
			BlockEntity be = inventory.player.level.getBlockEntity(traderPosition);
			if(be instanceof IEnergyTrader)
				return (IEnergyTrader)be;
			return null;
		});
	}
	
	protected EnergyTraderStorageMenu(MenuType<?> type, int windowID, Inventory inventory, @Nonnull Supplier<IEnergyTrader> traderSource) {
		super(type, windowID);
		
		this.player = inventory.player;
		this.traderSource = traderSource;
		
		this.upgradeSlots = new SuppliedContainer(new SafeUpgradeSlotSupplier(this.traderSource));
		
		this.getTrader().userOpen(this.player);
		
		//Upgrade Slots
		for(int i = 0; i < this.upgradeSlots.getContainerSize(); ++i)
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
	public ItemStack quickMoveStack(Player player, int index)
	{
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.coinSlots.getContainerSize() + this.batteryInventory.getContainerSize() + this.upgradeSlots.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.coinSlots.getContainerSize() + this.batteryInventory.getContainerSize() + this.upgradeSlots.getContainerSize(),  this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size())
			{
				//Merge coins into coin -> upgrade slots
				if(!this.moveItemStackTo(slotStack, 0, this.coinSlots.getContainerSize() + this.batteryInventory.getContainerSize() + this.upgradeSlots.getContainerSize(),false))
				{
					return ItemStack.EMPTY;
				}
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
	}
	
	@Override
	public boolean stillValid(Player player) { return this.getTrader() != null && this.hasPermission(Permissions.OPEN_STORAGE); }
	
	@Override
	public void removed(Player player)
	{
		super.removed(player);
		this.clearContainer(player, this.coinSlots);
		this.clearContainer(player, this.batteryInventory);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(this.player);
		
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
			this.player.closeContainer();
			return;
		}
		
		if(!this.hasPermission(Permissions.STORE_COINS))
		{
			Settings.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
			return;
		}
		
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getTrader().addStoredMoney(addValue);
		this.coinSlots.clearContent();
		
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
		if(!this.batteryInventory.getItem(0).isEmpty() && this.batteryInventory.getItem(1).isEmpty())
		{
			//Try to fill the energy storage with the battery, or vice-versa
			ItemStack batteryStack = this.batteryInventory.getItem(0);
			ItemStack batteryOutput = this.getTrader().getEnergyHandler().batteryInteraction(batteryStack);
			if(batteryStack.getCount() > 1)
				batteryStack.shrink(1);
			else
				batteryStack = ItemStack.EMPTY;
			this.batteryInventory.setItem(0, batteryStack);
			this.batteryInventory.setItem(1, batteryOutput);
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
		Container inventory = InventoryUtil.buildInventory(coinList);
		this.clearContainer(this.player, inventory);
		
		//Clear the coin storage
		this.getTrader().clearStoredMoney();
		
	}
	
	//Menu Combination Functions/Types
	public static class EnergyTraderStorageMenuUniversal extends EnergyTraderStorageMenu {

		public EnergyTraderStorageMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.ENERGY_TRADER_STORAGE_UNIVERSAL, windowID, inventory, () -> {
				UniversalTraderData data = null;
				if(inventory.player.level.isClientSide)
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
	
	private class SafeUpgradeSlotSupplier implements Supplier<Container>
	{
		private final Supplier<IEnergyTrader> traderSource;
		private final IEnergyTrader getTrader() { return this.traderSource.get(); }
		private final int upgradeSlotCount;
		SafeUpgradeSlotSupplier(Supplier<IEnergyTrader> traderSource) {
			this.traderSource = traderSource;
			this.upgradeSlotCount = this.getTrader().getUpgradeInventory().getContainerSize();
		}
		@Override
		public Container get() {
			IEnergyTrader trader = this.getTrader();
			if(trader != null)
				return trader.getUpgradeInventory();
			return new SimpleContainer(this.upgradeSlotCount);
		}
	}
	
}
