package io.github.lightman314.lctech.common;

import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;

public class FluidTraderUtil {

	public static final int TRADEBUTTON_VERT_SPACER = 4;
	public static final int TRADEBUTTON_VERTICALITY = FluidTradeButton.HEIGHT + TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_HORIZ_SPACER = 6;
	public static final int TRADEBUTTON_HORIZONTAL = FluidTradeButton.WIDTH + TRADEBUTTON_HORIZ_SPACER;
	
	public static int getWidth(IFluidTrader trader)
	{
		return Math.max(176, getTradeDisplayWidth(trader));
	}
	
	public static int getTradeDisplayWidth(IFluidTrader trader)
	{
		return 12 + (getTradeDisplayColumnCount(trader) * TRADEBUTTON_HORIZONTAL) - TRADEBUTTON_HORIZ_SPACER;
	}
	
	public static int getTradeDisplayHeight(IFluidTrader trader)
	{
		return 17 + (getTradeDisplayRowCount(trader) * TRADEBUTTON_VERTICALITY) + 7;
	}
	
	public static int getTradeDisplayOffset(IFluidTrader trader)
	{
		if(getTradeDisplayWidth(trader) > 176)
			return 0;
		else
			return (176 - getTradeDisplayWidth(trader))/2;
	}
	
	public static int getInventoryDisplayOffset(IFluidTrader trader)
	{
		if(getTradeDisplayWidth(trader) <= 176)
			return 0;
		else
			return (getTradeDisplayWidth(trader) - 176) /2;
	}
	
	public static int getTradeDisplayColumnCount(IFluidTrader trader)
	{
		if(trader.getTradeCount() <= 4)
			return 2;
		else if(trader.getTradeCount() <= 6)
			return 3;
		else
			return 4;
	}
	
	public static int getTradeDisplayRowCount(IFluidTrader trader)
	{
		return ((trader.getTradeCount() - 1)/getTradeDisplayColumnCount(trader)) + 1;
	}
	
	public static int getColumnOf(IFluidTrader trader, int slotIndex)
	{
		return slotIndex % getTradeDisplayColumnCount(trader);
	}
	
	public static int getRowOf(IFluidTrader trader, int slotIndex)
	{
		return slotIndex / getTradeDisplayColumnCount(trader);
	}
	
	public static int getButtonPosX(IFluidTrader trader, int slotIndex)
	{
		float offset = 0f;
		if(getRowOf(trader, slotIndex) == getTradeDisplayRowCount(trader) - 1 && trader.getTradeCount() % getTradeDisplayColumnCount(trader) != 0)
		{
			offset = (0.5f * getTradeDisplayColumnCount(trader)) - (0.5f * (trader.getTradeCount() % getTradeDisplayColumnCount(trader)));
		}
		return (int)(6 + getTradeDisplayOffset(trader) + (((slotIndex % getTradeDisplayColumnCount(trader)) + offset) * (FluidTradeButton.WIDTH + 6f)));
	}
	
	public static int getButtonPosY(IFluidTrader trader, int slotIndex)
	{
		return 17 + (getRowOf(trader, slotIndex) * (FluidTradeButton.HEIGHT + TRADEBUTTON_VERT_SPACER));
	}
	
	public static int getPriceButtonPosX(IFluidTrader trader, int slotIndex)
	{
		return getButtonPosX(trader, slotIndex) + FluidTradeButton.ICONPOS_X;
	}
	
	public static int getPriceButtonPosY(IFluidTrader trader, int slotIndex)
	{
		return getButtonPosY(trader, slotIndex) + FluidTradeButton.PRICEBUTTON_Y;
	}
	
}
