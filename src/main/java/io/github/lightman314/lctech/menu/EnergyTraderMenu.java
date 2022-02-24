package io.github.lightman314.lctech.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.menu.slots.BatteryInputSlot;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderCashRegisterMenu;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
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

public class EnergyTraderMenu extends AbstractContainerMenu implements ITraderMenu{
	
	private final Supplier<IEnergyTrader> traderSource;
	public IEnergyTrader getTrader() { return this.traderSource == null ? null : this.traderSource.get(); }
	
	public final Player player;
	
	Container batteryInventory = new SimpleContainer(1);
	Container coinSlots = new SimpleContainer(5);
	
	//World Constructor
	public EnergyTraderMenu(int windowID, Inventory inventory, BlockPos traderPosition)
	{
		this(ModMenus.ENERGY_TRADER, windowID, inventory, traderPosition);
	}
	
	protected EnergyTraderMenu(MenuType<?> type, int windowID, Inventory inventory, BlockPos traderPosition)
	{
		this(type, windowID, inventory, () -> {
			BlockEntity be = inventory.player.level.getBlockEntity(traderPosition);
			if(be instanceof IEnergyTrader)
				return (IEnergyTrader)be;
			return null;
		});
	}
	
	protected EnergyTraderMenu(MenuType<?> type, int windowID, Inventory inventory, @Nonnull Supplier<IEnergyTrader> traderSource) {
		super(type, windowID);
		
		this.player = inventory.player;
		this.traderSource = traderSource;
		
		this.getTrader().userOpen(this.player);
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getContainerSize(); ++x)
		{
			this.addSlot(new CoinSlot(coinSlots, x, 8 + (x + 4) * 18, 89));
		}
		
		//Battery Slot
		this.addSlot(new BatteryInputSlot(batteryInventory, 0, 8, 89));
		
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
		
	}
	
	public ItemStack getBatteryStack() { return this.batteryInventory.getItem(0); }
	
	@Override
	public ItemStack quickMoveStack(Player player, int index)
	{
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.coinSlots.getContainerSize() + this.batteryInventory.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.coinSlots.getContainerSize() + this.batteryInventory.getContainerSize(),  this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					//Merge coins into coin slots
					if(!this.moveItemStackTo(slotStack,  0,  this.coinSlots.getContainerSize(),false))
					{
						return ItemStack.EMPTY;
					}
				}
				else
				{
					//Merge non-coincs into the battery slot
					if(!this.moveItemStackTo(slotStack, this.coinSlots.getContainerSize(), this.coinSlots.getContainerSize() + this.batteryInventory.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
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
	public boolean stillValid(Player player) { return this.getTrader() != null; }
	
	@Override
	public void removed(Player player)
	{
		super.removed(player);
		this.clearContainer(player, this.coinSlots);
		this.clearContainer(player, this.batteryInventory);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(player);
		
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
	
	public long GetCoinValue()
	{
		long value = 0;
		value += MoneyUtil.getValue(coinSlots);
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			value += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
		}
		return value;
	}
	
	@Override
	public void CollectCoinStorage()
	{
		if(this.getTrader().getInternalStoredMoney().getRawValue() <= 0)
			return;
		
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		
		if(this.getTrader().getCoreSettings().hasBankAccount())
			return;
		
		//Get the coin count from the trader
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getTrader().getInternalStoredMoney());
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
		for(int i = 0; i < coinList.size(); ++i)
		{
			if(!InventoryUtil.PutItemStack(coinSlots,  coinList.get(i)))
			{
				Container inventory = new SimpleContainer(1);
				inventory.setItem(0, coinList.get(i));
				this.clearContainer(this.player, inventory);
			}
		}
		//Clear the coin storage
		this.getTrader().clearStoredMoney();
		
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
			return;
		}
		
		EnergyTradeData trade = this.getTrader().getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
			return;
		
		//Abort if the trade is not valid
		if(!trade.isValid())
			return;
		
		if(this.getTrader().runPreTradeEvent(this.player, tradeIndex).isCanceled())
			return;
		
		//Get the cost of the trade
		CoinValue price = this.getTrader().runTradeCostEvent(this.player, tradeIndex).getCostResult();
		
		//Abort if not enough energy in the tank
		if(!trade.hasStock(this.getTrader(), this.player) && !this.getTrader().getCoreSettings().isCreative())
			return;
		
		//Abort if the energy storage doesn't have enough space for the purchased energy
		if(trade.isPurchase() && (!trade.hasSpace(this.getTrader()) || this.getTrader().getCoreSettings().isCreative()))
			return;
		
		//Abort if the energy cannot be transferred properly
		if(!trade.canTransferEnergy(this.getTrader(), this.getBatteryStack()))
			return;
		
		if(trade.isSale())
		{
			if(!MoneyUtil.ProcessPayment(this.coinSlots, this.player, price))
				return;
			//Add the stored money to the trader
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				this.getTrader().addStoredMoney(price);
			}
		}
		else if(trade.isPurchase())
		{
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			//Remove the stored money from the trader
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				this.getTrader().removeStoredMoney(price);
			}
		}
		
		//Transfer Energy
		ItemStack newBattery = trade.transferEnergy(this.getTrader(), this.getBatteryStack());
		this.batteryInventory.setItem(0, newBattery);
		this.getTrader().markEnergyStorageDirty();
		
		//Log the successful trade
		this.getTrader().getLogger().AddLog(this.player, trade, price, this.getTrader().getCoreSettings().isCreative());
		this.getTrader().markLoggerDirty();
		
		//Post the trade success event
		this.getTrader().runPostTradeEvent(this.player, tradeIndex, price);
		
	}
	
	//Menu Combination Functions/Types
	public static class EnergyTraderMenuUniversal extends EnergyTraderMenu {

		public EnergyTraderMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.ENERGY_TRADER_UNIVERSAL, windowID, inventory, () -> {
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
	
	public static class EnergyTraderMenuCR extends EnergyTraderMenu implements ITraderCashRegisterMenu{

		private CashRegisterBlockEntity cashRegister;
		
		public EnergyTraderMenuCR(int windowID, Inventory inventory, BlockPos traderPos, CashRegisterBlockEntity cashRegister) {
			super(ModMenus.ENERGY_TRADER_CR, windowID, inventory, traderPos);
			this.cashRegister = cashRegister;
		}
		@Override
		public boolean isCashRegister() { return true; }
		
		@Override
		public CashRegisterBlockEntity getCashRegister() { return this.cashRegister; }
		
		private TraderBlockEntity getTraderBE()
		{
			IEnergyTrader trader = super.getTrader();
			if(trader instanceof TraderBlockEntity)
				return (TraderBlockEntity) trader;
			return null;
		}
		
		@Override
		public int getThisCRIndex() { return this.cashRegister.getTraderIndex(this.getTraderBE()); }
		
		@Override
		public int getTotalCRSize() { return this.cashRegister.getPairedTraderSize(); }
		
		@Override
		public void OpenNextContainer(int direction) {
			int thisIndex = this.cashRegister.getTraderIndex((TraderBlockEntity)this.getTrader());
			this.cashRegister.OpenContainer(thisIndex, thisIndex + direction, direction, this.player);
		}
		
		@Override
		public void OpenContainerIndex(int index) {
			int previousIndex = index-1;
			if(previousIndex < 0)
				previousIndex = this.cashRegister.getPairedTraderSize() - 1;
			this.cashRegister.OpenContainer(previousIndex, index, 1, this.player);
		}

	}
	
	public boolean isCashRegister() { return false; }
	
	public CashRegisterBlockEntity getCashRegister() { return null; }

	public int getThisCRIndex() { return 0; }
	
	public int getTotalCRSize() { return 0; }
	
}
