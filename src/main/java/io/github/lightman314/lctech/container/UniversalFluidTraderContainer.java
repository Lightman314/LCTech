package io.github.lightman314.lctech.container;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.interfaces.IFluidTradeButtonContainer;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.container.slots.FluidInputSlot;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.extendedinventory.MessageUpdateWallet;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.PacketDistributor;

public class UniversalFluidTraderContainer extends UniversalContainer implements ITraderContainer, IFluidTradeButtonContainer{

	public final PlayerEntity player;
	
	IInventory bucketInventory = new Inventory(1);
	IInventory coinSlots = new Inventory(5);
	
	public UniversalFluidTraderData getData()
	{
		UniversalTraderData data = this.getRawData();
		if(data instanceof UniversalFluidTraderData)
			return (UniversalFluidTraderData)data;
		return null;
	}
	
	public UniversalFluidTraderContainer(int windowId, PlayerInventory inventory, UUID traderID)
	{
		this(ModContainers.UNIVERSAL_FLUID_TRADER, windowId, inventory, traderID);
	}
	
	protected UniversalFluidTraderContainer(ContainerType<?> type, int windowId, PlayerInventory inventory, UUID traderID)
	{
		
		super(type, windowId, traderID, inventory.player);
		
		this.player = inventory.player;
		
		//Coin Slots
		for(int x = 0; x < coinSlots.getSizeInventory(); x++)
		{
			this.addSlot(new CoinSlot(coinSlots, x, 8 + (x + 4) * 18, getCoinSlotHeight()));
		}
		
		//Bucket Slot
		this.addSlot(new FluidInputSlot(bucketInventory, 0, 8, getCoinSlotHeight()));
		
		//Player Inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, getPlayerInventoryStartHeight() + y * 18));
			}
		}
		
		//Player Hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x , 8 + x * 18, getPlayerInventoryStartHeight() + 58));
		}
		
	}
	
	@Override
	public ItemStack getBucketItem() {
		return this.bucketInventory.getStackInSlot(0);
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			clickedStack = slotStack.copy();
			if(index < this.coinSlots.getSizeInventory() + this.bucketInventory.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.coinSlots.getSizeInventory() + this.bucketInventory.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.inventorySlots.size())
			{
				if(MoneyUtil.isCoin(slotStack.getItem()))
				{
					//Merge coins into coin slots
					if(!this.mergeItemStack(slotStack, 0, this.coinSlots.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else
				{
					//Merge non-coins into the bucket slots
					if(!this.mergeItemStack(slotStack, this.coinSlots.getSizeInventory(), this.coinSlots.getSizeInventory() + this.bucketInventory.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
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
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity player)
	{
		super.onContainerClosed(player);
		this.clearContainer(player, player.world, this.coinSlots);
		this.clearContainer(player, player.world, this.bucketInventory);
	}

	public boolean isOwner()
	{
		return this.getData().isOwner(this.player);
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
			if(!LightmansCurrency.isCuriosLoaded())
				if(!LightmansCurrency.isCuriosLoaded())
					LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)this.player), new MessageUpdateWallet(player.getEntityId(), wallet));
		}
		for(int i = 0; i < coinList.size(); i++)
		{
			if(!InventoryUtil.PutItemStack(coinSlots, coinList.get(i)))
			{
				IInventory inventory = new Inventory(1);
				inventory.setInventorySlotContents(0, coinList.get(i));
				this.clearContainer(player, player.getEntityWorld(), inventory);
			}
		}
		//Clear the coin storage
		getData().clearStoredMoney();
		
	}
	
	public void ExecuteTrade(int tradeIndex)
	{
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
			LCTech.LOGGER.error("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return;
		}
		
		//Get the cost of the trade
		CoinValue price = this.TradeCostEvent(trade).getCostResult();
		
		//Abort if not enough fluid in the tank
		if(!trade.hasStock(this.getData(), price))
		{
			LCTech.LOGGER.debug("Not enough fluid to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
			return;
		}
		//Abort if the tank doesn't have enough space for the purchased fluid.
		if(trade.isPurchase() && !(trade.hasSpace() || this.getData().isCreative()))
		{
			LCTech.LOGGER.debug("Not enough space in the fluid tank to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
			return;
		}
		//Abort if the liquids cannot be transferred properly
		if(!trade.canTransferFluids(this.bucketInventory.getStackInSlot(0)))
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
			if(!this.getData().isCreative())
			{
				//Add the stored money to the trader
				this.getData().addStoredMoney(price);
			}
				
		}
		else if(trade.isPurchase())
		{
			//Put the payment in the purchasers wallet, coin slot, etc.
			MoneyUtil.ProcessChange(this.coinSlots, this.player, price);
			
			if(!this.getData().isCreative())
			{
				//Remove the stored money to the trader
				this.getData().removeStoredMoney(price);
			}
			
		}
		
		//Transfer Fluids
		ItemStack newBucket = trade.transferFluids(this.bucketInventory.getStackInSlot(0), this.getData().isCreative());
		this.bucketInventory.setInventorySlotContents(0, newBucket);
		this.getData().markTradesDirty();
		
		
	}

	@Override
	public TradeEvent.TradeCostEvent TradeCostEvent(FluidTradeData trade) {
		TradeEvent.TradeCostEvent event = new TradeEvent.TradeCostEvent(this.player, trade, this, () -> this.getData());
		this.getData().tradeCost(event);
		trade.tradeCost(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	@Override
	public boolean PermissionToTrade(int tradeIndex, List<ITextComponent> denialOutput) {
		FluidTradeData trade = this.getData().getTrade(tradeIndex);
		PreTradeEvent event = new PreTradeEvent(this.player, trade, this, () -> this.getData());
		this.getData().beforeTrade(event);
		trade.beforeTrade(event);
		MinecraftForge.EVENT_BUS.post(event);
		event.getDenialReasons().forEach(reason -> denialOutput.add(reason));
		return !event.isCanceled();
	}

	@Override
	protected void onForceReopen() {
		this.getData().openTradeMenu(this.player);
	}
	
}
