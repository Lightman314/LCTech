package io.github.lightman314.lctech.common.logger;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;

public class FluidShopLogger extends TextLogger{

	public FluidShopLogger() {
		super("FluidShopHistory");
	}
	
	public void AddLog(PlayerEntity player, FluidTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		
	}

}
