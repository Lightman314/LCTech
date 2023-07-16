package io.github.lightman314.lctech.common.menu.traderstorage.fluid;

import java.util.function.Function;

import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid.FluidTradeEditClientTab;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class FluidTradeEditTab extends TraderStorageTab {

	public FluidTradeEditTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new FluidTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.getTrader().hasPermission(player, Permissions.EDIT_TRADES); }

	int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public FluidTradeData getTrade() {
		if(this.menu.getTrader() instanceof FluidTraderData trader)
		{
			if(this.tradeIndex >= trader.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				this.menu.sendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC, null));
				return null;
			}
			return trader.getTrade(this.tradeIndex);
		}
		return null;
	}

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

	public void setTradeIndex(int tradeIndex) { this.tradeIndex = tradeIndex; }

	public void setType(TradeDirection type) {
		FluidTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTradeDirection(type);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putInt("NewType", type.index);
				this.menu.sendMessage(message);
			}
		}
	}

	//public void setCustomName(int selectedSlot, String customName)

	public void setQuantity(int amount) {
		FluidTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setBucketQuantity(amount);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putInt("NewQuantity", amount);
				this.menu.sendMessage(message);
			}
		}
	}

	public void setPrice(CoinValue price) {
		FluidTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCost(price);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.put("NewPrice", price.save());
				this.menu.sendMessage(message);
			}
		}
	}

	public void setFluid(FluidStack fluid) {
		FluidTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setProduct(fluid);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.getTrader() instanceof FluidTraderData fluidTrader)
			{
				if(fluidTrader.getStorage().refactorTanks())
					fluidTrader.markStorageDirty();
			}
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				CompoundTag fluidTag = new CompoundTag();
				fluid.writeToNBT(fluidTag);
				message.put("NewFluid", fluidTag);
				this.menu.sendMessage(message);
			}
		}
	}

	@Override
	public void receiveMessage(CompoundTag message) {
		if(message.contains("TradeIndex"))
		{
			this.tradeIndex = message.getInt("TradeIndex");
		}
		else if(message.contains("NewFluid"))
		{
			this.setFluid(FluidStack.loadFluidStackFromNBT(message.getCompound("NewFluid")));
		}
		else if(message.contains("NewQuantity"))
		{
			this.setQuantity(message.getInt("NewQuantity"));
		}
		else if(message.contains("NewPrice"))
		{
			this.setPrice(CoinValue.safeLoad(message, "NewPrice"));
		}
		else if(message.contains("NewType"))
		{
			this.setType(TradeDirection.fromIndex(message.getInt("NewType")));
		}
	}

}