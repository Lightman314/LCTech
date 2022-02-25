package io.github.lightman314.lctech.common.logger;

import javax.annotation.Nonnull;

import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.api.TextLogger;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class EnergyShopLogger extends TextLogger {

	public EnergyShopLogger() {
		super("EnergyShopHistory");
	}
	
	public void AddLog(PlayerEntity player, EnergyTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		ITextComponent creativeText = isCreative ? new TranslationTextComponent("log.shoplog.creative") : new StringTextComponent("");
		ITextComponent playerName = new StringTextComponent(player.getName().getString()).mergeStyle(TextFormatting.GREEN);
		ITextComponent boughtText = new TranslationTextComponent("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		
		ITextComponent cost = getCostText(pricePaid);
		
		AddLog(new TranslationTextComponent("log.shoplog.energy.format", creativeText, playerName, boughtText, EnergyUtil.formatEnergyAmount(trade.getAmount()), cost));
		
	}
	
}
