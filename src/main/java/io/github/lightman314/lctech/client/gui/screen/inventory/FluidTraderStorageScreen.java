package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.TradeFluidPriceScreen;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.menu.FluidTraderStorageMenu;
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidTraderStorageScreen extends AbstractContainerScreen<FluidTraderStorageMenu>{

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
	
	public FluidTraderStorageScreen(FluidTraderStorageMenu container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.imageWidth = FluidTraderUtil.getWidth(this.menu.getTrader()) + 64;
		this.imageHeight = 100 + FluidTraderUtil.getTradeDisplayHeight(this.menu.getTrader());
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		
		IFluidTrader trader = this.menu.getTrader();
		
		if(trader == null)
			return;
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		int startX = this.leftPos;
		int startY = this.topPos;
		
		int columnCount = FluidTraderUtil.getTradeDisplayColumnCount(trader);
		int rowCount = FluidTraderUtil.getTradeDisplayRowCount(trader);
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader) + 32;
		
		//Top-left corner
		this.blit(poseStack, startX + tradeOffset, startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			this.blit(poseStack, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, FluidTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				this.blit(poseStack, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY, 6 + FluidTradeButton.WIDTH, 0, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		this.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 75, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			this.blit(poseStack, startX + tradeOffset, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 0, 17, 6, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				this.blit(poseStack, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 17, FluidTradeButton.WIDTH, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					this.blit(poseStack, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6 + FluidTradeButton.WIDTH, 17, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			this.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 75, 17, 6, FluidTraderUtil.TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		this.blit(poseStack, startX + tradeOffset, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 0, 104, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			this.blit(poseStack, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 104, FluidTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				this.blit(poseStack, startX + tradeOffset + (x * FluidTraderUtil.TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 6, 104, FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		this.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * FluidTraderUtil.TRADEBUTTON_VERTICALITY), 75, 104, 6, 7);
		
		//Draw the bottom (player inventory & coin input slots)
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(trader) + 32;
		int tradeHeight = FluidTraderUtil.getTradeDisplayHeight(trader);
		this.blit(poseStack, startX + inventoryOffset, startY + tradeHeight, 0, 111, 176 + 32, 100);
		//Draw the upgrade slots
		this.blit(poseStack, startX + inventoryOffset - 32, startY + tradeHeight, 176, 111, 32, 100);
		
		//Draw the fake fluid trade buttons
		for(int i = 0; i < this.menu.getTrader().getTradeCount(); i++)
		{
			FluidTradeButton.renderFluidTradeButton(poseStack, this, font, startX + FluidTraderUtil.getButtonPosX(trader, i) + 32, startY + FluidTraderUtil.getButtonPosY(trader, i), i, trader, false, true, true);
		}
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		IFluidTrader trader = this.menu.getTrader();
		
		if(trader == null)
			return;
		
		font.draw(poseStack, this.menu.getTrader().getName(), 8.0f + FluidTraderUtil.getTradeDisplayOffset(trader) + 32, 6.0f, 0x404040);
		
		font.draw(poseStack, this.playerInventoryTitle, FluidTraderUtil.getInventoryDisplayOffset(trader) + 8.0f + 32, this.imageHeight - 94, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int traderOffset = FluidTraderUtil.getTradeDisplayOffset(this.menu.getTrader()) + 32;
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(this.menu.getTrader()) + 32;
		
		this.buttonShowTrades = this.addRenderableWidget(IconAndButtonUtil.traderButton(this.leftPos + traderOffset - 20, this.topPos, this::PressTradesButton));
		this.buttonCollectMoney = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos + traderOffset - 20, this.topPos + 20, this::PressCollectionButton, () -> this.menu.getTrader()));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = !this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().getCoreSettings().hasBankAccount();
		
		this.buttonStoreMoney = this.addRenderableWidget(IconAndButtonUtil.storeCoinButton(this.leftPos + inventoryOffset + 176 + 32, this.topPos + FluidTraderUtil.getTradeDisplayHeight(this.menu.getTrader()), this::PressStoreCoinsButton));
		this.buttonStoreMoney.visible = false;
		
		this.buttonShowLog = this.addRenderableWidget(IconAndButtonUtil.showLoggerButton(this.leftPos + traderOffset, this.topPos - 20, this::PressLogButton, () -> this.logWindow.visible));
		this.buttonClearLog = this.addRenderableWidget(IconAndButtonUtil.clearLoggerButton(this.leftPos + traderOffset + 20, this.topPos - 20, this::PressClearLogButton));
		
		int tradeWindowWidth = FluidTraderUtil.getTradeDisplayWidth(this.menu.getTrader());
		
		this.buttonOpenSettings = this.addRenderableWidget(IconAndButtonUtil.openSettingsButton(this.leftPos + traderOffset + tradeWindowWidth - 20, this.topPos - 20, this::PressSettingsButton));
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addRenderableWidget(IconAndButtonUtil.tradeRuleButton(this.leftPos + traderOffset + tradeWindowWidth - 40, this.topPos - 20, this::PressTradeRulesButton));
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.logWindow = this.addWidget(IconAndButtonUtil.traderLogWindow(this, this.menu::getTrader));
		this.logWindow.visible = false;
		
		for(int i = 0; i < this.menu.getTrader().getTradeCount(); i++)
		{
			this.tradePriceButtons.add(this.addRenderableWidget(new PlainButton(this.leftPos + FluidTraderUtil.getPriceButtonPosX(this.menu.getTrader(), i) + 32, this.topPos + FluidTraderUtil.getPriceButtonPosY(this.menu.getTrader(), i), 10, 10, this::PressTradePriceButton, GUI_TEXTURE, 176 + 64, 40)));
		}
		
		tick();
		
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		
		IFluidTrader trader = this.menu.getTrader();
		
		if(trader == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.renderBackground(poseStack);
		if(this.logWindow.visible)
		{
			this.logWindow.render(poseStack, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(poseStack, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(poseStack, mouseX, mouseY, partialTicks);
			IconAndButtonUtil.renderButtonTooltips(poseStack, mouseX, mouseY, Lists.newArrayList(this.buttonShowLog, this.buttonClearLog));
			return;
		}
		super.render(poseStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(poseStack, mouseX, mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(poseStack, mouseX, mouseY, this.renderables);
		
		for(int i = 0; i < trader.getTradeCount(); i++)
		{
			int result = FluidTradeButton.tryRenderTooltip(poseStack, this, i, trader, this.leftPos + FluidTraderUtil.getButtonPosX(trader, i) + 32, this.topPos + FluidTraderUtil.getButtonPosY(trader, i), mouseX, mouseY, true);
			if(result == -2 && this.menu.getCarried().isEmpty() && this.menu.hasPermission(Permissions.EDIT_TRADES))
				this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lctech.trader.fluid_edit"), mouseX, mouseY);
		}
		
	}
	
	public void containerTick()
	{
		
		IFluidTrader trader = this.menu.getTrader();
		
		if(trader == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		if(!this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.PressTradesButton(this.buttonShowTrades);
			return;
		}
		
		this.buttonCollectMoney.visible = (!trader.getCoreSettings().isCreative() || trader.getStoredMoney().getRawValue() > 0) && this.menu.hasPermission(Permissions.COLLECT_COINS) && !trader.getCoreSettings().hasBankAccount();
		this.buttonCollectMoney.active = trader.getStoredMoney().getRawValue() > 0;
		
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd() && this.menu.hasPermission(Permissions.STORE_COINS);
		this.buttonClearLog.visible = trader.getLogger().logText.size() > 0 && this.menu.hasPermission(Permissions.CLEAR_LOGS);
		
		boolean visible = this.menu.hasPermission(Permissions.EDIT_TRADES);
		this.tradePriceButtons.forEach(button -> button.visible = visible);
		
	}
	
	//0 for left-click. 1 for right click
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		IFluidTrader trader = this.menu.getTrader();
		ItemStack heldItem = this.menu.getCarried();
		int tradeCount = trader.getTradeCount();
		for(int i = 0; i < tradeCount; ++i)
		{
			FluidTradeData trade = trader.getTrade(i);
			int buttonX = this.leftPos + FluidTraderUtil.getButtonPosX(trader, i) + 32;
			int buttonY = this.topPos + FluidTraderUtil.getButtonPosY(trader, i);
			//Interact with the bucket/product
			if(FluidTradeButton.isMouseOverBucket(buttonX, buttonY, (int)mouseX, (int)mouseY) && this.menu.hasPermission(Permissions.EDIT_TRADES))
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
					this.menu.PlayerTankInteraction(i);
					LCTechPacketHandler.instance.sendToServer(new MessageFluidTradeTankInteraction(i));
					return true;
				}
			}
			//Interact with the drain/fill buttons
			if(this.menu.hasPermission(FluidPermissions.EDIT_DRAINABILITY))
			{
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
		this.menu.getTrader().sendOpenTraderMessage();
	}
	
	private void PressCollectionButton(Button button)
	{
		if(menu.hasPermission(Permissions.COLLECT_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressStoreCoinsButton(Button button)
	{
		if(menu.hasPermission(Permissions.COLLECT_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageStoreCoins());
		}
	}
	
	private void PressTradePriceButton(Button button)
	{
		if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
			return;
		int tradeIndex = 0;
		if(tradePriceButtons.contains(button))
			tradeIndex = tradePriceButtons.indexOf(button);
		
		this.minecraft.setScreen(new TradeFluidPriceScreen(this.menu::getTrader, tradeIndex));
	}
	
	private void PressLogButton(Button button)
	{
		this.logWindow.visible = !this.logWindow.visible;
	}
	
	private void PressClearLogButton(Button button)
	{
		this.menu.getTrader().sendClearLogMessage();
	}
	
	private void PressTradeRulesButton(Button button)
	{
		this.minecraft.setScreen(new TradeRuleScreen(this.menu.getTrader().getRuleScreenHandler()));
	}
	
	private void PressSettingsButton(Button button)
	{
		this.menu.player.closeContainer();
		this.minecraft.setScreen(new TraderSettingsScreen(this.menu::getTrader, (player) -> this.menu.getTrader().sendOpenStorageMessage()));
	}
	
	
}
