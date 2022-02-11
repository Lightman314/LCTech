package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.EnergyTradeButton;
import io.github.lightman314.lctech.menu.EnergyTraderMenu;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRNextTrader;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRSkipTo;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public class EnergyTraderScreen extends AbstractContainerScreen<EnergyTraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/energy_trader.png");
	
	public static final int ENERGY_BAR_HEIGHT = 55;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	//Universal Trader
	Button buttonBack;
	
	//Cash Register Trader
	Button buttonLeft;
	Button buttonRight;
	EditBox pageInput;
	Button buttonSkipToPage;
	
	List<EnergyTradeButton> tradeButtons = new ArrayList<>();
	
	public EnergyTraderScreen(EnergyTraderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 203;
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		//Render the background
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		//Render the energy bar
		double fillPercent = (double)this.menu.getTrader().getTotalEnergy() / (double)this.menu.getTrader().getMaxEnergy();
		int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
		int yOffset = ENERGY_BAR_HEIGHT - fillHeight;
		this.blit(pose, this.leftPos + 8, this.topPos + 18 + yOffset, this.imageWidth, yOffset, 16, fillHeight);
		
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY)
	{
		this.font.draw(pose, this.menu.getTrader().getTitle(), 8f, 6f, 0x404040);
		this.font.draw(pose, this.playerInventoryTitle, 8f, this.imageHeight - 94f, 0x404040);
		this.font.draw(pose, new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())), 80f, this.imageHeight - 124f, 0x404040);
	}
	
	@Override
	protected void init()
	{
		
		super.init();
		
		this.buttonShowStorage = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos, this::PressStorageButton, this.font, IconData.of(Items.CHEST)));
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE) && !this.menu.isCashRegister();
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().getCoreSettings().hasBankAccount();
		
		//Universal Widget(s)
		this.buttonBack = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos + 40, this::PressBackButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 32, 0)));
		this.buttonBack.visible = this.menu.isUniversal();
		
		//Cash Register Widget(s)
		if(this.menu.isCashRegister() && this.menu.getCashRegister().getPairedTraderSize() > 1)
		{
			this.buttonLeft = this.addRenderableWidget(new IconButton(this.leftPos - 20, this.topPos, this::PressArrowButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 16, 16)));
			this.buttonRight = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth, this.topPos, this::PressArrowButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 32, 16)));
			
			this.pageInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 50, this.topPos - 19, this.imageWidth - 120, 18, new TextComponent("")));
			this.pageInput.setMaxLength(2);
			this.pageInput.setValue(String.valueOf(this.menu.getThisCRIndex() + 1));
			
			this.buttonSkipToPage = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - 68, this.topPos - 20, this::PressPageSkipButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 32, 16)));
			this.buttonSkipToPage.active = false;
		}
		
		this.initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		for(int y = 0; y < 2; ++y)
		{
			for(int x = 0; x < 2; ++x)
			{
				this.tradeButtons.add(this.addRenderableWidget(new EnergyTradeButton(this.leftPos + 28 + 73 * x, this.topPos + 17 + 31 * y, this::PressTradeButton, x + 2 * y, this, this.font, () -> this.menu.getTrader(), () -> this.menu.GetCoinValue(), () -> this.menu.getBatteryStack())));
			}
		}
	}
	
	@Override
	public void containerTick()
	{
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE) && !this.menu.isCashRegister();
		
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.menu.getTrader().getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.menu.getTrader().getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.getTrader().getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
		if(this.buttonSkipToPage != null)
		{
			if(this.pageInput != null)
				TextInputUtil.whitelistInteger(this.pageInput, 1, this.menu.getTotalCRSize());
			int pageInputValue = this.getPageInput();
			this.buttonSkipToPage.active = pageInputValue >= 0 && pageInputValue < this.menu.getTotalCRSize() && pageInputValue != this.menu.getThisCRIndex();
		}
		
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		if(this.buttonShowStorage != null && this.buttonShowStorage.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.openstorage"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.getTrader().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonBack != null && this.buttonBack.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.trader.universaltrader.back"), mouseX, mouseY);
		}
		else if(this.isMouseOverEnergy(mouseX, mouseY))
		{
			this.renderComponentTooltip(pose, IEnergyTrader.getEnergyHoverTooltip(this.menu.getTrader()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); ++i)
		{
			this.tradeButtons.get(i).tryRenderTooltip(pose, this, this.menu.getTrader(), mouseX, mouseY);
		}
	}
	
	private boolean isMouseOverEnergy(int mouseX, int mouseY)
	{
		return mouseX >= this.leftPos + 7 && mouseX < this.leftPos + 7 + 18 && mouseY >= this.topPos + 17 && mouseY < this.topPos + 17 + 57;
	}
	
	private void PressStorageButton(Button button)
	{
		if(this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.menu.getTrader().sendOpenStorageMessage();
		}
	}
	
	private void PressCollectionButton(Button button)
	{
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressTradeButton(Button button)
	{
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
	}
	
	private void PressBackButton(Button button)
	{
		this.minecraft.setScreen(new TradingTerminalScreen());
	}

	private void PressArrowButton(Button button)
	{
		int direction = 1;
		if(button == this.buttonLeft)
			direction = -1;
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCRNextTrader(direction));
	}
	
	private void PressPageSkipButton(Button button)
	{
		int page = this.getPageInput();
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCRSkipTo(page));
	}
	
	private int getPageInput()
	{
		if(this.pageInput != null)
			return TextInputUtil.getIntegerValue(this.pageInput) - 1;
		return -1;
	}
	
	
	
}
