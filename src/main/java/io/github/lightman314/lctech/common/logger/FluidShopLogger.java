package io.github.lightman314.lctech.common.logger;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.network.chat.Component;

public class FluidShopLogger extends TextLogger{

	public FluidShopLogger() {
		super("FluidShopHistory");
	}
	
	public void AddLog(PlayerReference player, FluidTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		Component creativeText = isCreative ? Component.translatable("log.shoplog.creative") : Component.empty();
		Component playerName = getPlayerText(player);
		Component boughtText = Component.translatable("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		
		Component fluidName = FluidFormatUtil.getFluidName(trade.getProduct());
		
		Component fluidText = Component.translatable("log.shoplog.fluid.fluidformat", FluidFormatUtil.formatFluidAmount(trade.getQuantity()), fluidName);
		Component cost = getCostText(pricePaid);
		
		AddLog(Component.translatable("log.shoplog.fluid.format",creativeText, playerName, boughtText, fluidText, cost));
		
	}

}
