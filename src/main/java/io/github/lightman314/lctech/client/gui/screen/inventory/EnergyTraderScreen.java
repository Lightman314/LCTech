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
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRNextTrader;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRSkipTo;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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
		
		IEnergyTrader trader = this.menu.getTrader();
		
		if(trader == null)
			return;
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		//Render the background
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		//Render the energy bar
		double fillPercent = (double)trader.getTotalEnergy() / (double)trader.getMaxEnergy();
		int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
		int yOffset = ENERGY_BAR_HEIGHT - fillHeight;
		this.blit(pose, this.leftPos + 8, this.topPos + 18 + yOffset, this.imageWidth, yOffset, 16, fillHeight);
		
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		
		IEnergyTrader trader = this.menu.getTrader();
		
		if(trader == null)
			return;
		
		this.font.draw(pose, trader.getTitle(), 8f, 6f, 0x404040);
		this.font.draw(pose, this.playerInventoryTitle, 8f, this.imageHeight - 94f, 0x404040);
		this.font.draw(pose, new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())), 80f, this.imageHeight - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		
		super.init();
		
		this.buttonShowStorage = this.addRenderableWidget(IconAndButtonUtil.storageButton(this.leftPos - 20, this.topPos, this::PressStorageButton));
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE) && !this.menu.isCashRegister();
		
		this.buttonCollectMoney = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos - 20, this.topPos + 20, this::PressCollectionButton, this.menu::getTrader));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().getCoreSettings().hasBankAccount();
		
		//Universal Widget(s)
		this.buttonBack = this.addRenderableWidget(IconAndButtonUtil.backToTerminalButton(this.leftPos - 20, this.topPos + 40, this::PressBackButton));
		this.buttonBack.visible = this.menu.isUniversal();
		
		//Cash Register Widget(s)
		if(this.menu.isCashRegister() && this.menu.getCashRegister().getPairedTraderSize() > 1)
		{
			this.buttonLeft = this.addRenderableWidget(IconAndButtonUtil.leftButton(this.leftPos - 20, this.topPos, this::PressArrowButton));
			this.buttonRight = this.addRenderableWidget(IconAndButtonUtil.rightButton(this.leftPos + this.imageWidth, this.topPos, this::PressArrowButton));
			
			this.pageInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 50, this.topPos - 19, this.imageWidth - 120, 18, new TextComponent("")));
			this.pageInput.setMaxLength(2);
			this.pageInput.setValue(String.valueOf(this.menu.getThisCRIndex() + 1));
			
			this.buttonSkipToPage = this.addRenderableWidget(IconAndButtonUtil.rightButton(this.leftPos + this.imageWidth - 68, this.topPos - 20, this::PressPageSkipButton));
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
		
		IEnergyTrader trader = this.menu.getTrader();
		
		if(trader == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE) && !this.menu.isCashRegister();
		
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !trader.getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = trader.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !trader.getCoreSettings().isCreative();
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
		
		IEnergyTrader trader = this.menu.getTrader();
		
		if(trader == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, this.renderables);

		if(this.isMouseOverEnergy(mouseX, mouseY))
		{
			this.renderComponentTooltip(pose, IEnergyTrader.getEnergyHoverTooltip(trader), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); ++i)
		{
			this.tradeButtons.get(i).tryRenderTooltip(pose, this, trader, mouseX, mouseY);
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
