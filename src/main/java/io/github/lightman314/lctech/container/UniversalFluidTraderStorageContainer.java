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
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.UniversalMenu;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ICreativeTraderMenu;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UniversalFluidTraderStorageContainer extends UniversalMenu implements ITraderStorageMenu, ICreativeTraderMenu{

	public final Player player;
	
	final Container coinSlots;
	
	public UniversalFluidTraderData getData()
	{
		UniversalTraderData data = this.getRawData();
		if(data instanceof UniversalFluidTraderData)
			return (UniversalFluidTraderData)data;
		return null;
	}
	
	public UniversalFluidTraderStorageContainer(int windowId, Inventory inventory, UUID traderID) {
		super(ModContainers.UNIVERSAL_FLUID_TRADER_STORAGE, windowId, traderID, inventory.player);
		
		this.player = inventory.player;
		
		//No Storage slots, it's all handled by the buttons
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.getData()) + 32;
		
		//Upgrade slots
		for(int i = 0; i < this.getData().getUpgradeInventory().getContainerSize(); i++)
		{
			this.addSlot(new UpgradeInputSlot(this.getData().getUpgradeInventory(), i, inventoryOffset - 24, getStorageBottom() + 6 + i * 18, this.getData(), this::OnUpgradeSlotChanged));
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
		if(this.player.level.isClientSide) //Flag the fluid handler as client to block marking the data as dirty.
			this.getData().getFluidHandler().flagAsClient();
		
		this.getData().getFluidHandler().OnPlayerInteraction(this, this.player, tradeIndex);
	}
	
	public int getStorageBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.getData());
	}
	
	public void tick()
	{
		if(this.getData() == null)
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
				if(!this.moveItemStackTo(slotStack, this.getData().getUpgradeInventory().getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				if(!this.moveItemStackTo(slotStack, 0, this.getData().getUpgradeInventory().getContainerSize() + this.coinSlots.getContainerSize(), false))
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
		return true;
	}
	
	@Override
	public void removed(Player player)
	{
		this.clearContainer(player, this.coinSlots);
		
		super.removed(player);
		
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
		if(this.player.level.isClientSide)
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
			this.player.closeContainer();
			return;
		}
		
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.getData().addStoredMoney(addValue);
		this.coinSlots.clearContent();
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
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
		Container inventory = InventoryUtil.buildInventory(coinList);
		this.clearContainer(this.player, inventory);
		
		//Clear the coin storage
		this.getData().clearStoredMoney();
		
	}
	
	public void ToggleCreative()
	{
		if(this.getData() == null)
		{
			this.player.closeContainer();
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