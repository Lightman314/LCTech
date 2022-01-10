package io.github.lightman314.lctech.container;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.container.slots.UpgradeInputSlot;
import io.github.lightman314.lctech.core.ModContainers;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditOpen;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
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

public class FluidTraderStorageContainer extends Container implements ITraderStorageContainer{

	public final PlayerEntity player;
	public final FluidTraderTileEntity tileEntity;
	
	final IInventory coinSlots;
	
	public FluidTraderStorageContainer(int windowId, PlayerInventory inventory, FluidTraderTileEntity tileEntity) {
		super(ModContainers.FLUID_TRADER_STORAGE, windowId);
		
		this.player = inventory.player;
		this.tileEntity = tileEntity;
		this.tileEntity.userOpen(this.player);
		
		//No Storage slots, it's all handled by the buttons
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.tileEntity) + 32;
		
		//Upgrade slots
		for(int i = 0; i < this.tileEntity.getUpgradeInventory().getSizeInventory(); i++)
		{
			this.addSlot(new UpgradeInputSlot(this.tileEntity.getUpgradeInventory(), i, inventoryOffset - 24, getStorageBottom() + 6 + i * 18, this.tileEntity, this::OnUpgradeSlotChanged));
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
			
			//Move items from the coin/upgrade slots into the players inventory
			if(index < this.coinSlots.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack, this.tileEntity.getUpgradeInventory().getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				if(!this.mergeItemStack(slotStack, 0, this.tileEntity.getUpgradeInventory().getSizeInventory() + this.coinSlots.getSizeInventory(), false))
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
		return this.hasPermission(Permissions.OPEN_STORAGE);
	}
	
	@Override
	public void onContainerClosed(PlayerEntity player)
	{
		this.clearContainer(player, player.world, this.coinSlots);
		
		super.onContainerClosed(player);
		
		this.tileEntity.userClose(this.player);
		
	}
	
	public boolean hasPermission(String permission)
	{
		return this.tileEntity.hasPermission(this.player, permission);
	}
	
	public int getPermissionLevel(String permission)
	{
		return this.tileEntity.getPermissionLevel(this.player, permission);
	}
	
	private void OnUpgradeSlotChanged()
	{
		this.tileEntity.reapplyUpgrades();
		this.tileEntity.markTradesDirty();
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
		
		if(!this.hasPermission(Permissions.STORE_COINS))
		{
			Settings.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
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
		
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
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

}