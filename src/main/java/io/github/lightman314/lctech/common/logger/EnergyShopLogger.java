package io.github.lightman314.lctech.common.logger;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.network.chat.Component;

public class EnergyShopLogger extends TextLogger {

	public EnergyShopLogger() {
		super("EnergyShopHistory");
	}
	
	public void AddLog(PlayerReference player, EnergyTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		Component creativeText = isCreative ? Component.translatable("log.shoplog.creative") : Component.empty();
		Component playerName = getPlayerText(player);
		Component boughtText = Component.translatable("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		
		Component cost = getCostText(pricePaid);
		
		AddLog(Component.translatable("log.shoplog.energy.format", creativeText, playerName, boughtText, EnergyUtil.formatEnergyAmount(trade.getAmount()), cost));
		
	}
	
}
