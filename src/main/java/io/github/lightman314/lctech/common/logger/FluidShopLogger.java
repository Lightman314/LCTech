package io.github.lightman314.lctech.common.logger;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidShopLogger extends TextLogger{

	public FluidShopLogger() {
		super("FluidShopHistory");
	}
	
	public void AddLog(PlayerEntity player, FluidTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		ITextComponent creativeText = isCreative ? new TranslationTextComponent("log.shoplog.creative") : new StringTextComponent("");
		ITextComponent playerName = new StringTextComponent("§a" + player.getName().getString());
		ITextComponent boughtText = new TranslationTextComponent("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		
		ITextComponent fluidName = FluidFormatUtil.getFluidName(trade.getProduct());
		
		ITextComponent fluidText = new TranslationTextComponent("log.shoplog.fluid.fluidformat",trade.getProduct().getAmount(), fluidName);
		ITextComponent cost = new StringTextComponent("§e" + pricePaid.getString());
		
		AddLog(new TranslationTextComponent("log.shoplog.fluid.format",creativeText, playerName, boughtText, fluidText, cost));
		
	}

}
