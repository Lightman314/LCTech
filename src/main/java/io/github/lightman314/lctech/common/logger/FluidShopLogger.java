package io.github.lightman314.lctech.common.logger;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class FluidShopLogger extends TextLogger{

	public FluidShopLogger() {
		super("FluidShopHistory");
	}
	
	public void AddLog(Player player, FluidTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		Component creativeText = isCreative ? new TranslatableComponent("log.shoplog.creative") : new TextComponent("");
		Component playerName = new TextComponent("§a" + player.getName().getString());
		Component boughtText = new TranslatableComponent("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		
		Component fluidName = FluidFormatUtil.getFluidName(trade.getProduct());
		
		Component fluidText = new TranslatableComponent("log.shoplog.fluid.fluidformat",trade.getProduct().getAmount(), fluidName);
		Component cost = new TextComponent("§e" + pricePaid.getString());
		
		AddLog(new TranslatableComponent("log.shoplog.fluid.format",creativeText, playerName, boughtText, fluidText, cost));
		
	}

}
