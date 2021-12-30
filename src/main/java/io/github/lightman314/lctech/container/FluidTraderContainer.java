package io.github.lightman314.lctech.container;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.interfaces.IFluidTradeButtonContainer;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.container.slots.FluidInputSlot;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class FluidTraderContainer extends AbstractContainerMenu implements ITraderMenu, IFluidTradeButtonContainer{

	public final Player player;
	public final FluidTraderTileEntity tileEntity;
	
	Container bucketInventory = new SimpleContainer(1);
	Container coinSlots = new SimpleContainer(5);
	
	public FluidTraderContainer(int windowId, Inventory inventory, FluidTraderTileEntity tileEntity)
	{
		this(ModContainers.FLUID_TRADER, windowId, inventory, tileEntity);
	}
	
	protected FluidTraderContainer(MenuType<?> type, int windowId, Inventory inventory, FluidTraderTileEntity tileEntity)
	{
		
		super(type, windowId);
		
		this.player = inventory.player;
		this.tileEntity = tileEntity;
		
		this.tileEntity.userOpen(this.player);
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.tileEntity);
		
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
	
	@Override
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
		return tileEntity.getTradeCount();
	}
	
	protected int getTradeButtonBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.tileEntity);
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
		
		this.tileEntity.userClose(player);
	}

	public boolean isOwner()
	{
		return this.tileEntity.isOwner(this.player);
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
		
		if(tileEntity.getStoredMoney().getRawValue() <= 0)
			return;
		
		//Get the coin count from the tile entity
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.tileEntity.getStoredMoney());
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
		tileEntity.clearStoredMoney();
		
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
		
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
		
		FluidTradeData trade = tileEntity.getTrade(tradeIndex);
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
		if(!PermissionToTrade(tradeIndex, null))
			return;
		
		//Get the cost of the trade
		CoinValue price = this.TradeCostEvent(trade).getCostResult();
		
		//Abort if not enough fluid in the tank
		if(!trade.hasStock(this.tileEntity, price) && !this.tileEntity.isCreative())
		{
			LCTech.LOGGER.debug("Not enough fluid to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
			return;
		}
		//Abort if the tank doesn't have enough space for the purchased fluid.
		if(trade.isPurchase() && !(trade.hasSpace() || this.tileEntity.isCreative()))
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
			if(!this.tileEntity.isCreative())
			{
				//Add the stored money to the trader
				this.tileEntity.addStoredMoney(price);
			}
				
		}
		else if(trade.isPurchase())
		{
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			
			if(!this.tileEntity.isCreative())
			{
				//Remove the stored money to the trader
				this.tileEntity.removeStoredMoney(price);
			}
			
		}
		
		//Log the successful trade
		this.tileEntity.getLogger().AddLog(player, trade, price, this.tileEntity.isCreative());
		this.tileEntity.markLoggerDirty();
		
		//Transfer Fluids
		ItemStack newBucket = trade.transferFluids(this.bucketInventory.getItem(0), this.tileEntity.isCreative());
		this.bucketInventory.setItem(0, newBucket);
		this.tileEntity.markTradesDirty();
		
		
	}

	@Override
	public TradeEvent.TradeCostEvent TradeCostEvent(FluidTradeData trade) {
		TradeEvent.TradeCostEvent event = new TradeEvent.TradeCostEvent(this.player, trade, () -> this.tileEntity);
		this.tileEntity.tradeCost(event);
		trade.tradeCost(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	@Override
	public boolean PermissionToTrade(int tradeIndex, List<Component> denialOutput) {
		FluidTradeData trade = this.tileEntity.getTrade(tradeIndex);
		PreTradeEvent event = new PreTradeEvent(this.player, trade, () -> this.tileEntity);
		this.tileEntity.beforeTrade(event);
		trade.beforeTrade(event);
		MinecraftForge.EVENT_BUS.post(event);
		if(denialOutput != null)
			event.getDenialReasons().forEach(reason -> denialOutput.add(reason));
		return !event.isCanceled();
	}
	
}
