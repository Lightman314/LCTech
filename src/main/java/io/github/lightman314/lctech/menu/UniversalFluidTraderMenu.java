package io.github.lightman314.lctech.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.menu.slots.FluidInputSlot;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.UniversalMenu;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UniversalFluidTraderMenu extends UniversalMenu implements ITraderMenu{

	public final Player player;
	
	Container bucketInventory = new SimpleContainer(1);
	Container coinSlots = new SimpleContainer(5);
	
	public UniversalFluidTraderData getData()
	{
		UniversalTraderData data = this.getRawData();
		if(data instanceof UniversalFluidTraderData)
			return (UniversalFluidTraderData)data;
		return null;
	}
	
	public UniversalFluidTraderMenu(int windowId, Inventory inventory, UUID traderID)
	{
		this(ModMenus.UNIVERSAL_FLUID_TRADER, windowId, inventory, traderID);
	}
	
	protected UniversalFluidTraderMenu(MenuType<?> type, int windowId, Inventory inventory, UUID traderID)
	{
		
		super(type, windowId, traderID, inventory.player);
		
		this.player = inventory.player;
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.getData());
		
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
	
	public int getTradeCount()
	{
		return this.getData().getTradeCount();
	}
	
	protected int getTradeButtonBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.getData());
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
	public boolean stillValid(Player playerIn) {
		return true;
	}
	
	@Override
	public void removed(Player player)
	{
		super.removed(player);
		this.clearContainer(player, this.coinSlots);
		this.clearContainer(player, this.bucketInventory);
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
	
	public void tick()
	{
		
	}
	
	@Override
	public void CollectCoinStorage() {
		
		if(getData().getStoredMoney().getRawValue() <= 0)
			return;
		
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		
		if(this.getData().getCoreSettings().hasBankAccount())
			return;
		
		//Get the coin count from the tile entity
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getData().getStoredMoney());
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
		getData().clearStoredMoney();
		
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
		
		if(this.getData() == null)
		{
			this.player.closeContainer();
			return;
		}
		
		FluidTradeData trade = getData().getTrade(tradeIndex);
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
		if(this.getData().runPreTradeEvent(this.player, tradeIndex).isCanceled())
			return;
		
		//Get the cost of the trade
		CoinValue price = this.getData().runTradeCostEvent(this.player, tradeIndex).getCostResult();
		
		//Abort if not enough fluid in the tank
		if(!trade.hasStock(this.getData(), PlayerReference.of(this.player)) && !this.getData().getCoreSettings().isCreative())
		{
			LCTech.LOGGER.debug("Not enough fluid to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
			return;
		}
		//Abort if the tank doesn't have enough space for the purchased fluid.
		if(trade.isPurchase() && !(trade.hasSpace() || this.getData().getCoreSettings().isCreative()))
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
			if(!this.getData().getCoreSettings().isCreative())
			{
				//Add the stored money to the trader
				this.getData().addStoredMoney(price);
			}
				
		}
		else if(trade.isPurchase())
		{
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			
			if(!this.getData().getCoreSettings().isCreative())
			{
				//Remove the stored money to the trader
				this.getData().removeStoredMoney(price);
			}
			
		}
		
		//Transfer Fluids
		ItemStack newBucket = trade.transferFluids(this.bucketInventory.getItem(0), this.getData().getCoreSettings().isCreative());
		this.bucketInventory.setItem(0, newBucket);
		this.getData().markTradesDirty();
		
		//Log the successful trade
		this.getData().getLogger().AddLog(player, trade, price, this.getData().getCoreSettings().isCreative());
		this.getData().markLoggerDirty();
		
		//Run the trade success event
		this.getData().runPostTradeEvent(this.player, tradeIndex, price);
		
	}

	@Override
	protected void onForceReopen() {
		this.getData().openTradeMenu(this.player);
	}
	
}
