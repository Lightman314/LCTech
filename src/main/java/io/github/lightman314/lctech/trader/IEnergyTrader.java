package io.github.lightman314.lctech.trader;

import io.github.lightman314.lctech.trader.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.world.entity.player.Player;

public interface IEnergyTrader extends ITrader, IUpgradeable{

	//Override un-needed functions
	public default int getTradeCount() { return 1; }
	public default int getTradeCountLimit() { return 1; }
	public default void requestAddOrRemoveTrade(boolean isAdd) { }
	public default void addTrade(Player requestor) { }
	public default void removeTrade(Player requestor) { }
	
}
