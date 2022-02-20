package io.github.lightman314.lctech.client.gui.screen;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeDirection;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TradeEnergyPriceScreen extends Screen implements ICoinValueInput{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/energytradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	PlayerEntity player;
	Supplier<IEnergyTrader> trader;
	Supplier<EnergyTradeData> trade;
	int tradeIndex;
	TradeDirection localDirection;
	int localAmount;
	
	Button buttonSetSell;
	Button buttonSetPurchase;
	
	Button buttonTradeRules;
	
	TextFieldWidget amountInput;
	
	CoinValueInput priceInput;
	
	public TradeEnergyPriceScreen(Supplier<IEnergyTrader> trader, int tradeIndex) {
		super(new TranslationTextComponent("gui.lightmanscurrency.changeprice"));
		this.trader = trader;
		this.tradeIndex = tradeIndex;
		this.trade = () -> this.trader.get().getTrade(this.tradeIndex);
		//Store local copies of togglable data
		EnergyTradeData localTrade = this.trade.get();
		this.localDirection = localTrade.getTradeDirection();
		this.localAmount = localTrade.getAmount();
	}
	
	protected void init()
	{
		
		super.init();
		
		this.player = this.minecraft.player;
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.priceInput = this.addButton(new CoinValueInput(guiTop, this.title, this.trade.get().getCost(), this));
		//this.priceInput.init();
		
		this.buttonSetSell = this.addButton(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.tradedirection.sale"), this::SetTradeType));
		this.buttonSetPurchase = this.addButton(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 6, 50, 21, new TranslationTextComponent("gui.button.lightmanscurrency.tradedirection.purchase"), this::SetTradeType));
		
		
		this.addButton(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.save"), this::PressSaveButton));
		this.addButton(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.back"), this::PressBackButton));
		//this.addButton(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 62, 51, 20, new TranslationTextComponent("gui.button.lightmanscurrency.free"), this::PressFreeButton));
		this.buttonTradeRules = this.addButton(IconAndButtonUtil.tradeRuleButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton));
		this.buttonTradeRules.visible = this.trader.get().getCoreSettings().hasPermission(this.player, Permissions.EDIT_TRADE_RULES);
		
		this.amountInput = this.addListener(new TextFieldWidget(this.font, guiLeft + 20, guiTop + CoinValueInput.HEIGHT + 30, 120, 20, new StringTextComponent("")));
		this.amountInput.setText("" + this.localAmount);
		
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
		
		this.buttonTradeRules.visible = this.trader.get().hasPermission(this.player, Permissions.EDIT_TRADE_RULES);
		
		TextInputUtil.whitelistInteger(this.amountInput, 0, this.trader.get().getMaxEnergy());
		
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
		
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY + CoinValueInput.HEIGHT, 0, 0, this.xSize, this.ySize - CoinValueInput.HEIGHT);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		//Render the amount input
		this.amountInput.render(matrixStack, mouseX, mouseY, partialTicks);
		//Render the energy unit on the right of the amount input
		this.font.drawString(matrixStack, EnergyUtil.ENERGY_UNIT, startX + 144, startY + CoinValueInput.HEIGHT + 34, 0xFFFFFF);
		
		//Mouse over for buttons
		IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, this.buttons);
		
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
		this.trader.get().sendPriceMessage(new TradePriceData(this.tradeIndex, this.priceInput.getCoinValue(), this.localDirection, TextInputUtil.getIntegerValue(this.amountInput, 0)));
	}
	
	private void PressBackButton(Button button)
	{
		this.trader.get().sendOpenStorageMessage();
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
		this.minecraft.displayGuiScreen(new TradeRuleScreen(GetRuleScreenBackHandler()));
	}
	
	public ITradeRuleScreenHandler GetRuleScreenBackHandler() { return new CloseRuleHandler(this.trader, this.tradeIndex); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{
		final Supplier<IEnergyTrader> trader;
		final int tradeIndex;
		
		public CloseRuleHandler(Supplier<IEnergyTrader> trader, int tradeIndex)
		{
			this.trader = trader;
			this.tradeIndex = tradeIndex;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.trader.get().getTrade(this.tradeIndex); }
		
		public void reopenLastScreen() {
			Minecraft.getInstance().displayGuiScreen(new TradeEnergyPriceScreen(this.trader, this.tradeIndex));
		}
		
		public void updateServer(List<TradeRule> newRules)
		{
			this.trader.get().sendUpdateTradeRuleMessage(newRules);
		}
		
	}
	
	public static class TradePriceData
	{
		
		public final int tradeIndex;
		public final CoinValue cost;
		public final TradeDirection type;
		public final int amount;
		
		public TradePriceData(int tradeIndex, CoinValue cost, TradeDirection type, int amount)
		{
			this.tradeIndex = tradeIndex;
			this.cost = cost;
			this.type = type;
			this.amount = amount;
		}
	}

	
}
