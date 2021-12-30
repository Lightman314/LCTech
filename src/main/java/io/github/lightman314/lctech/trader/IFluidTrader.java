package io.github.lightman314.lctech.trader;

import java.util.List;

import io.github.lightman314.lctech.tileentities.TradeFluidHandler;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public interface IFluidTrader extends ITrader, IUpgradeable{

	public FluidTradeData getTrade(int tradeIndex);
	public List<FluidTradeData> getAllTrades();
	public void markTradesDirty();
	public Container getUpgradeInventory();
	public void reapplyUpgrades();
	public TradeFluidHandler getFluidHandler();
	public boolean drainCapable();
	public void openTradeMenu(Player player);
	public void openStorageMenu(Player player);
	public void openFluidEditMenu(Player player, int tradeIndex);
	
}
