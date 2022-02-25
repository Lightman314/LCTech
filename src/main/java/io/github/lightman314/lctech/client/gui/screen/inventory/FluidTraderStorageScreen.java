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
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.permissions.FluidPermissions;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
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

public class FluidTraderStorageScreen extends ContainerScreen<FluidTraderStorageContainer>{

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
	
	public FluidTraderStorageScreen(FluidTraderStorageContainer container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.xSize = FluidTraderUtil.getWidth(this.container.getTrader()) + 64;
		this.ySize = 100 + FluidTraderUtil.getTradeDisplayHeight(this.container.getTrader());
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
		
		IFluidTrader trader = this.container.getTrader();
		
		if(trader == null)
			return;
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = this.guiLeft;
		int startY = this.guiTop;
		
		int columnCount = FluidTraderUtil.getTradeDisplayColumnCount(this.container.getTrader());
		int rowCount = FluidTraderUtil.getTradeDisplayRowCount(this.container.getTrader());
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(this.container.getTrader()) + 32;
		
		//Top-left corner
		this.blit(matrix, startX + tradeOffset, startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			this.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, FluidTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				this.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY, 6 + FluidTradeButton.WIDTH, 0, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		this.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 75, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			this.blit(matrix, startX + tradeOffset, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 0, 17, 6, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				this.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 17, FluidTradeButton.WIDTH, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					this.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6 + FluidTradeButton.WIDTH, 17, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			this.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 75, 17, 6, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		this.blit(matrix, startX + tradeOffset, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 0, 104, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			this.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 104, FluidTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				this.blit(matrix, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 104, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		this.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 75, 104, 6, 7);
		
		//Draw the bottom (player inventory & coin input slots)
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(trader) + 32;
		int tradeHeight = FluidTraderUtil.getTradeDisplayHeight(trader);
		this.blit(matrix, startX + inventoryOffset, startY + tradeHeight, 0, 111, 176 + 32, 100);
		//Draw the upgrade slots
		this.blit(matrix, startX + inventoryOffset - 32, startY + tradeHeight, 176, 111, 32, 100);
		
		//Draw the fake fluid trade buttons
		for(int i = 0; i < trader.getTradeCount(); i++)
		{
			FluidTradeButton.renderFluidTradeButton(matrix, this, font, startX + FluidTraderUtil.getButtonPosX(trader, i) + 32, startY + FluidTraderUtil.getButtonPosY(trader, i), i, trader, false, true, true);
		}
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		IFluidTrader trader = this.container.getTrader();
		
		if(trader == null)
			return;

		font.drawString(matrix, trader.getTitle().getString(), 8.0f + FluidTraderUtil.getTradeDisplayOffset(trader) + 32, 6.0f, 0x404040);
		
		font.drawString(matrix, this.playerInventory.getDisplayName().getString(), FluidTraderUtil.getInventoryDisplayOffset(trader) + 8.0f + 32, ySize - 94, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		IFluidTrader trader = this.container.getTrader();
		
		int traderOffset = FluidTraderUtil.getTradeDisplayOffset(trader) + 32;
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(trader) + 32;
		
		this.buttonShowTrades = this.addButton(IconAndButtonUtil.traderButton(this.guiLeft + traderOffset - 20, this.guiTop, this::PressTradesButton));
		this.buttonCollectMoney = this.addButton(IconAndButtonUtil.collectCoinButton(this.guiLeft + traderOffset - 20, this.guiTop + 20, this::PressCollectionButton, this.container::getTrader));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.container.hasPermission(Permissions.COLLECT_COINS) && !trader.getCoreSettings().hasBankAccount();
		
		this.buttonShowLog = this.addButton(IconAndButtonUtil.showLoggerButton(this.guiLeft + traderOffset, this.guiTop - 20, this::PressLogButton, () -> this.logWindow.visible));
		this.buttonClearLog = this.addButton(IconAndButtonUtil.clearLoggerButton(this.guiLeft + traderOffset + 20, this.guiTop - 20, this::PressClearLogButton));
		
		this.buttonStoreMoney = this.addButton(IconAndButtonUtil.storeCoinButton(this.guiLeft + inventoryOffset + 176 + 32, this.guiTop + FluidTraderUtil.getTradeDisplayHeight(trader), this::PressStoreCoinsButton));
		this.buttonStoreMoney.visible = false;
		
		int tradeWindowWidth = FluidTraderUtil.getTradeDisplayWidth(trader);
		
		this.buttonOpenSettings = this.addButton(IconAndButtonUtil.openSettingsButton(this.guiLeft + traderOffset + tradeWindowWidth - 20, this.guiTop - 20, this::PressSettingsButton));
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addButton(IconAndButtonUtil.tradeRuleButton(this.guiLeft + traderOffset + tradeWindowWidth - 40, this.guiTop - 20, this::PressTradeRulesButton));
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.logWindow = this.addListener(IconAndButtonUtil.traderLogWindow(this, this.container::getTrader));
		this.logWindow.visible = false;
		
		for(int i = 0; i < trader.getTradeCount(); i++)
		{
			this.tradePriceButtons.add(this.addButton(new PlainButton(this.guiLeft + FluidTraderUtil.getPriceButtonPosX(trader, i) + 32, this.guiTop + FluidTraderUtil.getPriceButtonPosY(trader, i), 10, 10, this::PressTradePriceButton, GUI_TEXTURE, 176 + 64, 40)));
		}
		
		tick();
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		IFluidTrader trader = this.container.getTrader();
		
		if(trader == null)
		{
			this.container.player.closeScreen();
			return;
		}
		
		this.renderBackground(matrixStack);
		if(this.logWindow.visible)
		{
			this.logWindow.render(matrixStack, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(matrixStack, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(matrixStack, mouseX, mouseY, partialTicks);
			IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, Lists.newArrayList(this.buttonShowLog, this.buttonClearLog));
			return;
		}
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(matrixStack, mouseX, mouseY, this.buttons);

		for(int i = 0; i < trader.getTradeCount(); i++)
		{
			int result = FluidTradeButton.tryRenderTooltip(matrixStack, this, i, trader, this.guiLeft + FluidTraderUtil.getButtonPosX(trader, i) + 32, this.guiTop + FluidTraderUtil.getButtonPosY(trader, i), mouseX, mouseY, true);
			if(result == -2 && this.container.player.inventory.getItemStack().isEmpty() && this.container.hasPermission(Permissions.EDIT_TRADES))
				this.renderTooltip(matrixStack, new TranslationTextComponent("tooltip.lctech.trader.fluid_edit"), mouseX, mouseY);
		}
		
	}
	
	public void tick()
	{
		
		IFluidTrader trader = this.container.getTrader();
		
		if(trader == null)
		{
			this.container.player.closeScreen();
			return;
		}
		
		if(!this.container.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.PressTradesButton(this.buttonShowTrades);
			return;
		}
		super.tick();
		
		this.buttonCollectMoney.visible = (!trader.getCoreSettings().isCreative() || trader.getStoredMoney().getRawValue() > 0) && this.container.hasPermission(Permissions.COLLECT_COINS) && !trader.getCoreSettings().hasBankAccount();
		this.buttonCollectMoney.active = trader.getStoredMoney().getRawValue() > 0;
		
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd();
		this.buttonClearLog.visible = trader.getLogger().logText.size() > 0 && this.container.hasPermission(Permissions.CLEAR_LOGS);
		
		boolean visible = this.container.hasPermission(Permissions.EDIT_TRADES);
		this.tradePriceButtons.forEach(button -> button.visible = visible);
		
	}
	
	//0 for left-click. 1 for right click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		IFluidTrader trader = this.container.getTrader();
		ItemStack heldItem = this.container.player.inventory.getItemStack();
		int tradeCount = trader.getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			FluidTradeData trade = trader.getTrade(i);
			int buttonX = this.guiLeft + FluidTraderUtil.getButtonPosX(trader, i) + 32;
			int buttonY = this.guiTop + FluidTraderUtil.getButtonPosY(trader, i);
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
					trader.sendSetTradeFluidMessage(i, FluidStack.EMPTY);
					return true;
				}
				else
				{
					//If the held item is not empty, set the product to the fluid in the players hand
					final int index = i;
					AtomicBoolean consume = new AtomicBoolean(false);
					FluidUtil.getFluidContained(heldItem).ifPresent(fluid->{
						trade.setProduct(fluid);
						trader.sendSetTradeFluidMessage(index, fluid);
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
			if(this.container.hasPermission(FluidPermissions.EDIT_DRAINABILITY) && trader.drainCapable())
			{
				//Interact with the drain/fill buttons
				for(int icon = 0; icon <= 1; icon++)
				{
					if(FluidTradeButton.isMouseOverIcon(icon, buttonX, buttonY, (int)mouseX, (int)mouseY))
					{
						trader.sendToggleIconMessage(i, icon);
					}
				}
			}
			
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void PressTradesButton(Button button)
	{
		this.container.getTrader().sendOpenTraderMessage();
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
		
		this.minecraft.displayGuiScreen(new TradeFluidPriceScreen(this.container::getTrader, tradeIndex));
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		this.container.getTrader().sendClearLogMessage();
	}
	
	private void PressTradeRulesButton(Button button)
	{
		this.minecraft.displayGuiScreen(new TradeRuleScreen(this.container.getTrader().getRuleScreenHandler()));
	}
	
	private void PressSettingsButton(Button button)
	{
		this.container.player.closeScreen();
		this.minecraft.displayGuiScreen(new TraderSettingsScreen(this.container::getTrader, (player) -> this.container.getTrader().sendOpenStorageMessage()));
	}
	
}
