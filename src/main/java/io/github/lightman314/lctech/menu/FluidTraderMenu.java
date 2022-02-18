package io.github.lightman314.lctech.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.menu.slots.FluidInputSlot;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderCashRegisterMenu;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FluidTraderMenu extends AbstractContainerMenu implements ITraderMenu {

	public final Player player;
	private final Supplier<IFluidTrader> traderSource;
	public IFluidTrader getTrader() { return this.traderSource == null ? null : this.traderSource.get(); }
	
	Container bucketInventory = new SimpleContainer(1);
	Container coinSlots = new SimpleContainer(5);
	
	public FluidTraderMenu(int windowId, Inventory inventory, BlockPos traderPos)
	{
		this(ModMenus.FLUID_TRADER, windowId, inventory, traderPos);
	}
	
	protected FluidTraderMenu(MenuType<?> type, int windowId, Inventory inventory, BlockPos traderPos) {
		this(type, windowId, inventory, IFluidTrader.BlockEntitySource(inventory.player.level, traderPos));
	}
	
	protected FluidTraderMenu(MenuType<?> type, int windowId, Inventory inventory, Supplier<IFluidTrader> traderSource)
	{
		
		super(type, windowId);
		
		this.player = inventory.player;
		this.traderSource = traderSource;
		
		this.getTrader().userOpen(this.player);
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.getTrader());
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getContainerSize(); x++)
		{
			this.addSlot(new CoinSlot(coinSlots, x, 8 + (x + 4) * 18 + inventoryOffset, getCoinSlotHeight()));
		}
		
		//Bucket Slot
		this.addSlot(new FluidInputSlot(bucketInventory, 0, 8 + inventoryOffset, getCoinSlotHeight()));
		
		//Player Inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18 + inventoryOffset, getPlayerInventoryStartHeight() + y * 18));
			}
		}
		
		//Player Hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x , 8 + x * 18 + inventoryOffset, getPlayerInventoryStartHeight() + 58));
		}
		
	}
	
	public ItemStack getBucketItem() {
		return this.bucketInventory.getItem(0);
	}
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.coinSlots.getContainerSize() + this.bucketInventory.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.coinSlots.getContainerSize() + this.bucketInventory.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					//Merge coins into coin slots
					if(!this.moveItemStackTo(slotStack, 0, this.coinSlots.getContainerSize(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else
				{
					//Merge non-coins into the bucket slots
					if(!this.moveItemStackTo(slotStack, this.coinSlots.getContainerSize(), this.coinSlots.getContainerSize() + this.bucketInventory.getContainerSize(), false))
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
	
	protected int getTradeButtonBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.getTrader());
	}
	
	protected int getCoinSlotHeight()
	{
		return getTradeButtonBottom() + 8 + 11;
	}
	
	protected int getPlayerInventoryStartHeight()
	{
		return getCoinSlotHeight() + 32;
	}
	
	@Override
	public boolean stillValid(Player player) { return this.getTrader() != null; }
	
	@Override
	public void removed(Player player)
	{
		super.removed(player);
		this.clearContainer(player, this.coinSlots);
		this.clearContainer(player, this.bucketInventory);
		
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
	public void CollectCoinStorage() {
		
		if(this.getTrader() == null)
			return;
		
		if(this.getTrader().getInternalStoredMoney().getRawValue() <= 0)
			return;
		
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		
		if(this.getTrader().getCoreSettings().hasBankAccount())
			return;
		
		//Get the coin count from the tile entity
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getTrader().getInternalStoredMoney());
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); i++)
			{
				ItemStack extraCoins = WalletItem.PickupCoin(wallet, coinList.get(i));
				if(!extraCoins.isEmpty())
					spareCoins.add(extraCoins);
			}
			coinList = spareCoins;
		}
		for(int i = 0; i < coinList.size(); i++)
		{
			if(!InventoryUtil.PutItemStack(coinSlots, coinList.get(i)))
			{
				Container inventory = new SimpleContainer(1);
				inventory.setItem(0, coinList.get(i));
				this.clearContainer(player, inventory);
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
		
		FluidTradeData trade = this.getTrader().getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
		{
			LCTech.LOGGER.error("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			LCTech.LOGGER.warn("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return;
		}
		
		//Check if the player is allowed to do the trade
		if(this.getTrader().runPreTradeEvent(this.player, tradeIndex).isCanceled())
			return;
		
		//Get the cost of the trade
		CoinValue price = this.getTrader().runTradeCostEvent(this.player, tradeIndex).getCostResult();
		
		//Abort if not enough fluid in the tank
		if(!trade.hasStock(this.getTrader(), PlayerReference.of(this.player)) && !this.getTrader().getCoreSettings().isCreative())
		{
			LCTech.LOGGER.debug("Not enough fluid to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
			return;
		}
		//Abort if the tank doesn't have enough space for the purchased fluid.
		if(trade.isPurchase() && !(trade.hasSpace() || this.getTrader().getCoreSettings().isCreative()))
		{
			LCTech.LOGGER.debug("Not enough space in the fluid tank to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
			return;
		}
		//Abort if the liquids cannot be transferred properly
		if(!trade.canTransferFluids(this.bucketInventory.getItem(0)))
		{
			LCTech.LOGGER.debug("The fluids cannot be properly transfered for the trade at index " + tradeIndex + ". Cannot execute trade.");
			return;
		}
		
		if(trade.isSale())
		{
			//Process the trades payment
			if(!MoneyUtil.ProcessPayment(this.coinSlots, this.player, price))
			{
				LCTech.LOGGER.debug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade.");
				return;
			}
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				//Add the stored money to the trader
				this.getTrader().addStoredMoney(price);
			}
				
		}
		else if(trade.isPurchase())
		{
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			
			if(!this.getTrader().getCoreSettings().isCreative())
			{
				//Remove the stored money to the trader
				this.getTrader().removeStoredMoney(price);
			}
			
		}
		
		//Transfer Fluids
		ItemStack newBucket = trade.transferFluids(this.bucketInventory.getItem(0), this.getTrader().getCoreSettings().isCreative());
		this.bucketInventory.setItem(0, newBucket);
		this.getTrader().markTradesDirty();
		
		//Log the successful trade
		this.getTrader().getLogger().AddLog(player, trade, price, this.getTrader().getCoreSettings().isCreative());
		this.getTrader().markLoggerDirty();
		
		//Post the trade success event
		this.getTrader().runPostTradeEvent(this.player, tradeIndex, price);
		
	}
	
	//Menu Variants
	public static class FluidTraderMenuUniversal extends FluidTraderMenu
	{
		public FluidTraderMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.UNIVERSAL_FLUID_TRADER, windowID, inventory, IFluidTrader.UniversalSource(inventory.player.level, traderID));
		}
		
		@Override
		public boolean isUniversal() { return true; }
	}
	
	public boolean isUniversal() { return false; }
	
	public static class FluidTraderMenuCR extends FluidTraderMenu implements ITraderCashRegisterMenu
	{
		
		CashRegisterBlockEntity cashRegister;
		
		public FluidTraderMenuCR(int windowID, Inventory inventory, BlockPos traderPos, CashRegisterBlockEntity cashRegister) {
			super(ModMenus.FLUID_TRADER_CR, windowID, inventory, traderPos);
			this.cashRegister = cashRegister;
		}
		
		@Override
		public boolean isCashRegister() { return true; }
		
		@Override
		public CashRegisterBlockEntity getCashRegister() { return this.cashRegister; }
		
		private TraderBlockEntity getTraderBE()
		{
			IFluidTrader trader = super.getTrader();
			if(trader instanceof TraderBlockEntity)
				return (TraderBlockEntity)trader;
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
