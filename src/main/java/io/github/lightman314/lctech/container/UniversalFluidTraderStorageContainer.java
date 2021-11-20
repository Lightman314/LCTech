package io.github.lightman314.lctech.container;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.container.slots.UpgradeInputSlot;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditOpen;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
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
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class UniversalFluidTraderStorageContainer extends UniversalContainer implements ITraderStorageContainer, ICreativeTraderContainer{

	public final PlayerEntity player;
	
	final IInventory coinSlots;
	
	public UniversalFluidTraderData getData()
	{
		UniversalTraderData data = this.getRawData();
		if(data instanceof UniversalFluidTraderData)
			return (UniversalFluidTraderData)data;
		return null;
	}
	
	public UniversalFluidTraderStorageContainer(int windowId, PlayerInventory inventory, UUID traderID) {
		super(ModContainers.UNIVERSAL_FLUID_TRADER_STORAGE, windowId, traderID, inventory.player);
		
		this.player = inventory.player;
		
		//No Storage slots, it's all handled by the buttons
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.getData()) + 32;
		
		//Upgrade slots
		for(int i = 0; i < this.getData().getUpgradeInventory().getSizeInventory(); i++)
		{
			this.addSlot(new UpgradeInputSlot(this.getData().getUpgradeInventory(), i, inventoryOffset - 24, getStorageBottom() + 6 + i * 18, this.getData(), this::OnUpgradeSlotChanged));
		}
		
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
		if(this.player.world.isRemote) //Flag the fluid handler as client to block marking the data as dirty.
			this.getData().getFluidHandler().flagAsClient();
		
		this.getData().getFluidHandler().OnPlayerInteraction(this.player, tradeIndex);
	}
	
	public int getStorageBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.getData());
	}
	
	public void tick()
	{
		if(this.getData() == null)
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
			
			//Move items from the coin/upgrade slots into the players inventory
			if(index < this.coinSlots.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack, this.getData().getUpgradeInventory().getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				if(!this.mergeItemStack(slotStack, 0, this.getData().getUpgradeInventory().getSizeInventory() + this.coinSlots.getSizeInventory(), false))
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
		
	}
	
	public boolean isOwner()
	{
		return this.getData().isOwner(this.player);
	}
	
	public boolean hasPermissions()
	{
		return this.getData().hasPermissions(this.player);
	}
	
	private void OnUpgradeSlotChanged()
	{
		this.getData().reapplyUpgrades();
		if(this.isServer())
			this.getData().markTradesDirty();
	}
	
	public void openFluidEditScreenForTrade(int tradeIndex)
	{
		if(this.player.world.isRemote)
		{
			LCTechPacketHandler.instance.sendToServer(new MessageFluidEditOpen(tradeIndex));
		}
		else
		{
			this.getData().openFluidEditMenu(this.player, tradeIndex);
		}
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void AddCoins()
	{
		if(this.getData() == null)
		{
			this.player.closeScreen();
			return;
		}
		
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getData().addStoredMoney(addValue);
		this.coinSlots.clear();
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.getData() == null)
		{
			this.player.closeScreen();
			return;
		}
		
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(getData().getStoredMoney());
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
		this.getData().clearStoredMoney();
		
	}
	
	public void ToggleCreative()
	{
		if(this.getData() == null)
		{
			this.player.closeScreen();
			return;
		}
		
		this.getData().toggleCreative();
	}
	
	public void AddTrade()
	{
		this.getData().addTrade();
	}
	
	public void RemoveTrade()
	{
		this.getData().removeTrade();
	}

	@Override
	protected void onForceReopen() {
		this.getData().openStorageMenu(this.player);
	}

}