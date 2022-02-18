package io.github.lightman314.lctech.client.gui.widget.button;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EnergyTradeButton extends Button{

	public static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/energy_trader_buttons.png");
	
	public static final int WIDTH = 70;
	public static final int HEIGHT = 26;
	
	public static final int TEXTPOS_1 = 4;
	public static final int TEXTPOS_2 = 15;
	
	int tradeIndex;
	Supplier<IEnergyTrader> source;
	Supplier<Long> availableCoins;
	Supplier<ItemStack> batterySlot;
	Screen screen;
	Font font;
	
	public EnergyTradeButton(int x, int y, OnPress pressable, int tradeIndex, Screen screen, Font font, Supplier<IEnergyTrader> source, Supplier<Long> availableCoins, Supplier<ItemStack> batterySlot)
	{
		super(x, y, WIDTH, HEIGHT, new TextComponent(""), pressable);
		this.tradeIndex = tradeIndex;
		this.screen = screen;
		this.font = font;
		this.source = source;
		this.availableCoins = availableCoins;
		this.batterySlot = batterySlot;
	}
	
	private boolean tradeExists() { return this.tradeIndex < this.source.get().getTradeCount(); }
	private EnergyTradeData getTrade() { return this.source.get().getTrade(this.tradeIndex); }
	
	private static PlayerReference getPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		return PlayerReference.of(minecraft.player);
	}
	
	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.visible = this.tradeExists();
		this.active = isActive(this.getTrade(), this.source.get());
		if(this.visible)
			renderEnergyTradeButton(pose, this.screen, this.font, this.x, this.y, this.tradeIndex, this.source.get(), this.isHovered, false, this.availableCoins.get(), this.batterySlot.get());
	}
	
	public static void renderEnergyTradeButton(PoseStack pose, Screen screen, Font font, int x, int y, int tradeIndex, IEnergyTrader trader)
	{
		renderEnergyTradeButton(pose, screen, font, x, y, tradeIndex, trader, false, true, 0, ItemStack.EMPTY);
	}
	
	private static void renderEnergyTradeButton(PoseStack poseStack, Screen screen, Font font, int x, int y, int tradeIndex, IEnergyTrader trader, boolean hovered, boolean forceActive, long availableCoins, ItemStack batterySlot)
	{
		
		PlayerReference player = getPlayer();
		
		EnergyTradeData trade = trader.getTrade(tradeIndex);
		RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);
		boolean active = forceActive ? true : isActive(trade, trader);
		
		if(active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		int yOffset = getRenderYOffset(trade.getTradeDirection());
		int xOffset = hovered ? WIDTH : 0;
		//Draw Button BG
		screen.blit(poseStack, x, y, xOffset, yOffset, WIDTH, HEIGHT);
		
		//Collect data
		boolean hasPermission = forceActive;
		boolean hasDiscount = false;
		boolean isValid = forceActive ? true : trade.isValid();
		boolean hasStock = forceActive ? true : trade.hasStock(trader, player) || trader.getCoreSettings().isCreative();
		boolean hasSpace = forceActive ? true : trade.hasSpace(trader) || trader.getCoreSettings().isCreative();
		boolean canAfford = forceActive ? true : false;
		CoinValue cost = trade.getCost();
		if(!forceActive)
		{
			//Discount check
			TradeCostEvent event = trader.runTradeCostEvent(player, tradeIndex);
			cost = event.getCostResult();
			hasDiscount = event.getCostMultiplier() != 1d;
			//Permission
			hasPermission = !trader.runPreTradeEvent(player, tradeIndex).isCanceled();
			//CanAfford
			canAfford = canAfford(trader, tradeIndex, availableCoins, batterySlot) && trade.canTransferEnergy(trader, batterySlot);
		}
		
		//Render the trade text
		String tradeText = ItemTradeButton.getTradeText(cost, trade.getCost().isFree(), isValid, hasStock, hasSpace, hasPermission);
		int textColor = ItemTradeButton.getTradeTextColor(isValid, canAfford, hasStock, hasPermission, hasDiscount);
		int stringLength = font.width(tradeText);
		font.draw(poseStack, tradeText, x + WIDTH / 2 - stringLength / 2, y + TEXTPOS_1, textColor);
		
		//Render the amount
		String tradeAmount = EnergyUtil.formatEnergyAmount(trade.getAmount());
		stringLength = font.width(tradeAmount);
		font.draw(poseStack, tradeAmount, x + WIDTH / 2 - stringLength / 2, y + TEXTPOS_2, 0xFFFFFF);
		
	}
	
	public void tryRenderTooltip(PoseStack pose, Screen screen, IEnergyTrader trader, int mouseX, int mouseY)
	{
		if(this.isHovered && this.visible)
			renderTooltip(pose, screen, this.tradeIndex, trader, this.x, this.y, mouseX, mouseY);
	}
	
	public static void renderTooltip(PoseStack pose, Screen screen, int tradeIndex, IEnergyTrader trader, int x, int y, int mouseX, int mouseY)
	{
		EnergyTradeData trade = trader.getTrade(tradeIndex);
		int amount = trade.getAmount();
		if(amount <= 0)
			return;
		
		List<Component> tooltips = Lists.newArrayList();
		//Energy Amount, Selling/Purchasing
		tooltips.add(new TranslatableComponent("gui.lctech.energytrade.tooltip." + trade.getTradeDirection().name().toLowerCase(), new TextComponent(EnergyUtil.formatEnergyAmount(trade.getAmount())).withStyle(ChatFormatting.GOLD)));
		//Stock
		tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.stock", trader.getCoreSettings().isCreative() ? new TranslatableComponent("tooltip.lightmanscurrency.trader.stock.infinite") : new TextComponent("" + trade.getStock(trader, getPlayer())).withStyle(ChatFormatting.GOLD)));
		//If denied, give denial reason
		PreTradeEvent pte = trader.runPreTradeEvent(getPlayer(), tradeIndex);
		if(pte.isCanceled())
			pte.getDenialReasons().forEach(reason -> tooltips.add(reason));
		
		screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
	}
	
	public static int getRenderYOffset(TradeData.TradeDirection tradeType)
	{
		if(tradeType == TradeData.TradeDirection.PURCHASE)
			return HEIGHT;
		return 0;
	}
	
	protected static boolean canAfford(IEnergyTrader trader, int tradeIndex, long availableCoins, ItemStack batterySlot)
	{
		EnergyTradeData trade = trader.getTrade(tradeIndex);
		if(trade.isSale())
		{
			if(trade.getCost().isFree())
				return true;
			else
				return availableCoins >= trader.runTradeCostEvent(getPlayer(), tradeIndex).getCostResult().getRawValue();
		}
		else if(trade.isPurchase())
		{
			return trade.canTransferEnergy(trader, batterySlot);
		}
		return true;
	}
	
	public static boolean isActive(EnergyTradeData trade, IEnergyTrader trader)
	{
		if(trade.isValid())
		{
			return trader.getCoreSettings().isCreative() || trade.hasStock(trader, getPlayer());
		}
		return false;
	}
	
}
