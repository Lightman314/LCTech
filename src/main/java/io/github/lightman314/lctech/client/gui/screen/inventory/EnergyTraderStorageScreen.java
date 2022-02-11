package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.TradeEnergyPriceScreen;
import io.github.lightman314.lctech.client.gui.widget.button.EnergyTradeButton;
import io.github.lightman314.lctech.menu.EnergyTraderStorageMenu;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemTraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TextLogWindow;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageStoreCoins;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public class EnergyTraderStorageScreen extends AbstractContainerScreen<EnergyTraderStorageMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/energy_trader_storage.png");
	
	public static final int ENERGY_BAR_HEIGHT = 55;
	
	Button buttonShowTrades;
	Button buttonCollectMoney;
	
	Button buttonOpenSettings;
	
	Button buttonStoreMoney;
	
	Button buttonShowLog;
	Button buttonClearLog;
	
	TextLogWindow logWindow;
	
	Button buttonTradeRules;
	
	List<Button> tradePriceButtons = Lists.newArrayList();
	
	Button buttonAddTrade;
	Button buttonRemoveTrade;
	
	public EnergyTraderStorageScreen(EnergyTraderStorageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 208;
		this.imageHeight = 203;
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		//Render the background
		this.blit(pose, this.leftPos, this.topPos, 0, 0, 176, this.imageHeight);
		//Render the coin slot background
		this.blit(pose, this.leftPos + 176, this.topPos + 103, 176, 103, 32, 100);
		
		//Render the energy bar
		double fillPercent = (double)this.menu.getTrader().getTotalEnergy() / (double)this.menu.getTrader().getMaxEnergy();
		int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
		int yOffset = ENERGY_BAR_HEIGHT - fillHeight;
		this.blit(pose, this.leftPos + 8, this.topPos + 18 + yOffset, 176, yOffset, 16, fillHeight);
		
		//Draw the fake energy trader buttons
		for(int y = 0; y < 2; y++)
		{
			for(int x = 0; x < 2; x++)
			{
				int tradeIndex = x + y * 2;
				if(tradeIndex < this.menu.getTrader().getTradeCount())
					EnergyTradeButton.renderEnergyTradeButton(pose, this, this.font, this.leftPos + 28 + 73 * x, this.topPos + 17 + 31 * y, tradeIndex, this.menu.getTrader());
			}
		}
		
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY)
	{
		this.font.draw(pose, this.menu.getTrader().getName(), 8f, 6f, 0x404040);
		this.font.draw(pose, this.playerInventoryTitle, 8f, this.imageHeight - 94f, 0x404040);
	}
	
	@Override
	protected void init()
	{
		
		super.init();
		
		this.buttonShowTrades = this.addRenderableWidget(new IconButton(this.leftPos, this.topPos - 20, this::PressTradesButton, this.font, IconData.of(ModItems.TRADING_CORE)));
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos + 20, this.topPos - 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().getCoreSettings().hasBankAccount();
		
		this.buttonStoreMoney = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth, this.topPos + this.imageHeight - 100, this::PressStoreCoinsButton, this.font, IconData.of(GUI_TEXTURE, 176 + 32, 0)));
		this.buttonStoreMoney.visible = false;
		
		this.buttonShowLog = this.addRenderableWidget(new IconButton(this.leftPos + 40, this.topPos - 20, this::PressLogButton, this.font, IconData.of(new TranslatableComponent("gui.button.lightmanscurrency.showlog"))));
		this.buttonClearLog = this.addRenderableWidget(new IconButton(this.leftPos + 60, this.topPos - 20, this::PressClearLogButton, this.font, IconData.of(new TranslatableComponent("gui.button.lightmanscurrency.clearlog"))));
		
		this.buttonOpenSettings = this.addRenderableWidget(new IconButton(this.leftPos + 176 - 20, this.topPos - 20, this::PressSettingsButton, this.font, IconData.of(ItemTraderStorageScreen.GUI_TEXTURE, 176 + 32, 0)));
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addRenderableWidget(new IconButton(this.leftPos + 176 - 40, this.topPos - 20, this::PressTradeRulesButton, this.font, IconData.of(Items.BOOK)));
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.logWindow = this.addWidget(new TextLogWindow(this.leftPos + this.imageWidth / 2 - TextLogWindow.WIDTH / 2, this.topPos, () -> this.menu.getTrader().getLogger(), this.font));
		this.logWindow.visible = false;
		
		this.buttonAddTrade = this.addRenderableWidget(new PlainButton(this.leftPos + 147, this.topPos + 5, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 240, 0));
		this.buttonRemoveTrade = this.addRenderableWidget(new PlainButton(this.leftPos + 159, this.topPos + 5, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 240, 20));
		
		for(int y = 0; y < 2; ++y)
		{
			for(int x = 0; x < 2; ++x)
			{
				this.tradePriceButtons.add(this.addRenderableWidget(new PlainButton(this.leftPos + 28 + 73 * x, this.topPos + 17 + 31 * y, 10, 10, this::PressTradePriceButton, GUI_TEXTURE, 240, 40)));
			}
		}
		
		this.containerTick();
		
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(pose);
		if(this.logWindow.visible)
		{
			this.logWindow.render(pose, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(pose, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(pose, mouseX, mouseY, partialTicks);
			if(this.buttonShowLog.isMouseOver(mouseX,  mouseY))
				this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.hide"), mouseX, mouseY);
			else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
			return;
		}
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		if(this.buttonShowTrades.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.opentrades"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.getTrader().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.show"), mouseX, mouseY);
		}
		else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
		}
		else if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		else if(this.buttonOpenSettings.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.settings"), mouseX, mouseY);
		}
		else if(this.isMouseOverEnergy(mouseX, mouseY))
		{
			this.renderComponentTooltip(pose, IEnergyTrader.getEnergyHoverTooltip(this.menu.getTrader()), mouseX, mouseY);
		}
		else
		{
			for(int y = 0; y < 2; ++y)
			{
				for(int x = 0; x < 2; ++x)
				{
					int tradeIndex = x + y * 2;
					int xPos = this.leftPos + 28 + 73 * x;
					int yPos = this.topPos + 17 + 31 * y;
					if(tradeIndex < this.menu.getTrader().getTradeCount() && this.isMouseOverTradeButton(xPos, yPos, mouseX, mouseY))
					{
						EnergyTradeButton.renderTooltip(pose, this, tradeIndex, this.menu.getTrader(), xPos, yPos, mouseX, mouseY);
					}
				}
			}
		}
		
	}
	
	private boolean isMouseOverEnergy(int mouseX, int mouseY)
	{
		return mouseX >= this.leftPos + 7 && mouseX < this.leftPos + 7 + 18 && mouseY >= this.topPos + 17 && mouseY < this.topPos + 17 + 57;
	}
	
	private boolean isMouseOverTradeButton(int xPos, int yPos, int mouseX, int mouseY)
	{
		return mouseX >= xPos && mouseX < xPos + EnergyTradeButton.WIDTH && mouseY >= yPos && mouseY < yPos + EnergyTradeButton.HEIGHT;
	}
	
	@Override
	public void containerTick()
	{
		if(!this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.PressTradesButton(this.buttonShowTrades);
			return;
		}
		
		this.buttonCollectMoney.visible = (!this.menu.getTrader().getCoreSettings().isCreative() || this.menu.getTrader().getStoredMoney().getRawValue() > 0) && this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().getCoreSettings().hasBankAccount();
		this.buttonCollectMoney.active = this.menu.getTrader().getStoredMoney().getRawValue() > 0;
		
		this.buttonOpenSettings.visible = this.menu.hasPermission(Permissions.EDIT_SETTINGS);
		this.buttonTradeRules.visible = this.menu.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.buttonStoreMoney.visible = this.menu.HasCoinsToAdd() && this.menu.hasPermission(Permissions.STORE_COINS);
		this.buttonClearLog.visible = this.menu.getTrader().getLogger().logText.size() > 0 && this.menu.hasPermission(Permissions.CLEAR_LOGS);
		
		boolean visible = this.menu.hasPermission(Permissions.EDIT_TRADES);
		this.tradePriceButtons.forEach(button -> button.visible = visible);
		
		this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = this.menu.hasPermission(Permissions.EDIT_TRADES);
		this.buttonAddTrade.active = this.menu.getTrader().getTradeCount() < this.menu.getTrader().getTradeCountLimit();
		this.buttonRemoveTrade.active = this.menu.getTrader().getTradeCount() > 1;
		
		for(int i = 0; i < 4; ++i)
		{
			this.tradePriceButtons.get(i).visible = i < this.menu.getTrader().getTradeCount();
		}
		
	}
	
	private void PressTradesButton(Button button)
	{
		this.menu.getTrader().sendOpenTraderMessage();
	}
	
	private void PressCollectionButton(Button button)
	{
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressStoreCoinsButton(Button button)
	{
		if(this.menu.hasPermission(Permissions.STORE_COINS))
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
		
		this.minecraft.setScreen(new TradeEnergyPriceScreen(this.menu.getTraderSource(), tradeIndex));
		
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
		this.minecraft.setScreen(new TraderSettingsScreen(() -> this.menu.getTrader(), (player) -> this.menu.getTrader().sendOpenStorageMessage()));
	}
	
	private void PressAddRemoveTradeButton(Button button)
	{
		this.menu.getTrader().requestAddOrRemoveTrade(button == this.buttonAddTrade);
	}
	
}
