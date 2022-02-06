package io.github.lightman314.lctech.menu;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.blockentities.FluidTraderBlockEntity;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.menu.slots.UpgradeInputSlot;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditOpen;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FluidTraderStorageMenu extends AbstractContainerMenu implements ITraderStorageMenu{

	public final Player player;
	public final FluidTraderBlockEntity tileEntity;
	
	final Container coinSlots;
	
	public FluidTraderStorageMenu(int windowId, Inventory inventory, FluidTraderBlockEntity tileEntity) {
		super(ModMenus.FLUID_TRADER_STORAGE, windowId);
		
		this.player = inventory.player;
		this.tileEntity = tileEntity;
		this.tileEntity.userOpen(this.player);
		
		//No Storage slots, it's all handled by the buttons
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.tileEntity) + 32;
		
		//Upgrade slots
		for(int i = 0; i < this.tileEntity.getUpgradeInventory().getContainerSize(); i++)
		{
			this.addSlot(new UpgradeInputSlot(this.tileEntity.getUpgradeInventory(), i, inventoryOffset - 24, getStorageBottom() + 6 + i * 18, this.tileEntity, this::OnUpgradeSlotChanged));
		}
		
		//Coin slots
		this.coinSlots = new SimpleContainer(5);
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
		this.tileEntity.getFluidHandler().OnPlayerInteraction(this, this.player, tradeIndex);
	}
	
	public int getStorageBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.tileEntity);
	}
	
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
			return;
		}
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
			
			//Move items from the coin/upgrade slots into the players inventory
			if(index < this.coinSlots.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack, this.tileEntity.getUpgradeInventory().getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				if(!this.moveItemStackTo(slotStack, 0, this.tileEntity.getUpgradeInventory().getContainerSize() + this.coinSlots.getContainerSize(), false))
				{
					return ItemStack.EMPTY;
				}
			}
			if(slotStack.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
		}
		
		return clickedStack;
		
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return this.hasPermission(Permissions.OPEN_STORAGE);
	}
	
	@Override
	public void removed(Player player)
	{
		this.clearContainer(player, this.coinSlots);
		
		super.removed(player);
		
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
		if(this.player.level.isClientSide)
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
			this.player.closeContainer();
			return;
		}
		
		if(!this.hasPermission(Permissions.STORE_COINS))
		{
			Settings.PermissionWarning(this.player, "store coins", Permissions.STORE_COINS);
			return;
		}
		
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.tileEntity.addStoredMoney(addValue);
		this.coinSlots.clearContent();
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeContainer();
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
		Container inventory = InventoryUtil.buildInventory(coinList);
		this.clearContainer(this.player, inventory);
		
		//Clear the coin storage
		this.tileEntity.clearStoredMoney();
		
	}

}