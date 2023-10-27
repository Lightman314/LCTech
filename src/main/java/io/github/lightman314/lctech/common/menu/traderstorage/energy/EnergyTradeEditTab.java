package io.github.lightman314.lctech.common.menu.traderstorage.energy;

import java.util.function.Function;

import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy.EnergyTradeEditClientTab;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.tradedata.EnergyTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyTradeEditTab extends TraderStorageTab {

	public EnergyTradeEditTab(TraderStorageMenu menu) {super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object menu) { return new EnergyTradeEditClientTab(menu, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.getTrader().hasPermission(player, Permissions.EDIT_TRADES); }

	int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public EnergyTradeData getTrade() {
		if(this.menu.getTrader() instanceof EnergyTraderData trader)
		{
			if(this.tradeIndex >= trader.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				this.menu.SendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC));
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
		EnergyTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTradeDirection(type);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleInt("NewType", type.index));
		}
	}

	public void setQuantity(int amount) {
		EnergyTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setAmount(amount);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleInt("NewQuantity", amount));
		}
	}

	public void setPrice(CoinValue price) {
		EnergyTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCost(price);
			this.menu.getTrader().markTradesDirty();
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleCoinValue("NewPrice", price));
		}
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
		{
			this.tradeIndex = message.getInt("TradeIndex");
		}
		else if(message.contains("NewQuantity"))
		{
			this.setQuantity(message.getInt("NewQuantity"));
		}
		else if(message.contains("NewPrice"))
		{
			this.setPrice(message.getCoinValue("NewPrice"));
		}
		else if(message.contains("NewType"))
		{
			this.setType(TradeDirection.fromIndex(message.getInt("NewType")));
		}
	}

}