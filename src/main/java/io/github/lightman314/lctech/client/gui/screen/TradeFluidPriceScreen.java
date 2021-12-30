package io.github.lightman314.lctech.client.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageSetFluidPrice;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageSetFluidTradeRules;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.MessageSetFluidPrice2;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.MessageSetFluidTradeRules2;
import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData.FluidTradeType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TradeFluidPriceScreen extends Screen implements ICoinValueInput{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/fluidtradeprice.png");
	
	private int xSize = 176;
	private int ySize = 88 + CoinValueInput.HEIGHT;
	
	Player player;
	Supplier<IFluidTrader> trader;
	Supplier<FluidTradeData> trade;
	int tradeIndex;
	FluidTradeType localDirection;
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
	
	final Consumer<TradePriceData> saveData;
	final Consumer<Player> openStorage;
	final Consumer<List<TradeRule>> updateTradeRules;
	
	public static final Consumer<TradePriceData> SAVEDATA_TILEENTITY(BlockEntity blockEntity) { return (data) -> LCTechPacketHandler.instance.sendToServer(new MessageSetFluidPrice(blockEntity.getBlockPos(), data.tradeIndex, data.cost, data.type, data.quantity, data.canDrain, data.canFill)); }
	public static final Consumer<Player> OPENSTORAGE_TILEENTITY(BlockEntity blockEntity) { return (player) -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(blockEntity.getBlockPos())); }
	public static final Consumer<List<TradeRule>> UPDATETRADERULES_TILEENTITY(BlockEntity blockEntity, int tradeIndex) { return (newRules) -> LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeRules(blockEntity.getBlockPos(), newRules, tradeIndex)); }
	
	public static final Consumer<TradePriceData> SAVEDATA_UNIVERSAL(UniversalTraderData traderData) { return (data) -> LCTechPacketHandler.instance.sendToServer(new MessageSetFluidPrice2(traderData.getTraderID(), data.tradeIndex, data.cost, data.type, data.quantity, data.canFill)); }
	public static final Consumer<Player> OPENSTORAGE_UNIVERSAL(UniversalTraderData traderData) { return (data) -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(traderData.getTraderID())); }
	public static final Consumer<List<TradeRule>> UPDATETRADERULES_UNIVERSAL(UniversalTraderData traderData, int tradeIndex) { return (newRules) -> LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeRules2(traderData.getTraderID(), newRules, tradeIndex)); }
	
	public TradeFluidPriceScreen(Supplier<IFluidTrader> trader, int tradeIndex, Player player, 
			Consumer<TradePriceData> saveData, Consumer<Player> openStorage, Consumer<List<TradeRule>> updateTradeRules) {
		super(new TranslatableComponent("gui.lightmanscurrency.changeprice"));
		this.trader = trader;
		this.tradeIndex = tradeIndex;
		this.trade = () -> this.trader.get().getTrade(this.tradeIndex);
		this.player = player;
		//Store local copies of togglable data
		FluidTradeData localTrade = this.trade.get();
		this.localDirection = localTrade.getTradeType();
		this.localDrainable = localTrade.canDrain();
		this.localFillable = localTrade.canFill();
		this.localQuantity = localTrade.getBucketQuantity();
		
		//Save various functions
		this.saveData = saveData;
		this.openStorage = openStorage;
		this.updateTradeRules = updateTradeRules;
	}
	
	protected void init()
	{
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.priceInput = this.addRenderableWidget(new CoinValueInput(guiTop, this.title, this.trade.get().getCost(), this));
		this.priceInput.init();
		
		this.buttonSetSell = this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 6, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.sale"), this::SetTradeType));
		this.buttonSetPurchase = this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 6, 50, 21, new TranslatableComponent("gui.button.lightmanscurrency.tradedirection.purchase"), this::SetTradeType));
		
		this.buttonAddBucket = this.addRenderableWidget(new IconButton(guiLeft + 59, guiTop + CoinValueInput.HEIGHT + 6, this::PressQuantityButton, GUI_TEXTURE, this.xSize + 16, 0));
		this.buttonRemoveBucket = this.addRenderableWidget(new IconButton(guiLeft + 98, guiTop + CoinValueInput.HEIGHT + 6, this::PressQuantityButton, GUI_TEXTURE, this.xSize + 32, 0));
		
		this.addRenderableWidget(new Button(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.save"), this::PressSaveButton));
		this.addRenderableWidget(new Button(guiLeft + 120, guiTop + CoinValueInput.HEIGHT + 62, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::PressBackButton));
		//this.addButton(new Button(guiLeft + 63, guiTop + CoinValueInput.HEIGHT + 62, 51, 20, new TranslationTextComponent("gui.button.lightmanscurrency.free"), this::PressFreeButton));
		this.buttonTradeRules = this.addRenderableWidget(new IconButton(guiLeft + this.xSize, guiTop + CoinValueInput.HEIGHT, this::PressTradeRuleButton, GUI_TEXTURE, this.xSize, 0));
		
		this.buttonToggleDrainable = this.addRenderableWidget(new PlainButton(guiLeft + 7, guiTop + CoinValueInput.HEIGHT + 37, 10, 10, this::PressToggleDrainButton, GUI_TEXTURE, this.xSize, 16));
		this.buttonToggleDrainable.visible = this.trader.get().drainCapable();
		this.buttonToggleFillable = this.addRenderableWidget(new PlainButton(guiLeft + 95, guiTop + CoinValueInput.HEIGHT + 37, 10, 10, this::PressToggleFillButton, GUI_TEXTURE, this.xSize + 20, 16));
		
		tick();
		
	}

	public void tick()
	{
		
		this.buttonSetSell.active = this.localDirection != FluidTradeType.SALE;
		this.buttonSetPurchase.active = this.localDirection != FluidTradeType.PURCHASE;

		this.buttonToggleDrainable.setResource(GUI_TEXTURE, this.xSize + (this.localDrainable ? 0 : 10), 16);
		this.buttonToggleFillable.setResource(GUI_TEXTURE, this.xSize + (this.localFillable ? 20 : 30), 16);
		
		this.buttonAddBucket.active = this.localQuantity < FluidTradeData.MAX_BUCKET_QUANTITY;
		this.buttonRemoveBucket.active = this.localQuantity > 1;
		
		super.tick();
		this.priceInput.tick();
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(matrixStack);
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, startX, startY + CoinValueInput.HEIGHT, 0, 0, this.xSize, this.ySize - CoinValueInput.HEIGHT);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//Render the Drainable & Fillable text
		if(this.trader.get().drainCapable())
			this.font.draw(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drainable").setStyle(Style.EMPTY.withColor(this.localDrainable ? FluidTradeButton.ENABLED_COLOR : FluidTradeButton.DISABLED_COLOR)), startX + 18, startY + CoinValueInput.HEIGHT + 38, 0xFFFFFF);
		this.font.draw(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.fillable").setStyle(Style.EMPTY.withColor(this.localFillable ? FluidTradeButton.ENABLED_COLOR : FluidTradeButton.DISABLED_COLOR)), startX + 106, startY + CoinValueInput.HEIGHT + 38, 0xFFFFFF);
		
		//Render the local quantity text
		String quantityText = this.localQuantity + "B";
		int textWidth = this.font.width(quantityText);
		this.font.draw(matrixStack, quantityText, startX + 1 + (176 / 2) - (textWidth / 2), startY + CoinValueInput.HEIGHT + 12, 0xFFFFFF);
		
		//Mouse over for buttons
		if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		else if(this.buttonToggleDrainable.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drainable.wordy"), mouseX, mouseY);
		else if(this.buttonToggleFillable.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.fillable.wordy"), mouseX, mouseY);
		else if(this.buttonAddBucket.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid.add_bucket"), mouseX, mouseY);
		else if(this.buttonRemoveBucket.isMouseOver(mouseX, mouseY))
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid.remove_bucket"), mouseX, mouseY);
	}
	
	private void SetTradeType(Button button)
	{
		if(button == buttonSetSell)
			this.localDirection = FluidTradeType.SALE;
		else
			this.localDirection = FluidTradeType.PURCHASE;
	}
	
	private void PressSaveButton(Button button)
	{
		SaveChanges();
		PressBackButton(button);
	}
	
	protected void SaveChanges()
	{
		this.saveData.accept(new TradePriceData(this.tradeIndex, this.priceInput.getCoinValue(), this.localDirection, this.localQuantity, this.localDrainable, this.localFillable));
	}
	
	private void PressBackButton(Button button)
	{
		this.openStorage.accept(this.player);
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
		else if(deltaQuantity > 1 && this.localQuantity >= FluidTradeData.MAX_BUCKET_QUANTITY)
			return;
		this.localQuantity += deltaQuantity;
	}
	
	@Override
	public void OnCoinValueChanged(CoinValueInput input) { }

	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		return super.addRenderableWidget(button);
	}

	@Override
	public Font getFont() {
		return this.font;
	}

	@Override
	public int getWidth() {
		return this.width;
	}
	
	private void PressTradeRuleButton(Button button)
	{
		SaveChanges();
		this.minecraft.setScreen(new TradeRuleScreen(GetRuleScreenBackHandler()));
	}
	
	public ITradeRuleScreenHandler GetRuleScreenBackHandler() { return new CloseRuleHandler(this.trader, this.tradeIndex, this.player, this.saveData, this.openStorage, this.updateTradeRules); }
	
	private static class CloseRuleHandler implements ITradeRuleScreenHandler
	{
		final Supplier<IFluidTrader> trader;
		final int tradeIndex;
		final Player player;
		final Consumer<TradePriceData> saveData;
		final Consumer<Player> openStorage;
		final Consumer<List<TradeRule>> updateTradeRules;
		
		public CloseRuleHandler(Supplier<IFluidTrader> trader, int tradeIndex, Player player, Consumer<TradePriceData> saveData, Consumer<Player> openStorage, Consumer<List<TradeRule>> updateTradeRules)
		{
			this.trader = trader;
			this.tradeIndex = tradeIndex;
			this.player = player;
			this.saveData = saveData;
			this.openStorage = openStorage;
			this.updateTradeRules = updateTradeRules;
		}
		
		public ITradeRuleHandler ruleHandler() { return this.trader.get().getTrade(this.tradeIndex); }
		
		public void reopenLastScreen() {
			Minecraft.getInstance().setScreen(new TradeFluidPriceScreen(this.trader, this.tradeIndex, this.player, this.saveData, this.openStorage, this.updateTradeRules));
		}
		
		public void updateServer(List<TradeRule> newRules)
		{
			this.updateTradeRules.accept(newRules);
		}
		
	}
	
	public static class TradePriceData
	{
		
		public final int tradeIndex;
		public final CoinValue cost;
		public final FluidTradeType type;
		public final int quantity;
		public final boolean canDrain;
		public final boolean canFill;
		
		public TradePriceData(int tradeIndex, CoinValue cost, FluidTradeType type, int quantity, boolean canDrain, boolean canFill)
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
