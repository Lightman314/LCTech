package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.TradeFluidPriceScreen;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.container.UniversalFluidTraderStorageContainer;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidTradeTankInteraction;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.MessageSetFluidTradeProduct2;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.UniversalTraderNameScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemTraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearUniversalLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageToggleCreative;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageAddOrRemoveAlly2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class UniversalFluidTraderStorageScreen extends ContainerScreen<UniversalFluidTraderStorageContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader_storage.png");
	public static final ResourceLocation ALLY_GUI_TEXTURE = TradeRuleScreen.GUI_TEXTURE;
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	Button buttonStoreMoney;
	IconButton buttonToggleCreative;
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	Button buttonChangeName;
	
	Button buttonShowLog;
	Button buttonClearLog;
	
	TextLogWindow logWindow;
	
	Button buttonTradeRules;
	
	boolean allyScreenOpen = false;
	Button buttonAllies;
	Button buttonAddAlly;
	Button buttonRemoveAlly;
	TextFieldWidget allyTextInput;
	
	List<Button> tradePriceButtons = Lists.newArrayList();
	
	public UniversalFluidTraderStorageScreen(UniversalFluidTraderStorageContainer container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.xSize = FluidTraderUtil.getWidth(this.container.getData()) + 64;
		this.ySize = 100 + FluidTraderUtil.getTradeDisplayHeight(this.container.getData());
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		
		FluidTraderStorageScreen.drawTraderStorageBackground(matrixStack, this, this.font, this.container, this.minecraft, this.xSize, this.ySize, this.container.getData());
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		FluidTraderStorageScreen.drawTraderStorageForeground(matrix, this.font, this.container.getData(), this.ySize, this.container.getData().getName(), this.playerInventory.getDisplayName());
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int traderOffset = FluidTraderUtil.getTradeDisplayOffset(this.container.getData()) + 32;
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.container.getData()) + 32;
		
		this.buttonShowTrades = this.addButton(new IconButton(this.guiLeft + traderOffset - 20, this.guiTop, this::PressTradesButton, GUI_TEXTURE, 176, 0));
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft + traderOffset - 20, this.guiTop + 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = !this.container.getData().isCreative() && this.container.isOwner();
		
		this.buttonStoreMoney = this.addButton(new IconButton(this.guiLeft + inventoryOffset + 176 + 32, this.guiTop + FluidTraderUtil.getTradeDisplayHeight(this.container.getData()), this::PressStoreCoinsButton, GUI_TEXTURE, 176, 16));
		this.buttonStoreMoney.visible = false;
		
		this.buttonChangeName = this.addButton(new Button(this.guiLeft + traderOffset, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.changename"), this::PressTraderNameButton));
		this.buttonChangeName.visible = this.container.isOwner();
		this.buttonShowLog = this.addButton(new Button(this.guiLeft + traderOffset + 20, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addButton(new Button(this.guiLeft + traderOffset + 40, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		
		int tradeWindowWidth = FluidTraderUtil.getTradeDisplayWidth(this.container.getData());
		this.buttonToggleCreative = this.addButton(new IconButton(this.guiLeft + traderOffset + tradeWindowWidth - 40, this.guiTop - 20, this::PressCreativeButton, GUI_TEXTURE, 176 + 32, 0));
		this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.container.player);
		this.buttonAddTrade = this.addButton(new PlainButton(this.guiLeft + traderOffset + tradeWindowWidth - 50, this.guiTop - 20, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64,0));
		this.buttonAddTrade.visible = this.container.getData().isCreative() && TradingOffice.isAdminPlayer(this.container.player);
		this.buttonAddTrade.active = this.container.getData().getTradeCount() < FluidTraderTileEntity.TRADE_LIMIT;
		this.buttonRemoveTrade = this.addButton(new PlainButton(this.guiLeft + traderOffset + tradeWindowWidth - 50, this.guiTop - 10, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 20));
		this.buttonAddTrade.visible = this.container.getData().isCreative() && TradingOffice.isAdminPlayer(this.container.player);
		this.buttonAddTrade.active = this.container.getData().getTradeCount() > 1;
		
		if(this.container.isOwner())
		{
			
			this.buttonAllies = this.addButton(new IconButton(this.guiLeft + traderOffset + 60, this.guiTop - 20, this::PressAllyButton, GUI_TEXTURE, 176 + 32, 16));
			
			this.allyTextInput = this.addListener(new TextFieldWidget(this.font, this.guiLeft + this.xSize / 2 - 176 / 2 + 10, this.guiTop + 9, 176 - 20, 20, new StringTextComponent("")));
			this.allyTextInput.setMaxStringLength(32);
			this.allyTextInput.visible = false;
			
			this.buttonAddAlly = this.addButton(new Button(this.guiLeft + this.xSize/2 - 176/2 + 10, this.guiTop + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.allies.add"), this::PressAddAllyButton));
			this.buttonAddAlly.visible = false;
			this.buttonRemoveAlly = this.addButton(new Button(this.guiLeft + this.xSize/2 - 176/2 + 88, this.guiTop + 30, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.allies.remove"), this::PressRemoveAllyButton));
			this.buttonRemoveAlly.visible = false;
			
		}
		
		this.buttonTradeRules = this.addButton(new IconButton(this.guiLeft + traderOffset + tradeWindowWidth - 20, this.guiTop - 20, this::PressTradeRulesButton, GUI_TEXTURE, 176 + 16, 16));
		
		this.logWindow = this.addListener(new TextLogWindow(this.guiLeft + this.xSize/2 - TextLogWindow.WIDTH/2, this.guiTop, () -> this.container.getData().getLogger(), this.font));
		this.logWindow.visible = false;
		
		for(int i = 0; i < this.container.getData().getTradeCount(); i++)
		{
			this.tradePriceButtons.add(this.addButton(new PlainButton(this.guiLeft + FluidTraderUtil.getPriceButtonPosX(this.container.getData(), i) + 32, this.guiTop + FluidTraderUtil.getPriceButtonPosY(this.container.getData(), i), 10, 10, this::PressTradePriceButton, GUI_TEXTURE, 176 + 64, 40)));
		}
		
		tick();
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		if(this.logWindow.visible)
		{
			this.logWindow.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonShowLog.isMouseOver(mouseX,  mouseY))
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.hide"), mouseX, mouseY);
			else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
			return;
		}
		else if(this.allyScreenOpen)
		{
			ItemTraderStorageScreen.drawAllyScreen(matrixStack, this, this.font, this.container.getData(), minecraft, this.xSize, this.ySize);
			
			this.allyTextInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonAddAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonRemoveAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonAllies.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
			this.buttonAllies.render(matrixStack, mouseX, mouseY, partialTicks);
			return;
		}
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
		
		if(this.buttonShowTrades.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.opentrades"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.getData().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonToggleCreative.visible && this.buttonToggleCreative.isMouseOver(mouseX, mouseY))
		{
			if(this.container.getData().isCreative())
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.disable"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.enable"), mouseX, mouseY);
		}
		else if(this.buttonAddTrade.visible && this.buttonAddTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
		}
		else if(this.buttonRemoveTrade.visible && this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
		}
		else if(this.buttonChangeName.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.changeName"), mouseX, mouseY);
		}
		else if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.show"), mouseX, mouseY);
		}
		else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
		}
		else if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		else if(this.buttonAllies != null && this.buttonAllies.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
		}
		else
		{
			UniversalFluidTraderData data = this.container.getData();
			for(int i = 0; i < data.getTradeCount(); i++)
			{
				int result = FluidTradeButton.tryRenderTooltip(matrixStack, this, i, data, this.guiLeft + FluidTraderUtil.getButtonPosX(data, i) + 32, this.guiTop + FluidTraderUtil.getButtonPosY(data, i), mouseX, mouseY, null, true);
				if(result == -2 && this.container.player.inventory.getItemStack().isEmpty())
					this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid_edit"), mouseX, mouseY);
			}
		}
		
	}
	
	public void tick()
	{
		if(!this.container.hasPermissions())
		{
			this.PressTradesButton(this.buttonShowTrades);
			return;
		}
		super.tick();
		
		this.container.tick();
		
		this.buttonCollectMoney.visible = (!this.container.getData().isCreative() || this.container.getData().getStoredMoney().getRawValue() > 0) && this.container.isOwner();
		this.buttonCollectMoney.active = this.container.getData().getStoredMoney().getRawValue() > 0;
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd();
		this.buttonClearLog.visible = this.container.getData().getLogger().logText.size() > 0 && this.container.isOwner();
		
		if(this.container.isOwner())
		{
			this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.container.player);
			if(this.buttonToggleCreative.visible)
			{
				if(this.container.getData().isCreative())
				{
					this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 32, 0);
					this.buttonAddTrade.visible = true;
					this.buttonAddTrade.active = this.container.getData().getTradeCount() < FluidTraderTileEntity.TRADE_LIMIT;
					this.buttonRemoveTrade.visible = true;
					this.buttonRemoveTrade.active = this.container.getData().getTradeCount() > 1;
				}
				else
				{
					this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 48, 0);
					this.buttonAddTrade.visible = false;
					this.buttonRemoveTrade.visible = false;
				}
			}
			else
			{
				this.buttonAddTrade.visible = false;
				this.buttonRemoveTrade.visible = false;
			}
			
			this.buttonAddAlly.visible = this.allyScreenOpen;
			this.buttonRemoveAlly.visible = this.allyScreenOpen;
			this.allyTextInput.visible = this.allyScreenOpen;
			
		}
		
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
		if(this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey) && this.allyScreenOpen)
			return false;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	//0 for left-click. 1 for right click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		ItemStack heldItem = this.container.player.inventory.getItemStack();
		int tradeCount = this.container.getData().getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			FluidTradeData trade = this.container.getData().getTrade(i);
			int buttonX = this.guiLeft + FluidTraderUtil.getButtonPosX(this.container.getData(), i) + 32;
			int buttonY = this.guiTop + FluidTraderUtil.getButtonPosY(this.container.getData(), i);
			//Interact with the bucket/product
			if(FluidTradeButton.isMouseOverBucket(buttonX, buttonY, (int)mouseX, (int)mouseY))
			{
				FluidStack currentProduct = trade.getProduct();
				if(heldItem.isEmpty() && currentProduct.isEmpty())
				{
					//Open the fluid edit screen if both the sell item and held items are empty
					this.container.openFluidEditScreenForTrade(i);
					return true;
				}
				else if(heldItem.isEmpty())
				{
					//If held item is empty, set the product to empty
					trade.setProduct(FluidStack.EMPTY);
					LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeProduct2(this.container.getData().getTraderID(), i, FluidStack.EMPTY));
					return true;
				}
				else
				{
					//If the held item is not empty, set the product to the fluid in the players hand
					final int index = i;
					AtomicBoolean consume = new AtomicBoolean(false);
					FluidUtil.getFluidContained(heldItem).ifPresent(fluid->{
						trade.setProduct(fluid);
						LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeProduct2(this.container.getData().getTraderID(), index, fluid));
						consume.set(true);
					});
					if(consume.get())
						return true;
				}
			}
			//Interact with the tank
			else if(FluidTradeButton.isMouseOverTank(buttonX, buttonY, (int)mouseX, (int)mouseY))
			{
				if(!heldItem.isEmpty())
				{
					//Interact with the tank
					this.container.PlayerTankInteraction(i);
					LCTechPacketHandler.instance.sendToServer(new MessageFluidTradeTankInteraction(i));
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.container.getData().getTraderID()));
	}
	
	private void PressCollectionButton(Button button)
	{
		if(container.isOwner())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressStoreCoinsButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
	}
	
	private void PressTradePriceButton(Button button)
	{
		int tradeIndex = 0;
		if(tradePriceButtons.contains(button))
			tradeIndex = tradePriceButtons.indexOf(button);
		
		this.minecraft.displayGuiScreen(new TradeFluidPriceScreen(() -> this.container.getData(), tradeIndex, this.playerInventory.player,
				TradeFluidPriceScreen.SAVEDATA_UNIVERSAL(this.container.getData()),
				TradeFluidPriceScreen.OPENSTORAGE_UNIVERSAL(this.container.getData()),
				TradeFluidPriceScreen.UPDATETRADERULES_UNIVERSAL(this.container.getData(), tradeIndex)));
	}
	
	private void PressTraderNameButton(Button button)
	{
		this.minecraft.displayGuiScreen(new UniversalTraderNameScreen(this.container.getData(), this.playerInventory.player));
	}
	
	private void PressCreativeButton(Button button)
	{
		if(container.isOwner())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageToggleCreative());
	}
	
	private void PressAddRemoveTradeButton(Button button)
	{
		if(container.isOwner())
		{
			if(button == this.buttonAddTrade)
			{
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(true));
			}
			else
			{
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(false));
			}
		}
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearUniversalLogger(this.container.getData().getTraderID()));
	}
	
	private void PressTradeRulesButton(Button button)
	{
		this.minecraft.displayGuiScreen(new TradeRuleScreen(this.container.getData().GetRuleScreenHandler()));
	}
	
	private void PressAllyButton(Button button)
	{
		this.allyScreenOpen = !this.allyScreenOpen;
	}
	
	private void PressAddAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getText();
		this.allyTextInput.setText("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly2(this.container.getData().getTraderID(), true, newAlly));
	}
	
	private void PressRemoveAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getText();
		this.allyTextInput.setText("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly2(this.container.getData().getTraderID(), false, newAlly));
	}
	
	
}
