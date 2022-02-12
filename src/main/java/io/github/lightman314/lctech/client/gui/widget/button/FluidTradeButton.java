package io.github.lightman314.lctech.client.gui.widget.button;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lctech.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class FluidTradeButton extends Button{
	
	public static final ResourceLocation TRADE_TEXTURES = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader_buttons.png");
	
	public static final int WIDTH = 63;
	public static final int HEIGHT = 54;
	
	public static final float TEXTPOS_X = WIDTH - 19;
	public static final float TEXTPOS_Y = 5f;
	
	public static final int BUCKETPOS_X = WIDTH - 17;
	public static final int BUCKETPOS_Y = 1;
	
	public static final int TANKPOS_X = 3;
	public static final int TANKPOS_Y = 19;
	public static final int TANK_SIZE_X = 45;
	public static final int TANK_SIZE_Y = 32;
	
	public static final int ICONPOS_X = 51;
	public static final int PRICEBUTTON_Y = 18;
	public static final int DRAINICON_Y = 30;
	public static final int FILLICON_Y = 42;
	
	public static final int ENABLED_COLOR = 0x00FF00;
	public static final int DISABLED_COLOR = 0xFF0000;
	
	int tradeIndex;
	Supplier<IFluidTrader> source;
	Supplier<Long> availableCoins;
	Supplier<ItemStack> bucketSlot;
	Screen screen;
	Font font;
	
	public FluidTradeButton(int x, int y, OnPress pressable, int tradeIndex, Screen screen, Font font, Supplier<IFluidTrader> source, Supplier<Long> availableCoins, Supplier<ItemStack> bucketSlot)
	{
		
		super(x,y,WIDTH, HEIGHT, new TextComponent(""), pressable);
		this.tradeIndex = tradeIndex;
		this.screen = screen;
		this.font = font;
		this.source = source;
		this.availableCoins = availableCoins;
		this.bucketSlot = bucketSlot;
		
	}
	
	private FluidTradeData getTrade() { return this.source.get().getTrade(this.tradeIndex); }
	
	private static PlayerReference getPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		return PlayerReference.of(minecraft.player);
	}
	
	@Override
	public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.active = isActive(this.getTrade(), this.source.get());
		renderFluidTradeButton(matrixStack, this.screen, this.font, this.x, this.y, this.tradeIndex, this.source.get(), this.isHovered, false, false, this.availableCoins.get(), this.bucketSlot.get());
	}
	
	public static void renderFluidTradeButton(PoseStack poseStack, Screen screen, Font font, int x, int y, int tradeIndex, IFluidTrader trader, boolean hovered, boolean forceActive, boolean storageMode)
	{
		renderFluidTradeButton(poseStack, screen, font, x, y, tradeIndex, trader, hovered, forceActive, storageMode, 0, ItemStack.EMPTY);
	}
	
	//No inverted input as fluid trade buttons are also the input method.
	private static void renderFluidTradeButton(PoseStack poseStack, Screen screen, Font font, int x, int y, int tradeIndex, IFluidTrader trader, boolean hovered, boolean forceActive, boolean storageMode, long availableCoins, ItemStack bucketSlot)
	{
		
		PlayerReference player = getPlayer();
		
		FluidTradeData trade = trader.getTrade(tradeIndex);
		RenderSystem.setShaderTexture(0, TRADE_TEXTURES);
		boolean active = forceActive ? true : isActive(trade, trader);
		
		if(active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		int yOffset = getRenderYOffset(trade.getTradeDirection());
		int xOffset = hovered ? WIDTH : 0;
		//Draw Button BG
		screen.blit(poseStack, x, y, xOffset, yOffset, WIDTH, HEIGHT);
		//Draw drain & fill icons
		if(trader.drainCapable())
			screen.blit(poseStack, x + ICONPOS_X, y + DRAINICON_Y, trade.canDrainExternally() ? 0 : 10, HEIGHT * 4,  10, 10);
		if(storageMode)
			screen.blit(poseStack, x + ICONPOS_X, y + FILLICON_Y, trade.canFillExternally() ? 20 : 30, HEIGHT * 4, 10, 10);
		
		//Collect data
		boolean hasPermission = forceActive ? true : false;
		boolean hasDiscount = false;
		boolean isValid = forceActive ? true : trade.isValid();
		boolean hasStock = forceActive ? true : trade.hasStock(trader, getPlayer()) || trader.getCoreSettings().isCreative();
		boolean hasSpace = forceActive ? true : trade.hasSpace() || trader.getCoreSettings().isCreative();
		boolean canAfford = forceActive ? true : false;
		CoinValue cost = trade.getCost();
		if(!forceActive)
		{
			
			//Discount Check
			TradeCostEvent event = trader.runTradeCostEvent(getPlayer(), tradeIndex);
			cost = event.getCostResult();
			hasDiscount = event.getCostMultiplier() != 1d;
			//Has Stock
			hasStock = trade.hasStock(trader, getPlayer()) || trader.getCoreSettings().isCreative();
			//Permission
			hasPermission = !trader.runPreTradeEvent(player, tradeIndex).isCanceled();
			//CanAfford
			canAfford = canAfford(trader, tradeIndex, availableCoins, bucketSlot) && trade.canTransferFluids(bucketSlot);
			
		}
		//Render the trade text
		//Run the Item Trade Button variant of get trade text/color as no significant changes have been made
		String tradeText = ItemTradeButton.getTradeText(cost, trade.getCost().isFree(), isValid, hasStock, hasSpace, hasPermission);
		int textColor = ItemTradeButton.getTradeTextColor(trade.isValid(), canAfford, hasStock, hasPermission, hasDiscount);
		int stringLength = font.width(tradeText);
		font.draw(poseStack, tradeText, x + TEXTPOS_X - stringLength, y + TEXTPOS_Y, textColor);
		
		//Render the fluid product as a bucket
		//Render an empty bucket if the product is empty
		ItemStack bucketStack = FluidItemUtil.getFluidDisplayItem(trade.getProduct());
		bucketStack.setCount(trade.getBucketQuantity());
		ItemRenderUtil.drawItemStack(screen, font, bucketStack, x + BUCKETPOS_X, y + BUCKETPOS_Y, true);
		//Render the fluid tank
		if(!trade.getTankContents().isEmpty())
		{
			//Shader reset in drawFluidTankInGUI function, as drawItemStack changes it in some way shape or form.
			FluidRenderUtil.drawFluidTankInGUI(trade.getTankContents(), x + TANKPOS_X, y + TANKPOS_Y, TANK_SIZE_X, TANK_SIZE_Y, trade.getTankFillPercent());
		}
		
		//Draw Button Overlay
		//Reset render system color & shader
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		if(active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		RenderSystem.setShaderTexture(0, TRADE_TEXTURES);
		screen.blit(poseStack, x, y, 2 * WIDTH, 0, WIDTH, HEIGHT); 
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
	
	public void tryRenderTooltip(PoseStack matrixStack, Screen screen, IFluidTrader trader, int mouseX, int mouseY, boolean storageMode)
	{
		if(this.isHovered)
			tryRenderTooltip(matrixStack, screen, this.tradeIndex, trader, this.x, this.y, mouseX, mouseY, storageMode);
	}
	
	public static int tryRenderTooltip(PoseStack matrixStack, Screen screen, int tradeIndex, IFluidTrader trader, int x, int y, int mouseX, int mouseY, boolean storageMode)
	{
		if(isMouseOverBucket(x,y,mouseX, mouseY))
		{
			List<Component> tooltip = getTooltipForBucket(screen, tradeIndex, trader);
			if(tooltip != null)
			{
				screen.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
				return 2;
			}
			return -2;
		}
		else if(isMouseOverTank(x,y,mouseX,mouseY))
		{
			List<Component> tooltip = getTooltipForTank(screen, trader.getTrade(tradeIndex), storageMode);
			if(tooltip != null)
			{
				screen.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
				return 1;
			}
			return -1;
		}
		else if(isMouseOverIcon(0, x, y, mouseX, mouseY) && trader.drainCapable())
		{
			FluidTradeData trade = trader.getTrade(tradeIndex);
			screen.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drain." + (trade.canDrainExternally() ? "enabled" : "disabled")).withStyle(Style.EMPTY.withColor(trade.canDrainExternally() ? ENABLED_COLOR : DISABLED_COLOR)), mouseX, mouseY);
		}
		else if(storageMode && isMouseOverIcon(1, x, y, mouseX, mouseY))
		{
			FluidTradeData trade = trader.getTrade(tradeIndex);
			screen.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.fill." + (trade.canFillExternally() ? "enabled" : "disabled")).withStyle(Style.EMPTY.withColor(trade.canFillExternally() ? ENABLED_COLOR : DISABLED_COLOR)), mouseX, mouseY);
		}
		return 0;
	}
	
	public static boolean isMouseOverBucket(int x, int y, int mouseX, int mouseY)
	{
		int minX = x + BUCKETPOS_X;
		int minY = y + BUCKETPOS_Y;
		return mouseX >= minX && mouseX < (minX + 16) && mouseY >= minY && mouseY < (minY + 16);
	}
	
	public static boolean isMouseOverTank(int x, int y, int mouseX, int mouseY)
	{
		int minX = x + TANKPOS_X;
		int minY = y + TANKPOS_Y;
		return mouseX >= minX && mouseX < (minX + TANK_SIZE_X) && mouseY >= minY && mouseY < (minY + TANK_SIZE_Y);
	}
	
	public static boolean isMouseOverIcon(int icon, int x, int y, int mouseX, int mouseY)
	{
		int minX = x + ICONPOS_X;
		int minY = y + (icon == 0 ? DRAINICON_Y : FILLICON_Y);
		return mouseX >= minX && mouseX < (minX + 10) && mouseY >= minY && mouseY < (minY + 10);
	}
	
	public static List<Component> getTooltipForBucket(Screen screen, int tradeIndex, IFluidTrader trader)
	{
		FluidTradeData trade = trader.getTrade(tradeIndex);
		FluidStack product = trade.getProduct();
		if(product.isEmpty())
			return null;
		
		List<Component> tooltips = Lists.newArrayList();
		
		//Fluid Name
		tooltips.add(new TranslatableComponent("gui.lctech.fluidtrade.tooltip." + trade.getTradeDirection().name().toLowerCase(), FluidFormatUtil.getFluidName(product, ChatFormatting.GOLD)));
		//Quantity
		tooltips.add(new TranslatableComponent("gui.lctech.fluidtrade.tooltip.quantity", trade.getBucketQuantity(), FluidFormatUtil.formatFluidAmount(trade.getQuantity())).withStyle(ChatFormatting.GOLD));
		//Stock
		tooltips.add(new TranslatableComponent("tooltip.lightmanscurrency.trader.stock", trader.getCoreSettings().isCreative() ? new TranslatableComponent("tooltip.lightmanscurrency.trader.stock.infinite") : new TextComponent("�6" + trade.getStock(trader, getPlayer()))));
		//If denied, give denial reason
		PreTradeEvent pte = trader.runPreTradeEvent(getPlayer(), tradeIndex);
		if(pte.isCanceled())
			pte.getDenialReasons().forEach(reason -> tooltips.add(reason));
		
		return tooltips;
	}
	
	public static List<Component> getTooltipForTank(Screen screen, FluidTradeData trade, boolean storageMode)
	{
		List<Component> tooltips = Lists.newArrayList();
		if(trade.getTankContents().isEmpty())
		{
			tooltips.add(new TranslatableComponent("gui.lctech.nofluid").withStyle(ChatFormatting.GRAY));
		}
		else
		{
			//Fluid Name
			tooltips.add(FluidFormatUtil.getFluidName(trade.getTankContents()));
			//'amount'/'capacity'mB
			tooltips.add(new TextComponent(FluidFormatUtil.formatFluidAmount(trade.getTankContents().getAmount()) + "mB/" + FluidFormatUtil.formatFluidAmount(trade.getTankCapacity()) + "mB").withStyle(ChatFormatting.GRAY));
			//Pending drain
			if(trade.hasPendingDrain())
			{
				tooltips.add(new TranslatableComponent("gui.lctech.fluidtrade.pending_drain", FluidFormatUtil.formatFluidAmount(trade.getPendingDrain())));
			}
			if(storageMode)
				tooltips.add(new TranslatableComponent("tooltip.lctech.trader.fluid.fill_tank"));
		}
		return tooltips;
		
	}
	
	public static int getRenderYOffset(TradeData.TradeDirection tradeType)
	{
		if(tradeType == TradeData.TradeDirection.PURCHASE)
			return HEIGHT;
		return 0;
	}
	
	protected static boolean canAfford(IFluidTrader trader, int tradeIndex, long availableCoins, ItemStack bucketSlot)
	{
		FluidTradeData trade = trader.getTrade(tradeIndex);
		if(trade.isSale())
		{
			if(trade.getCost().isFree())
				return true;
			else
				return availableCoins >= trader.runTradeCostEvent(getPlayer(), tradeIndex).getCostResult().getRawValue();
		}
		else if(trade.isPurchase())
		{
			return trade.canTransferFluids(bucketSlot);
		}
		return true;
	}
	
	public static boolean isActive(FluidTradeData trade, IFluidTrader trader)
	{
		if(trade.isValid())
		{
			return trader.getCoreSettings().isCreative() || trade.hasStock(trader, getPlayer());
		}
		return false;
	}
	
}
