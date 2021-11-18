package io.github.lightman314.lctech.trader;

import java.util.List;

import io.github.lightman314.lctech.tileentities.TradeFluidHandler;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.entity.player.PlayerEntity;

public interface IFluidTrader extends ITrader{

	public FluidTradeData getTrade(int tradeIndex);
	public List<FluidTradeData> getAllTrades();
	public void markTradesDirty();
	public TradeFluidHandler getFluidHandler();
	public boolean drainCapable();
	public void openTradeMenu(PlayerEntity player);
	public void openStorageMenu(PlayerEntity player);
	public void openFluidEditMenu(PlayerEntity player, int tradeIndex);
	
}
