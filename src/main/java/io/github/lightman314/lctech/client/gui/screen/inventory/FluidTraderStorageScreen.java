package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.TradeFluidPriceScreen;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.container.FluidTraderStorageContainer;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidTradeTankInteraction;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageSetFluidTradeProduct;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageToggleFluidIcon;
import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderNameScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemTraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveAlly;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageToggleCreative;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidTraderStorageScreen extends ContainerScreen<FluidTraderStorageContainer>{

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
	
	public FluidTraderStorageScreen(FluidTraderStorageContainer container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.xSize = FluidTraderUtil.getWidth(this.container.tileEntity) + 64;
		this.ySize = 100 + FluidTraderUtil.getTradeDisplayHeight(this.container.tileEntity);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		
		drawTraderStorageBackground(matrixStack, this, this.font, this.container, this.minecraft, this.xSize, this.ySize, this.container.tileEntity);
		
	}
	
	@SuppressWarnings("deprecation")
	public static void drawTraderStorageBackground(MatrixStack matrix, Screen screen, FontRenderer font, Container container, Minecraft minecraft, int xSize, int ySize, IFluidTrader trader)
	{
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (screen.width - xSize)/2;
		int startY = (screen.height - ySize)/2;
		
		int columnCount = FluidTraderUtil.getTradeDisplayColumnCount(trader);
		int rowCount = FluidTraderUtil.getTradeDisplayRowCount(trader);
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader) + 32;
		
		//Top-left corner
		screen.blit(matrix, startX + tradeOffset, startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			screen.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, FluidTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				screen.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY, 6 + FluidTradeButton.WIDTH, 0, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		screen.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 75, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			screen.blit(matrix, startX + tradeOffset, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 0, 17, 6, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				screen.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 17, FluidTradeButton.WIDTH, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					screen.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6 + FluidTradeButton.WIDTH, 17, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			screen.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 75, 17, 6, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		screen.blit(matrix, startX + tradeOffset, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 0, 104, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			screen.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 104, FluidTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				screen.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 104, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		screen.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 75, 104, 6, 7);
		
		//Draw the bottom (player inventory & coin input slots)
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(trader) + 32;
		int tradeHeight = FluidTraderUtil.getTradeDisplayHeight(trader);
		screen.blit(matrix, startX + inventoryOffset, startY + tradeHeight, 0, 111, 176 + 32, 100);
		//Draw the upgrade slots
		screen.blit(matrix, startX + inventoryOffset - 32, startY + tradeHeight, 176, 111, 32, 100);
		
		//Draw the fake fluid trade buttons
		for(int i = 0; i < trader.getTradeCount(); i++)
		{
			FluidTradeButton.renderFluidTradeButton(matrix, screen, font, startX + FluidTraderUtil.getButtonPosX(trader, i) + 32, startY + FluidTraderUtil.getButtonPosY(trader, i), i, trader, null, false, true, true);
		}
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		drawTraderStorageForeground(matrix, this.font, this.container.tileEntity, this.ySize, this.container.tileEntity.getName(), this.playerInventory.getDisplayName());
	}
	
	public static void drawTraderStorageForeground(MatrixStack matrix, FontRenderer font, IFluidTrader trader, int ySize, ITextComponent title, ITextComponent inventoryTitle)
	{
		
		font.drawString(matrix, title.getString(), 8.0f + FluidTraderUtil.getTradeDisplayOffset(trader) + 32, 6.0f, 0x404040);
		
		font.drawString(matrix, inventoryTitle.getString(), FluidTraderUtil.getInventoryDisplayOffset(trader) + 8.0f + 32, ySize - 94, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int traderOffset = FluidTraderUtil.getTradeDisplayOffset(this.container.tileEntity) + 32;
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.container.tileEntity) + 32;
		
		this.buttonShowTrades = this.addButton(new IconButton(this.guiLeft + traderOffset - 20, this.guiTop, this::PressTradesButton, GUI_TEXTURE, 176, 0));
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft + traderOffset - 20, this.guiTop + 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = !this.container.tileEntity.isCreative() && this.container.isOwner();
		
		this.buttonStoreMoney = this.addButton(new IconButton(this.guiLeft + inventoryOffset + 176 + 32, this.guiTop + FluidTraderUtil.getTradeDisplayHeight(this.container.tileEntity), this::PressStoreCoinsButton, GUI_TEXTURE, 176, 16));
		this.buttonStoreMoney.visible = false;
		
		this.buttonChangeName = this.addButton(new Button(this.guiLeft + traderOffset, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.changename"), this::PressTraderNameButton));
		this.buttonChangeName.visible = this.container.isOwner();
		this.buttonShowLog = this.addButton(new Button(this.guiLeft + traderOffset + 20, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addButton(new Button(this.guiLeft + traderOffset + 40, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		
		int tradeWindowWidth = FluidTraderUtil.getTradeDisplayWidth(this.container.tileEntity);
		this.buttonToggleCreative = this.addButton(new IconButton(this.guiLeft + traderOffset + tradeWindowWidth - 40, this.guiTop - 20, this::PressCreativeButton, GUI_TEXTURE, 176 + 32, 0));
		this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.container.player);
		this.buttonAddTrade = this.addButton(new PlainButton(this.guiLeft + traderOffset + tradeWindowWidth - 50, this.guiTop - 20, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64,0));
		this.buttonAddTrade.visible = this.container.tileEntity.isCreative() && TradingOffice.isAdminPlayer(this.container.player);
		this.buttonAddTrade.active = this.container.tileEntity.getTradeCount() < FluidTraderTileEntity.TRADE_LIMIT;
		this.buttonRemoveTrade = this.addButton(new PlainButton(this.guiLeft + traderOffset + tradeWindowWidth - 50, this.guiTop - 10, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 20));
		this.buttonAddTrade.visible = this.container.tileEntity.isCreative() && TradingOffice.isAdminPlayer(this.container.player);
		this.buttonAddTrade.active = this.container.tileEntity.getTradeCount() > 1;
		
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
		
		this.logWindow = this.addListener(new TextLogWindow(this.guiLeft + this.xSize/2 - TextLogWindow.WIDTH/2, this.guiTop, () -> this.container.tileEntity.getLogger(), this.font));
		this.logWindow.visible = false;
		
		for(int i = 0; i < this.container.tileEntity.getTradeCount(); i++)
		{
			this.tradePriceButtons.add(this.addButton(new PlainButton(this.guiLeft + FluidTraderUtil.getPriceButtonPosX(this.container.tileEntity, i) + 32, this.guiTop + FluidTraderUtil.getPriceButtonPosY(this.container.tileEntity, i), 10, 10, this::PressTradePriceButton, GUI_TEXTURE, 176 + 64, 40)));
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
			ItemTraderStorageScreen.drawAllyScreen(matrixStack, this, this.font, this.container.tileEntity, minecraft, this.xSize, this.ySize);
			
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
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonToggleCreative.visible && this.buttonToggleCreative.isMouseOver(mouseX, mouseY))
		{
			if(this.container.tileEntity.isCreative())
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
			FluidTraderTileEntity tileEntity = this.container.tileEntity;
			for(int i = 0; i < tileEntity.getTradeCount(); i++)
			{
				int result = FluidTradeButton.tryRenderTooltip(matrixStack, this, i, tileEntity, this.guiLeft + FluidTraderUtil.getButtonPosX(tileEntity, i) + 32, this.guiTop + FluidTraderUtil.getButtonPosY(tileEntity, i), mouseX, mouseY, null, true);
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
		
		this.buttonCollectMoney.visible = (!this.container.tileEntity.isCreative() || this.container.tileEntity.getStoredMoney().getRawValue() > 0) && this.container.isOwner();
		this.buttonCollectMoney.active = this.container.tileEntity.getStoredMoney().getRawValue() > 0;
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd();
		this.buttonClearLog.visible = this.container.tileEntity.getLogger().logText.size() > 0 && this.container.isOwner();
		
		if(this.container.isOwner())
		{
			this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.container.player);
			if(this.buttonToggleCreative.visible)
			{
				if(this.container.tileEntity.isCreative())
				{
					this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 32, 0);
					this.buttonAddTrade.visible = true;
					this.buttonAddTrade.active = this.container.tileEntity.getTradeCount() < FluidTraderTileEntity.TRADE_LIMIT;
					this.buttonRemoveTrade.visible = true;
					this.buttonRemoveTrade.active = this.container.tileEntity.getTradeCount() > 1;
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
		int tradeCount = this.container.tileEntity.getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			FluidTradeData trade = this.container.tileEntity.getTrade(i);
			int buttonX = this.guiLeft + FluidTraderUtil.getButtonPosX(this.container.tileEntity, i) + 32;
			int buttonY = this.guiTop + FluidTraderUtil.getButtonPosY(this.container.tileEntity, i);
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
					LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeProduct(this.container.tileEntity.getPos(), i, FluidStack.EMPTY));
					return true;
				}
				else
				{
					//If the held item is not empty, set the product to the fluid in the players hand
					final int index = i;
					AtomicBoolean consume = new AtomicBoolean(false);
					FluidUtil.getFluidContained(heldItem).ifPresent(fluid->{
						trade.setProduct(fluid);
						LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeProduct(this.container.tileEntity.getPos(), index, fluid));
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
			//Interact with the drain/fill buttons
			for(int icon = 0; icon <= 1; icon++)
			{
				if(FluidTradeButton.isMouseOverIcon(icon, buttonX, buttonY, (int)mouseX, (int)mouseY))
				{
					LCTechPacketHandler.instance.sendToServer(new MessageToggleFluidIcon(this.container.tileEntity.getPos(), i, icon));
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.container.tileEntity.getPos()));
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
		
		this.minecraft.displayGuiScreen(new TradeFluidPriceScreen(() -> this.container.tileEntity, tradeIndex, this.playerInventory.player,
				TradeFluidPriceScreen.SAVEDATA_TILEENTITY(this.container.tileEntity),
				TradeFluidPriceScreen.OPENSTORAGE_TILEENTITY(this.container.tileEntity),
				TradeFluidPriceScreen.UPDATETRADERULES_TILEENTITY(this.container.tileEntity, tradeIndex)));
	}
	
	private void PressTraderNameButton(Button button)
	{
		this.minecraft.displayGuiScreen(new TraderNameScreen(this.container.tileEntity, this.playerInventory.player));
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
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearLogger(this.container.tileEntity.getPos()));
	}
	
	private void PressTradeRulesButton(Button button)
	{
		this.minecraft.displayGuiScreen(new TradeRuleScreen(this.container.tileEntity.GetRuleScreenBackHandler()));
	}
	
	private void PressAllyButton(Button button)
	{
		this.allyScreenOpen = !this.allyScreenOpen;
	}
	
	private void PressAddAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getText();
		this.allyTextInput.setText("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly(this.container.tileEntity.getPos(), true, newAlly));
	}
	
	private void PressRemoveAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getText();
		this.allyTextInput.setText("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly(this.container.tileEntity.getPos(), false, newAlly));
	}
	
	
}
