package io.github.lightman314.lctech.menu;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.core.ModMenus;
import io.github.lightman314.lctech.menu.slots.UpgradeInputSlot;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidEditOpen;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderStorageMenu;
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

public class FluidTraderStorageMenu extends AbstractContainerMenu implements ITraderStorageMenu{

	public final Player player;
	private final Supplier<IFluidTrader> traderSource;
	public IFluidTrader getTrader() { return this.traderSource == null ? null : this.traderSource.get(); }
	
	final Container safeUpgradeSlots;
	final Container coinSlots;
	
	public FluidTraderStorageMenu(int windowId, Inventory inventory, BlockPos traderPos) {
		this(ModMenus.FLUID_TRADER_STORAGE, windowId, inventory, IFluidTrader.BlockEntitySource(inventory.player.level, traderPos));
	}
	
	protected FluidTraderStorageMenu(MenuType<?> type, int windowId, Inventory inventory, Supplier<IFluidTrader> traderSource) {
		super(type, windowId);
		
		this.player = inventory.player;
		this.traderSource = traderSource;
		
		this.getTrader().userOpen(this.player);
		
		//No Storage slots, it's all handled by the buttons
		
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.getTrader()) + 32;
		
		//Upgrade slots
		this.safeUpgradeSlots = new SuppliedContainer(new SafeUpgradeSlotSupplier(this.traderSource));
		for(int i = 0; i < this.safeUpgradeSlots.getContainerSize(); i++)
		{
			this.addSlot(new UpgradeInputSlot(this.safeUpgradeSlots, i, inventoryOffset - 24, getStorageBottom() + 6 + i * 18, this.getTrader(), this::OnUpgradeSlotChanged));
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
		this.getTrader().getFluidHandler().OnPlayerInteraction(this, this.player, tradeIndex);
	}
	
	public int getStorageBottom()
	{
		return FluidTraderUtil.getTradeDisplayHeight(this.getTrader());
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
				if(!this.moveItemStackTo(slotStack, this.safeUpgradeSlots.getContainerSize() + this.coinSlots.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				if(!this.moveItemStackTo(slotStack, 0, this.safeUpgradeSlots.getContainerSize() + this.coinSlots.getContainerSize(), false))
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
	public boolean stillValid(Player playerIn) { return this.getTrader() != null && this.hasPermission(Permissions.OPEN_STORAGE); }
	
	@Override
	public void removed(Player player)
	{
		this.clearContainer(player, this.coinSlots);
		
		super.removed(player);
		
		if(this.getTrader() != null)
			this.getTrader().userClose(this.player);
		
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
	
	private void OnUpgradeSlotChanged()
	{
		this.getTrader().reapplyUpgrades();
		this.getTrader().markTradesDirty();
	}
	
	public void openFluidEditScreenForTrade(int tradeIndex)
	{
		if(this.player.level.isClientSide)
		{
			LCTechPacketHandler.instance.sendToServer(new MessageFluidEditOpen(tradeIndex));
		}
		else
		{
			this.getTrader().openFluidEditMenu(this.player, tradeIndex);
		}
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void AddCoins()
	{
		if(this.getTrader() == null)
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
		this.getTrader().addStoredMoney(addValue);
		this.coinSlots.clearContent();
		
	}
	
	public void CollectCoinStorage()
	{
		if(this.getTrader() == null)
		{
			this.player.closeContainer();
			return;
		}
		
		if(!this.hasPermission(Permissions.COLLECT_COINS))
		{
			Settings.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
			return;
		}
		
		if(this.getTrader().getCoreSettings().hasBankAccount())
			return;
		
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(this.getTrader().getInternalStoredMoney());
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
		this.getTrader().clearStoredMoney();
		
	}
	
	//Additional Menu Types
	public static class FluidTraderStorageMenuUniversal extends FluidTraderStorageMenu
	{
		public FluidTraderStorageMenuUniversal(int windowID, Inventory inventory, UUID traderID) {
			super(ModMenus.UNIVERSAL_FLUID_TRADER_STORAGE, windowID, inventory, IFluidTrader.UniversalSource(inventory.player.level, traderID));
		}
	}
	
	
	private class SafeUpgradeSlotSupplier implements Supplier<Container>
	{
		private final Supplier<IFluidTrader> traderSource;
		private final IFluidTrader getTrader() { return this.traderSource.get(); }
		private final int upgradeSlotCount;
		SafeUpgradeSlotSupplier(Supplier<IFluidTrader> traderSource) {
			this.traderSource = traderSource;
			this.upgradeSlotCount = this.getTrader().getUpgradeInventory().getContainerSize();
		}
		@Override
		public Container get() {
			IFluidTrader trader = this.getTrader();
			if(trader != null)
				return trader.getUpgradeInventory();
			return new SimpleContainer(this.upgradeSlotCount);
		}
	}

}