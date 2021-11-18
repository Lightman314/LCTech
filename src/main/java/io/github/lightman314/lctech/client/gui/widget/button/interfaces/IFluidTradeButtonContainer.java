package io.github.lightman314.lctech.client.gui.widget.button.interfaces;

import java.util.List;

import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IFluidTradeButtonContainer {

	public long GetCoinValue();
	
	public ItemStack getBucketItem();
	
	public TradeEvent.TradeCostEvent TradeCostEvent(FluidTradeData trade);
	
	public boolean PermissionToTrade(int tradeIndex, List<ITextComponent> denialOutput);
	
}
