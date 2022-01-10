package io.github.lightman314.lctech.trader;

import java.util.List;

import io.github.lightman314.lctech.tileentities.handler.TradeFluidHandler;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.trader.upgrades.UpgradeType.IUpgradeable;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;

public interface IFluidTrader extends ITrader, IUpgradeable{

	public FluidTradeData getTrade(int tradeIndex);
	public List<FluidTradeData> getAllTrades();
	public void markTradesDirty();
	public IInventory getUpgradeInventory();
	public void reapplyUpgrades();
	public TradeFluidHandler getFluidHandler();
	public boolean drainCapable();
	public void openTradeMenu(PlayerEntity player);
	public void openStorageMenu(PlayerEntity player);
	public void openFluidEditMenu(PlayerEntity player, int tradeIndex);
	public FluidTraderSettings getFluidSettings();
	public void markFluidSettingsDirty();
	
}
