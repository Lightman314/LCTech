package io.github.lightman314.lctech.client.gui.screen;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

public class TradeFluidPriceScreen extends Screen implements ICoinValueInput{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/fluidtradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	Supplier<IFluidTrader> trader;
	public IFluidTrader getTrader() { return this.trader.get(); }
	Supplier<FluidTradeData> trade;
	int tradeIndex;
	TradeDirection localDirection;
	int localQuantity;
	
	Button buttonSetSell;
	Button buttonSetPurchase;
	
	Button buttonAddBucket;
	Button buttonRemoveBucket;
	
	PlainButton buttonToggleDrainable;
	boolean localDrainable;
	PlainButton buttonToggleFillable;
	boolean localFillable;
	
	Button buttonTradeRules;
	
	CoinValueInput priceInput;

	public TradeFluidPriceScreen(Supplier<IFluidTrader> trader, int tradeIndex) {
		super(new TranslationTextComponent("gui.lightmanscurrency.changeprice"));
		this.trader = trader;
		this.tradeIndex = tradeIndex;
		this.trade = () -> this.trader.get().getTrade(this.tradeIndex);
		//Store local copies of togglable data
		FluidTradeData localTrade = this.trade.get();
		this.localDirection = localTrade.getTradeDirection();
		this.localDrainable = localTrade.canDrainExternally();
		this.localFillable = localTrade.canFillExternally();
		this.localQuantity = localTrade.getBucketQuantity();
	}
	
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.priceInput = this.addListener(new CoinValueInput(guiTop, this.title, this.trade.get().getCost(), this));
		
		this.buttonSetSell = this.addButton(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.tradedirection.sale"), this::SetTradeType));
		this.buttonSetPurchase = this.addButton(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 6, 50, 21, new TranslationTextComponent("gui.button.lightmanscurrency.tradedirection.purchase"), this::SetTradeType));
		
		this.buttonAddBucket = this.addButton(new IconButton(guiLeft + 59, guiTop + CoinValueInput.HEIGHT + 6, this::PressQuantityButton, IconData.of(GUI_TEXTURE, this.xSize + 16, 0)));
		this.buttonRemoveBucket = this.addButton(new IconButton(guiLeft + 98, guiTop + CoinValueInput.HEIGHT + 6, this::PressQuantityButton, IconData.of(GUI_TEXTURE, this.xSize + 32, 0)));
		
		this.addButton(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.save"), this::PressSaveButton));
		this.addButton(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.back"), this::PressBackButton));
		//this.addButton(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 62, 51, 20, new TranslationTextComponent("gui.button.lightmanscurrency.free"), this::PressFreeButton));
		this.buttonTradeRules = this.addButton(IconAndButtonUtil.tradeRuleButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton));
		this.buttonTradeRules.visible = this.getTrader().hasPermission(this.minecraft.player, Permissions.EDIT_TRADE_RULES);
		
		this.buttonToggleDrainable = this.addButton(new PlainButton(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 37, 10, 10, this::PressToggleDrainButton, GUI_TEXTURE, this.xSize, 16));
		this.buttonToggleDrainable.visible = this.trader.get().drainCapable();
		this.buttonToggleFillable = this.addButton(new PlainButton(guiLeft + 95, guiTop + CoinValueInput.HEIGHT + 37, 10, 10, this::PressToggleFillButton, GUI_TEXTURE, this.xSize + 20, 16));
		this.buttonToggleFillable.visible = this.trader.get().drainCapable();
		
		tick();
		
	}

	public void tick()
	{
		
		if(this.trader.get() == null)
		{
			this.minecraft.displayGuiScreen(null);
			return;
		}
		
		this.buttonSetSell.active = this.localDirection != TradeDirection.SALE;
		this.buttonSetPurchase.active = this.localDirection != TradeDirection.PURCHASE;

		this.buttonToggleDrainable.setResource(GUI_TEXTURE, this.xSize + (this.localDrainable ? 0 : 10), 16);
		this.buttonToggleFillable.setResource(GUI_TEXTURE, this.xSize + (this.localFillable ? 20 : 30), 16);
		
		this.buttonAddBucket.active = this.localQuantity < this.trade.get().getMaxBucketQuantity();
		this.buttonRemoveBucket.active = this.localQuantity > 1;
		
		this.buttonTradeRules.visible = this.trader.get().hasPermission(this.minecraft.player, Permissions.EDIT_TRADE_RULES);
		
		super.tick();
		this.priceInput.tick();
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		if(this.trader.get() == null)
		{
			this.minecraft.displayGuiScreen(null);
			return;
		}
		
		this.renderBackground(matrixStack);
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY + CoinValueInput.HEIGHT, 0, 0, this.xSize, this.ySize - CoinValueInput.HEIGHT);
		
		//Render the price input before rendering the buttons
		this.priceInput.render(matrixStack, mouseX, mouseY, partialTicks);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//Render the Drainable & Fillable text
		if(this.trader.get().drainCapable())
		{
			this.font.func_243248_b(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid_settings.drainable").setStyle(Style.EMPTY.setColor(Color.fromInt(this.localDrainable ? FluidTradeButton.ENABLED_COLOR : FluidTradeButton.DISABLED_COLOR))), startX + 18, startY + CoinValueInput.HEIGHT + 38, 0xFFFFFF);
			this.font.func_243248_b(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid_settings.fillable").setStyle(Style.EMPTY.setColor(Color.fromInt(this.localFillable ? FluidTradeButton.ENABLED_COLOR : FluidTradeButton.DISABLED_COLOR))), startX + 106, startY + CoinValueInput.HEIGHT + 38, 0xFFFFFF);
		}	
		
		//Render the local quantity text
		String quantityText = this.localQuantity + "B";
		int textWidth = this.font.getStringWidth(quantityText);
		this.font.drawString(matrixStack, quantityText, startX + 1 + (176 / 2) - (textWidth / 2), startY + CoinValueInput.HEIGHT + 12, 0xFFFFFF);
		
		//Mouse over for buttons
		IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, this.buttons);
		
		if(this.buttonToggleDrainable.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid_settings.drainable.wordy"), mouseX, mouseY);
		else if(this.buttonToggleFillable.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid_settings.fillable.wordy"), mouseX, mouseY);
		else if(this.buttonAddBucket.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid.add_bucket"), mouseX, mouseY);
		else if(this.buttonRemoveBucket.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid.remove_bucket"), mouseX, mouseY);
	}
	
	private void SetTradeType(Button button)
	{
		if(button == buttonSetSell)
			this.localDirection = TradeDirection.SALE;
		else
			this.localDirection = TradeDirection.PURCHASE;
	}
	
	private void PressSaveButton(Button button)
	{
		SaveChanges();
		PressBackButton(button);
	}
	
	protected void SaveChanges()
	{
		this.getTrader().sendPriceMessage(new TradePriceData(this.tradeIndex, this.priceInput.getCoinValue(), this.localDirection, this.localQuantity, this.localDrainable, this.localFillable));
	}
	
	private void PressBackButton(Button button)
	{
		this.getTrader().sendOpenStorageMessage();
	}
	
	private void PressToggleDrainButton(Button button)
	{
		this.localDrainable = !this.localDrainable;
	}
	
	private void PressToggleFillButton(Button button)
	{
		this.localFillable = !this.localFillable;
	}
	
	private void PressQuantityButton(Button button)
	{
		int deltaQuantity = button == this.buttonAddBucket ? 1 : -1;
		if(deltaQuantity < 0 && this.localQuantity <= 1)
			return;
		else if(deltaQuantity > 1 && this.localQuantity >= this.trade.get().getMaxBucketQuantity())
			return;
		this.localQuantity += deltaQuantity;
	}
	
	@Override
	public void OnCoinValueChanged(CoinValueInput input) { }

	@Override
	public <T extends Button> T addCustomButton(T button) {
		return super.addButton(button);
	}
	
	@Override
	public <T extends IGuiEventListener> T addCustomListener(T listener) {
		return super.addListener(listener);
	}

	@Override
	public FontRenderer getFont() {
		return this.font;
	}

	@Override
	public int getWidth() {
		return this.width;
	}
	
	private void PressTradeRuleButton(Button button)
	{
		SaveChanges();
		this.minecraft.displayGuiScreen(new TradeRuleScreen(this.getRuleScreenHandler()));
	}
	
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new CloseRuleHandler(this.trader, this.tradeIndex); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{
		final Supplier<IFluidTrader> trader;
		final int tradeIndex;
		
		public CloseRuleHandler(Supplier<IFluidTrader> trader, int tradeIndex)
		{
			this.trader = trader;
			this.tradeIndex = tradeIndex;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.trader.get().getTrade(this.tradeIndex); }
		
		public void reopenLastScreen() {
			Minecraft.getInstance().displayGuiScreen(new TradeFluidPriceScreen(this.trader, this.tradeIndex));
		}
		
		public void updateServer(ResourceLocation type, CompoundNBT updateInfo)
		{
			this.trader.get().sendUpdateTradeRuleMessage(this.tradeIndex, type, updateInfo);
		}
		
	}
	
	public static class TradePriceData
	{
		
		public final int tradeIndex;
		public final CoinValue cost;
		public final TradeDirection type;
		public final int quantity;
		public final boolean canDrain;
		public final boolean canFill;
		
		public TradePriceData(int tradeIndex, CoinValue cost, TradeDirection type, int quantity, boolean canDrain, boolean canFill)
		{
			this.tradeIndex = tradeIndex;
			this.cost = cost;
			this.type = type;
			this.quantity = quantity;
			this.canDrain = canDrain;
			this.canFill = canFill;
		}
	}

	
}
