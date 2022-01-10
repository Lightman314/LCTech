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
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.MessageToggleFluidIcon2;
import io.github.lightman314.lctech.trader.permissions.FluidPermissions;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemTraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearUniversalLogger;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class UniversalFluidTraderStorageScreen extends ContainerScreen<UniversalFluidTraderStorageContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader_storage.png");
	public static final ResourceLocation ALLY_GUI_TEXTURE = TradeRuleScreen.GUI_TEXTURE;
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	
	Button buttonOpenSettings;
	
	Button buttonStoreMoney;
	
	Button buttonShowLog;
	Button buttonClearLog;
	
	TextLogWindow logWindow;
	
	Button buttonTradeRules;
	
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
		
		this.buttonShowTrades = this.addButton(new IconButton(this.guiLeft + traderOffset - 20, this.guiTop, this::PressTradesButton, this.font, IconData.of(GUI_TEXTURE, 176, 0)));
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft + traderOffset - 20, this.guiTop + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.container.hasPermission(Permissions.COLLECT_COINS);
		
		this.buttonShowLog = this.addButton(new Button(this.guiLeft + traderOffset, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.showlog"), this::PressLogButton));
		this.buttonClearLog = this.addButton(new Button(this.guiLeft + traderOffset + 20, this.guiTop - 20, 20, 20, new TranslationTextComponent("gui.button.lightmanscurrency.clearlog"), this::PressClearLogButton));
		
		this.buttonStoreMoney = this.addButton(new IconButton(this.guiLeft + inventoryOffset + 176 + 32, this.guiTop + FluidTraderUtil.getTradeDisplayHeight(this.container.getData()), this::PressStoreCoinsButton, this.font, IconData.of(GUI_TEXTURE, 176, 16)));
		this.buttonStoreMoney.visible = false;
		
		int tradeWindowWidth = FluidTraderUtil.getTradeDisplayWidth(this.container.getData());
		
		this.buttonOpenSettings = this.addButton(new IconButton(this.guiLeft + traderOffset + tradeWindowWidth - 20, this.guiTop - 20, this::PressSettingsButton, this.font, IconData.of(ItemTraderStorageScreen.GUI_TEXTURE, 176 + 32, 0)));
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addButton(new IconButton(this.guiLeft + traderOffset + tradeWindowWidth - 40, this.guiTop - 20, this::PressTradeRulesButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 16)));
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
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
		else if(this.buttonOpenSettings.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lightmanscurrency.trader.settings"), mouseX, mouseY);
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
		if(!this.container.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.PressTradesButton(this.buttonShowTrades);
			return;
		}
		super.tick();
		
		this.container.tick();
		
		this.buttonCollectMoney.visible = (!this.container.getData().getCoreSettings().isCreative() || this.container.getData().getStoredMoney().getRawValue() > 0) && this.container.hasPermission(Permissions.COLLECT_COINS);
		this.buttonCollectMoney.active = this.container.getData().getStoredMoney().getRawValue() > 0;
		
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd();
		this.buttonClearLog.visible = this.container.getData().getLogger().logText.size() > 0 && this.container.hasPermission(Permissions.CLEAR_LOGS);
		
		boolean visible = this.container.hasPermission(Permissions.EDIT_TRADES);
		this.tradePriceButtons.forEach(button -> button.visible = visible);
		
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
			if(FluidTradeButton.isMouseOverBucket(buttonX, buttonY, (int)mouseX, (int)mouseY) && this.container.hasPermission(Permissions.EDIT_TRADES))
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
			if(this.container.hasPermission(FluidPermissions.EDIT_DRAINABILITY))
			{
				//Interact with the drain/fill buttons
				for(int icon = 1; icon <= 1; icon++)
				{
					if(FluidTradeButton.isMouseOverIcon(icon, buttonX, buttonY, (int)mouseX, (int)mouseY))
					{
						LCTechPacketHandler.instance.sendToServer(new MessageToggleFluidIcon2(this.container.traderID, i, icon));
					}
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
		if(this.container.hasPermission(Permissions.COLLECT_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressStoreCoinsButton(Button button)
	{
		if(this.container.hasPermission(Permissions.STORE_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
	}
	
	private void PressTradePriceButton(Button button)
	{
		if(!this.container.hasPermission(Permissions.EDIT_TRADES))
			return;
		int tradeIndex = 0;
		if(tradePriceButtons.contains(button))
			tradeIndex = tradePriceButtons.indexOf(button);
		
		this.minecraft.displayGuiScreen(new TradeFluidPriceScreen(() -> this.container.getData(), tradeIndex, this.playerInventory.player,
				TradeFluidPriceScreen.SAVEDATA_UNIVERSAL(this.container.getData()),
				TradeFluidPriceScreen.OPENSTORAGE_UNIVERSAL(this.container.getData()),
				TradeFluidPriceScreen.UPDATETRADERULES_UNIVERSAL(this.container.getData(), tradeIndex)));
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
	
	private void PressSettingsButton(Button button)
	{
		this.container.player.closeScreen();
		this.minecraft.displayGuiScreen(new TraderSettingsScreen(() -> ClientTradingOffice.getData(this.container.traderID), (player) -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.container.traderID))));
	}
	
	
}
