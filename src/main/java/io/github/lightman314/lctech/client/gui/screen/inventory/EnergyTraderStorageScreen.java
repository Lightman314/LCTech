package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.screen.TradeEnergyPriceScreen;
import io.github.lightman314.lctech.client.gui.widget.button.EnergyTradeButton;
import io.github.lightman314.lctech.container.EnergyTraderStorageContainer;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class EnergyTraderStorageScreen extends ContainerScreen<EnergyTraderStorageContainer>{

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
	
	public EnergyTraderStorageScreen(EnergyTraderStorageContainer menu, PlayerInventory inventory, ITextComponent title) {
		super(menu, inventory, title);
		this.xSize = 208;
		this.ySize = 203;
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		
		//Render the background
		this.blit(matrix, this.guiLeft, this.guiTop, 0, 0, 176, this.ySize);
		//Render the coin slot background
		this.blit(matrix, this.guiLeft + 176, this.guiTop + 103, 176, 103, 32, 100);
		
		//Render the energy bar
		double fillPercent = (double)this.container.getTrader().getTotalEnergy() / (double)this.container.getTrader().getMaxEnergy();
		int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
		int yOffset = ENERGY_BAR_HEIGHT - fillHeight;
		this.blit(matrix, this.guiLeft + 8, this.guiTop + 18 + yOffset, 176, yOffset, 16, fillHeight);
		
		//Draw the fake energy trader buttons
		for(int y = 0; y < 2; y++)
		{
			for(int x = 0; x < 2; x++)
			{
				int tradeIndex = x + y * 2;
				if(tradeIndex < this.container.getTrader().getTradeCount())
					EnergyTradeButton.renderEnergyTradeButton(matrix, this, this.font, this.guiLeft + 28 + 73 * x, this.guiTop + 17 + 31 * y, tradeIndex, this.container.getTrader());
			}
		}
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		this.font.drawString(matrix, this.container.getTrader().getName().getString(), 8f, 6f, 0x404040);
		this.font.drawString(matrix, this.playerInventory.getName().getString(), 8f, this.ySize - 94f, 0x404040);
	}
	
	@Override
	protected void init()
	{
		
		super.init();
		
		this.buttonShowTrades = this.addButton(new IconButton(this.guiLeft, this.guiTop - 20, this::PressTradesButton, this.font, IconData.of(ModItems.TRADING_CORE)));
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft + 20, this.guiTop - 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.container.hasPermission(Permissions.COLLECT_COINS) && !this.container.getTrader().getCoreSettings().hasBankAccount();
		
		this.buttonStoreMoney = this.addButton(new IconButton(this.guiLeft + this.xSize, this.guiTop + this.ySize - 100, this::PressStoreCoinsButton, this.font, IconData.of(GUI_TEXTURE, 176 + 32, 0)));
		this.buttonStoreMoney.visible = false;
		
		this.buttonShowLog = this.addButton(new IconButton(this.guiLeft + 40, this.guiTop - 20, this::PressLogButton, this.font, IconData.of(new TranslationTextComponent("gui.button.lightmanscurrency.showlog"))));
		this.buttonClearLog = this.addButton(new IconButton(this.guiLeft + 60, this.guiTop - 20, this::PressClearLogButton, this.font, IconData.of(new TranslationTextComponent("gui.button.lightmanscurrency.clearlog"))));
		
		this.buttonOpenSettings = this.addButton(new IconButton(this.guiLeft + 176 - 20, this.guiTop - 20, this::PressSettingsButton, this.font, IconData.of(ItemTraderStorageScreen.GUI_TEXTURE, 176 + 32, 0)));
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		
		this.buttonTradeRules = this.addButton(new IconButton(this.guiLeft + 176 - 40, this.guiTop - 20, this::PressTradeRulesButton, this.font, IconData.of(Items.BOOK)));
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.logWindow = this.addListener(new TextLogWindow(this.guiLeft + this.xSize / 2 - TextLogWindow.WIDTH / 2, this.guiTop, () -> this.container.getTrader().getLogger(), this.font));
		this.logWindow.visible = false;
		
		this.buttonAddTrade = this.addButton(new PlainButton(this.guiLeft + 147, this.guiTop + 5, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 240, 0));
		this.buttonRemoveTrade = this.addButton(new PlainButton(this.guiLeft + 159, this.guiTop + 5, 10, 10, this::PressAddRemoveTradeButton, GUI_TEXTURE, 240, 20));
		
		for(int y = 0; y < 2; ++y)
		{
			for(int x = 0; x < 2; ++x)
			{
				this.tradePriceButtons.add(this.addButton(new PlainButton(this.guiLeft + 28 + 73 * x, this.guiTop + 17 + 31 * y, 10, 10, this::PressTradePriceButton, GUI_TEXTURE, 240, 40)));
			}
		}
		
		this.tick();
		
	}
	
	@Override
	public void render(MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(pose);
		if(this.logWindow.visible)
		{
			this.logWindow.render(pose, mouseX, mouseY, partialTicks);
			this.buttonShowLog.render(pose, mouseX, mouseY, partialTicks);
			if(this.buttonClearLog.visible)
				this.buttonClearLog.render(pose, mouseX, mouseY, partialTicks);
			if(this.buttonShowLog.isMouseOver(mouseX,  mouseY))
				this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.hide"), mouseX, mouseY);
			else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
			return;
		}
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(pose, mouseX, mouseY);
		
		if(this.buttonShowTrades.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.opentrades"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.getTrader().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonStoreMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.storecoins"), mouseX, mouseY);
		}
		else if(this.buttonShowLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.show"), mouseX, mouseY);
		}
		else if(this.buttonClearLog.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.log.clear"), mouseX, mouseY);
		}
		else if(this.buttonTradeRules.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.traderules"), mouseX, mouseY);
		}
		else if(this.buttonOpenSettings.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslationTextComponent("tooltip.lightmanscurrency.trader.settings"), mouseX, mouseY);
		}
		else if(this.isMouseOverEnergy(mouseX, mouseY))
		{
			this.func_243308_b(pose, IEnergyTrader.getEnergyHoverTooltip(this.container.getTrader()), mouseX, mouseY);
		}
		else
		{
			for(int y = 0; y < 2; ++y)
			{
				for(int x = 0; x < 2; ++x)
				{
					int tradeIndex = x + y * 2;
					int xPos = this.guiLeft + 28 + 73 * x;
					int yPos = this.guiTop + 17 + 31 * y;
					if(tradeIndex < this.container.getTrader().getTradeCount() && this.isMouseOverTradeButton(xPos, yPos, mouseX, mouseY))
					{
						EnergyTradeButton.renderTooltip(pose, this, tradeIndex, this.container.getTrader(), xPos, yPos, mouseX, mouseY);
					}
				}
			}
		}
		
	}
	
	private boolean isMouseOverEnergy(int mouseX, int mouseY)
	{
		return mouseX >= this.guiLeft + 7 && mouseX < this.guiLeft + 7 + 18 && mouseY >= this.guiTop + 17 && mouseY < this.guiTop + 17 + 57;
	}
	
	private boolean isMouseOverTradeButton(int xPos, int yPos, int mouseX, int mouseY)
	{
		return mouseX >= xPos && mouseX < xPos + EnergyTradeButton.WIDTH && mouseY >= yPos && mouseY < yPos + EnergyTradeButton.HEIGHT;
	}
	
	@Override
	public void tick()
	{
		
		super.tick();
		
		if(!this.container.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.PressTradesButton(this.buttonShowTrades);
			return;
		}
		
		this.buttonCollectMoney.visible = (!this.container.getTrader().getCoreSettings().isCreative() || this.container.getTrader().getStoredMoney().getRawValue() > 0) && this.container.hasPermission(Permissions.COLLECT_COINS) && !this.container.getTrader().getCoreSettings().hasBankAccount();
		this.buttonCollectMoney.active = this.container.getTrader().getStoredMoney().getRawValue() > 0;
		
		this.buttonOpenSettings.visible = this.container.hasPermission(Permissions.EDIT_SETTINGS);
		this.buttonTradeRules.visible = this.container.hasPermission(Permissions.EDIT_TRADE_RULES);
		
		this.buttonStoreMoney.visible = this.container.HasCoinsToAdd() && this.container.hasPermission(Permissions.STORE_COINS);
		this.buttonClearLog.visible = this.container.getTrader().getLogger().logText.size() > 0 && this.container.hasPermission(Permissions.CLEAR_LOGS);
		
		boolean visible = this.container.hasPermission(Permissions.EDIT_TRADES);
		this.tradePriceButtons.forEach(button -> button.visible = visible);
		
		this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = this.container.hasPermission(Permissions.EDIT_TRADES);
		this.buttonAddTrade.active = this.container.getTrader().getTradeCount() < this.container.getTrader().getTradeCountLimit();
		this.buttonRemoveTrade.active = this.container.getTrader().getTradeCount() > 1;
		
		for(int i = 0; i < 4; ++i)
		{
			this.tradePriceButtons.get(i).visible = i < this.container.getTrader().getTradeCount();
		}
		
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
		
		this.minecraft.displayGuiScreen(new TradeEnergyPriceScreen(this.container.getTraderSource(), tradeIndex));
		
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
		this.minecraft.displayGuiScreen(new TraderSettingsScreen(() -> this.container.getTrader(), (player) -> this.container.getTrader().sendOpenStorageMessage()));
	}
	
	private void PressAddRemoveTradeButton(Button button)
	{
		this.container.getTrader().requestAddOrRemoveTrade(button == this.buttonAddTrade);
	}
	
}
