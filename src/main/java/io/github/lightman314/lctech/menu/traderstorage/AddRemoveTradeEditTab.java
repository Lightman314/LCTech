package io.github.lightman314.lctech.menu.traderstorage;

import io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.AddRemoveTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AddRemoveTradeEditTab extends BasicTradeEditTab{

	public AddRemoveTradeEditTab(TraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new AddRemoveTradeEditClientTab(screen, this); }
	
	public void addTrade() {
		
		if(this.menu.getTrader() != null)
		{
			this.menu.getTrader().addTrade(this.menu.player);
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("AddTrade", true);
				this.menu.sendMessage(message);
			}
		}
		
	}
	
	public void removeTrade() {
		
		if(this.menu.getTrader() != null)
		{
			this.menu.getTrader().removeTrade(this.menu.player);
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("RemoveTrade", true);
				this.menu.sendMessage(message);
			}
		}
		
	}
	
	@Override
	public void receiveMessage(CompoundTag message) {
		super.receiveMessage(message);
		if(message.contains("AddTrade"))
			this.addTrade();
		if(message.contains("RemoveTrade"))
			this.removeTrade();
	}

}
