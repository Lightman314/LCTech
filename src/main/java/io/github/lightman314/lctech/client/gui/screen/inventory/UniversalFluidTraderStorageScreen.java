package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.TradeFluidPriceScreen;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.container.UniversalFluidTraderStorageContainer;
import io.github.lightman314.lctech.network.LCTechPacketHandler;
import io.github.lightman314.lctech.network.messages.fluid_trader.MessageFluidTradeTankInteraction;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.MessageSetFluidTradeProduct2;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.MessageToggleFluidIcon2;
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class UniversalFluidTraderStorageScreen extends AbstractContainerScreen<UniversalFluidTraderStorageContainer>{

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
	EditBox allyTextInput;
	
	List<Button> tradePriceButtons = Lists.newArrayList();
	
	public UniversalFluidTraderStorageScreen(UniversalFluidTraderStorageContainer container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.imageWidth = FluidTraderUtil.getWidth(this.menu.getData()) + 64;
		this.imageHeight= 100 + FluidTraderUtil.getTradeDisplayHeight(this.menu.getData());
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int x, int y) {
		
		FluidTraderStorageScreen.drawTraderStorageBackground(poseStack, this, this.font, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.getData());
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		FluidTraderStorageScreen.drawTraderStorageForeground(poseStack, this.font, this.menu.getData(), this.imageHeight, this.menu.getData().getName(), this.playerInventoryTitle);
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int traderOffset = FluidTraderUtil.getTradeDisplayOffset(this.menu.getData()) + 32;
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.menu.getData()) + 32;
		
		this.buttonShowTrades = this.addRenderableWidget(new IconButton(this.leftPos + traderOffset - 20, this.topPos, this::PressTradesButton, GUI_TEXTURE, 176, 0));
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos + traderOffset - 20, this.topPos + 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = !this.menu.getData().isCreative() && this.menu.isOwner();
		
		this.buttonStoreMoney = this.addRenderableWidget(new IconButton(this.leftPos + inventoryOffset + 176 + 32, this.topPos + FluidTraderUtil.getTradeDisplayHeight(this.menu.getData()), this::PressStoreCoinsButton, GUI_TEXTURE, 176, 16));
		this.buttonStoreMoney.visible = false;
		
		this.buttonChangeName = this.addRenderableWidget(new Button(this.leftPos + traderOffset, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.changename"), this::PressTraderNameButton));
		this.buttonChangeName.visible = this.menu.isOwner();
		this.buttonShowLog = this.addRenderableWidget(new Button(this.leftPos + traderOffset + 20, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addRenderableWidget(new Button(this.leftPos + traderOffset + 40, this.topPos - 20, 20, 20, new TranslatableComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		
		int tradeWindowWidth = FluidTraderUtil.getTradeDisplayWidth(this.menu.getData());
		this.buttonToggleCreative = this.addRenderableWidget(new IconButton(this.leftPos + traderOffset + tradeWindowWidth - 40, this.topPos - 20, this::PressCreativeButton, GUI_TEXTURE, 176 + 32, 0));
		this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.menu.player);
		this.buttonAddTrade = this.addRenderableWidget(new PlainButton(this.leftPos + traderOffset + tradeWindowWidth - 50, this.topPos - 20, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64,0));
		this.buttonAddTrade.visible = this.menu.getData().isCreative() && TradingOffice.isAdminPlayer(this.menu.player);
		this.buttonAddTrade.active = this.menu.getData().getTradeCount() < FluidTraderTileEntity.TRADE_LIMIT;
		this.buttonRemoveTrade = this.addRenderableWidget(new PlainButton(this.leftPos + traderOffset + tradeWindowWidth - 50, this.topPos - 10, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 176 + 64, 20));
		this.buttonAddTrade.visible = this.menu.getData().isCreative() && TradingOffice.isAdminPlayer(this.menu.player);
		this.buttonAddTrade.active = this.menu.getData().getTradeCount() > 1;
		
		if(this.menu.isOwner())
		{
			
			this.buttonAllies = this.addRenderableWidget(new IconButton(this.leftPos + traderOffset + 60, this.topPos - 20, this::PressAllyButton, GUI_TEXTURE, 176 + 32, 16));
			
			this.allyTextInput = this.addWidget(new EditBox(this.font, this.leftPos + this.imageWidth / 2 - 176 / 2 + 10, this.topPos + 9, 176 - 20, 20, new TextComponent("")));
			this.allyTextInput.setMaxLength(32);
			this.allyTextInput.visible = false;
			
			this.buttonAddAlly = this.addRenderableWidget(new Button(this.leftPos + this.imageWidth/2 - 176/2 + 10, this.topPos + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.allies.add"), this::PressAddAllyButton));
			this.buttonAddAlly.visible = false;
			this.buttonRemoveAlly = this.addRenderableWidget(new Button(this.leftPos + this.imageWidth/2 - 176/2 + 88, this.topPos + 30, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.allies.remove"), this::PressRemoveAllyButton));
			this.buttonRemoveAlly.visible = false;
			
		}
		
		this.buttonTradeRules = this.addRenderableWidget(new IconButton(this.leftPos + traderOffset + tradeWindowWidth - 20, this.topPos - 20, this::PressTradeRulesButton, GUI_TEXTURE, 176 + 16, 16));
		
		this.logWindow = this.addWidget(new TextLogWindow(this.leftPos + this.imageWidth/2 - TextLogWindow.WIDTH/2, this.topPos, () -> this.menu.getData().getLogger(), this.font));
		this.logWindow.visible = false;
		
		for(int i = 0; i < this.menu.getData().getTradeCount(); i++)
		{
			this.tradePriceButtons.add(this.addRenderableWidget(new PlainButton(this.leftPos + FluidTraderUtil.getPriceButtonPosX(this.menu.getData(), i) + 32, this.topPos + FluidTraderUtil.getPriceButtonPosY(this.menu.getData(), i), 10, 10, this::PressTradePriceButton, GUI_TEXTURE, 176 + 64, 40)));
		}
		
		tick();
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		if(this.logWindow.visible)
		{
			this.logWindow.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonShowLog.isMouseOver(mouseX,  mouseY))
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.hide"), mouseX, mouseY);
			else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
			return;
		}
		else if(this.allyScreenOpen)
		{
			ItemTraderStorageScreen.drawAllyScreen(matrixStack, this, this.font, this.menu.getData(), minecraft, this.imageWidth, this.height);
			
			this.allyTextInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonAddAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonRemoveAlly.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonAllies.isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
			this.buttonAllies.render(matrixStack, mouseX, mouseY, partialTicks);
			return;
		}
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
		
		if(this.buttonShowTrades.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.opentrades"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.getData().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonToggleCreative.visible && this.buttonToggleCreative.isMouseOver(mouseX, mouseY))
		{
			if(this.menu.getData().isCreative())
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.disable"), mouseX, mouseY);
			else
				this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.enable"), mouseX, mouseY);
		}
		else if(this.buttonAddTrade.visible && this.buttonAddTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
		}
		else if(this.buttonRemoveTrade.visible && this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
		}
		else if(this.buttonChangeName.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.changeName"), mouseX, mouseY);
		}
		else if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.show"), mouseX, mouseY);
		}
		else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
		}
		else if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		else if(this.buttonAllies != null && this.buttonAllies.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.allies"), mouseX, mouseY);
		}
		else
		{
			UniversalFluidTraderData data = this.menu.getData();
			for(int i = 0; i < data.getTradeCount(); i++)
			{
				int result = FluidTradeButton.tryRenderTooltip(matrixStack, this, i, data, this.leftPos + FluidTraderUtil.getButtonPosX(data, i) + 32, this.topPos + FluidTraderUtil.getButtonPosY(data, i), mouseX, mouseY, null, true);
				if(result == -2 && this.menu.getCarried().isEmpty())
					this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lctech.trader.fluid_edit"), mouseX, mouseY);
			}
		}
		
	}
	
	public void containerTick()
	{
		if(!this.menu.hasPermissions())
		{
			this.PressTradesButton(this.buttonShowTrades);
			return;
		}
		
		this.menu.tick();
		
		this.buttonCollectMoney.visible = (!this.menu.getData().isCreative() || this.menu.getData().getStoredMoney().getRawValue() > 0) && this.menu.isOwner();
		this.buttonCollectMoney.active = this.menu.getData().getStoredMoney().getRawValue() > 0;
		
		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd();
		this.buttonClearLog.visible = this.menu.getData().getLogger().logText.size() > 0 && this.menu.isOwner();
		
		if(this.menu.isOwner())
		{
			this.buttonToggleCreative.visible = TradingOffice.isAdminPlayer(this.menu.player);
			if(this.buttonToggleCreative.visible)
			{
				if(this.menu.getData().isCreative())
				{
					this.buttonToggleCreative.setResource(GUI_TEXTURE, 176 + 32, 0);
					this.buttonAddTrade.visible = true;
					this.buttonAddTrade.active = this.menu.getData().getTradeCount() < FluidTraderTileEntity.TRADE_LIMIT;
					this.buttonRemoveTrade.visible = true;
					this.buttonRemoveTrade.active = this.menu.getData().getTradeCount() > 1;
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
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if(this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.allyScreenOpen)
			return false;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	//0 for left-click. 1 for right click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		ItemStack heldItem = this.menu.getCarried();
		int tradeCount = this.menu.getData().getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			FluidTradeData trade = this.menu.getData().getTrade(i);
			int buttonX = this.leftPos + FluidTraderUtil.getButtonPosX(this.menu.getData(), i) + 32;
			int buttonY = this.topPos + FluidTraderUtil.getButtonPosY(this.menu.getData(), i);
			//Interact with the bucket/product
			if(FluidTradeButton.isMouseOverBucket(buttonX, buttonY, (int)mouseX, (int)mouseY))
			{
				FluidStack currentProduct = trade.getProduct();
				if(heldItem.isEmpty() && currentProduct.isEmpty())
				{
					//Open the fluid edit screen if both the sell item and held items are empty
					this.menu.openFluidEditScreenForTrade(i);
					return true;
				}
				else if(heldItem.isEmpty())
				{
					//If held item is empty, set the product to empty
					trade.setProduct(FluidStack.EMPTY);
					LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeProduct2(this.menu.getData().getTraderID(), i, FluidStack.EMPTY));
					return true;
				}
				else
				{
					//If the held item is not empty, set the product to the fluid in the players hand
					final int index = i;
					AtomicBoolean consume = new AtomicBoolean(false);
					FluidUtil.getFluidContained(heldItem).ifPresent(fluid->{
						trade.setProduct(fluid);
						LCTechPacketHandler.instance.sendToServer(new MessageSetFluidTradeProduct2(this.menu.getData().getTraderID(), index, fluid));
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
					this.menu.PlayerTankInteraction(i);
					LCTechPacketHandler.instance.sendToServer(new MessageFluidTradeTankInteraction(i));
					return true;
				}
			}
			//Interact with the drain/fill buttons
			for(int icon = 1; icon <= 1; icon++)
			{
				if(FluidTradeButton.isMouseOverIcon(icon, buttonX, buttonY, (int)mouseX, (int)mouseY))
				{
					LCTechPacketHandler.instance.sendToServer(new MessageToggleFluidIcon2(this.menu.traderID, i, icon));
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void PressTradesButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.menu.getData().getTraderID()));
	}
	
	private void PressCollectionButton(Button button)
	{
		if(menu.isOwner())
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
		
		this.minecraft.setScreen(new TradeFluidPriceScreen(() -> this.menu.getData(), tradeIndex, this.menu.player,
				TradeFluidPriceScreen.SAVEDATA_UNIVERSAL(this.menu.getData()),
				TradeFluidPriceScreen.OPENSTORAGE_UNIVERSAL(this.menu.getData()),
				TradeFluidPriceScreen.UPDATETRADERULES_UNIVERSAL(this.menu.getData(), tradeIndex)));
	}
	
	private void PressTraderNameButton(Button button)
	{
		this.minecraft.setScreen(new UniversalTraderNameScreen(this.menu.getData(), this.menu.player));
	}
	
	private void PressCreativeButton(Button button)
	{
		if(menu.isOwner())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageToggleCreative());
	}
	
	private void PressAddRemoveTradeButton(Button button)
	{
		if(menu.isOwner())
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
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearUniversalLogger(this.menu.getData().getTraderID()));
	}
	
	private void PressTradeRulesButton(Button button)
	{
		this.minecraft.setScreen(new TradeRuleScreen(this.menu.getData().GetRuleScreenHandler()));
	}
	
	private void PressAllyButton(Button button)
	{
		this.allyScreenOpen = !this.allyScreenOpen;
	}
	
	private void PressAddAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getValue();
		this.allyTextInput.setValue("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly2(this.menu.getData().getTraderID(), true, newAlly));
	}
	
	private void PressRemoveAllyButton(Button button)
	{
		String newAlly = this.allyTextInput.getValue();
		this.allyTextInput.setValue("");
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveAlly2(this.menu.getData().getTraderID(), false, newAlly));
	}
	
	
}
