package io.github.lightman314.lctech.common.logger;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class EnergyShopLogger extends TextLogger {

	public EnergyShopLogger() {
		super("EnergyShopHistory");
	}
	
	public void AddLog(Player player, EnergyTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		Component creativeText = isCreative ? new TranslatableComponent("log.shoplog.creative") : new TextComponent("");
		Component playerName = new TextComponent(player.getName().getString()).withStyle(ChatFormatting.GREEN);
		Component boughtText = new TranslatableComponent("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		
		Component cost = getCostText(pricePaid);
		
		AddLog(new TranslatableComponent("log.shoplog.energy.format", creativeText, playerName, boughtText, EnergyUtil.formatEnergyAmount(trade.getAmount()), cost));
		
	}
	
}
