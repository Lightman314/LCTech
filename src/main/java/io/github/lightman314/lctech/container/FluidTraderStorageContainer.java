package io.github.lightman314.lctech.container;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditOpen;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ICreativeTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class FluidTraderStorageContainer extends Container implements ITraderStorageContainer, ICreativeTraderContainer{

	public final PlayerEntity player;
	public final FluidTraderTileEntity tileEntity;
	
	final IInventory coinSlots;
	
	public FluidTraderStorageContainer(int windowId, PlayerInventory inventory, FluidTraderTileEntity tileEntity) {
		super(ModContainers.FLUID_TRADER_STORAGE, windowId);
		
		this.player = inventory.player;
		this.tileEntity = tileEntity;
		this.tileEntity.userOpen(this.player);
		
		//No Storage slots, it's all handled by the buttons
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.tileEntity);
		
		//Coin slots
		this.coinSlots = new Inventory(5);
		for(int i = 0; i < 5; i++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, i, inventoryOffset + 176 + 8, getStorageBottom() + 6 + i * 18));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0 ; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, inventoryOffset + 8 + x * 18, getStorageBottom() + 18 + y * 18));
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, inventoryOffset + 8 + x * 18, getStorageBottom() + 18 + 58));
		}
		
	}
	
	/**
	 * Interacts the players currently held item with the tank of the given trade index
	 * @param tradeIndex
	 */
	public void PlayerTankInteraction(int tradeIndex)
	{
		this.tileEntity.getFluidHandler().OnPlayerInteraction(this.player, tradeIndex);
	}
	
	public int getStorageBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.tileEntity);
	}
	
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
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
			
			//Move items from the coin slots into the players inventory
			if(index < this.coinSlots.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				if(!this.mergeItemStack(slotStack, 0, this.coinSlots.getSizeInventory(), false))
				{
					return ItemStack.EMPTY;
				}
			}
			if(slotStack.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();
		}
		
		return clickedStack;
		
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity player)
	{
		this.clearContainer(player, player.world, this.coinSlots);
		
		super.onContainerClosed(player);
		
		this.tileEntity.userClose(this.player);
		
	}
	
	public boolean isOwner()
	{
		return this.tileEntity.isOwner(this.player);
	}
	
	public boolean hasPermissions()
	{
		return this.tileEntity.hasPermissions(this.player);
	}
	
	public void openFluidEditScreenForTrade(int tradeIndex)
	{
		if(this.player.world.isRemote)
		{
			LCTechPacketHandler.instance.sendToServer(new MessageFluidEditOpen(tradeIndex));
		}
		else
		{
			this.tileEntity.openFluidEditMenu(this.player, tradeIndex);
		}
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void AddCoins()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.tileEntity.addStoredMoney(addValue);
		this.coinSlots.clear();
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(tileEntity.getStoredMoney());
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			List<ItemStack> spareCoins = Lists.newArrayList();
			for(int i = 0; i < coinList.size(); i++)
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
		this.tileEntity.clearStoredMoney();
		
	}
	
	public void ToggleCreative()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		
		this.tileEntity.toggleCreative();
	}
	
	public void AddTrade()
	{
		this.tileEntity.addTrade();
	}
	
	public void RemoveTrade()
	{
		this.tileEntity.removeTrade();
	}

}