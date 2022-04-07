package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.menu.FluidTraderMenu;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Deprecated
public class FluidTraderScreen extends AbstractContainerScreen<FluidTraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = FluidTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = FluidTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = FluidTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	Button buttonBack;
	
	Button buttonLeft;
	Button buttonRight;
	
	EditBox pageInput;
	Button buttonSkipToPage;
	
	List<FluidTradeButton> tradeButtons = new ArrayList<>();
	
	public FluidTraderScreen(FluidTraderMenu container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.imageWidth = FluidTraderUtil.getWidth(this.menu.getTrader());
		this.imageHeight = 133 + FluidTraderUtil.getTradeDisplayHeight(this.menu.getTrader());
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		
		IFluidTrader trader = this.menu.getTrader();
		
		if(trader == null)
			return;
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		int startX = (this.width - this.imageWidth)/2;
		int startY = (this.height - this.imageHeight)/2;
		
		int columnCount = FluidTraderUtil.getTradeDisplayColumnCount(trader);
		int rowCount = FluidTraderUtil.getTradeDisplayRowCount(trader);
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader);
		
		//Top-left corner
		this.blit(poseStack, startX + tradeOffset, startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, FluidTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY, 6 + FluidTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		this.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 75, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			this.blit(poseStack, startX + tradeOffset, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 0, 17, 6, TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6, 17, FluidTradeButton.WIDTH, TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6 + FluidTradeButton.WIDTH, 17, TRADEBUTTON_HORIZ_SPACER, TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			this.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 75, 17, 6, TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		this.blit(poseStack, startX + tradeOffset, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 0, 104, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//this of each button
			this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 104, FluidTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				this.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 104, TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		this.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 75, 104, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		this.blit(poseStack, startX + FluidTraderUtil.getInventoryDisplayOffset(trader), startY + FluidTraderUtil.getTradeDisplayHeight(trader), 0, 111, 176, 133);
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		IFluidTrader trader = this.menu.getTrader();
		
		if(trader == null)
			return;
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader);
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(trader);
		
		font.draw(poseStack, trader.getTitle(), tradeOffset + 8f, 6f, 0x404040);
		
		font.draw(poseStack, this.playerInventoryTitle, inventoryOffset + 8f, (this.imageHeight - 94), 0x404040);
		
		font.draw(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.credit",MoneyUtil.getStringOfValue(this.menu.GetCoinValue())), inventoryOffset + 80f, this.imageHeight - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(this.menu.getTrader());
		
		this.buttonShowStorage = this.addRenderableWidget(IconAndButtonUtil.storageButton(this.leftPos - 20 + tradeOffset, this.topPos, this::PressStorageButton));
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE) && !this.menu.isCashRegister();
		
		this.buttonCollectMoney = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, this.menu::getTrader));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getTrader().getCoreSettings().hasBankAccount();
		
		if(this.menu.isUniversal())
		{
			int tradeHeight = FluidTraderUtil.getTradeDisplayHeight(this.menu.getTrader());
			this.buttonBack = this.addRenderableWidget(IconAndButtonUtil.backToTerminalButton(this.leftPos - 20 + tradeOffset, this.topPos + tradeHeight - 20, this::PressBackButton));
		}
		
		/*if(this.menu.isCashRegister() && this.menu.getTotalCRSize() > 1)
		{
			
			this.buttonLeft = this.addRenderableWidget(IconAndButtonUtil.leftButton(this.leftPos + tradeOffset - 20, this.topPos, this::PressArrowButton));
			this.buttonRight = this.addRenderableWidget(IconAndButtonUtil.rightButton(this.leftPos + this.imageWidth - tradeOffset, this.topPos, this::PressArrowButton));
			
			this.pageInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 50, this.topPos - 19, this.imageWidth - 120, 18, new TextComponent("")));
			this.pageInput.setMaxLength(9);
			this.pageInput.setValue(String.valueOf(this.menu.getThisCRIndex() + 1));
			
			this.buttonSkipToPage = this.addRenderableWidget(IconAndButtonUtil.rightButton(this.leftPos + this.imageWidth - 68,  this.topPos - 20,  this::PressPageSkipButton));
			this.buttonSkipToPage.active = false;
			
		}*/
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.menu.getTrader().getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addRenderableWidget(new FluidTradeButton(this.leftPos + FluidTraderUtil.getButtonPosX(this.menu.getTrader(), i), this.topPos + FluidTraderUtil.getButtonPosY(this.menu.getTrader(), i), this::PressTradeButton, i, this, this.font, this.menu::getTrader, () -> this.menu.GetCoinValue(), () -> this.menu.getBucketItem())));
		}
	}
	
	@Override
	public void containerTick()
	{
		
		IFluidTrader trader = this.menu.getTrader();
		
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
			this.buttonSkipToPage.active = this.getPageInput() >= 0 && this.getPageInput() < this.menu.getTotalCRSize() && this.getPageInput() != this.menu.getThisCRIndex();
		}
		
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
		super.render(poseStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(poseStack, mouseX, mouseY);
		
		IconAndButtonUtil.renderButtonTooltips(poseStack, mouseX, mouseY, this.renderables);
		
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(poseStack, this, trader, mouseX, mouseY, false);
		}
	}

	private void PressStorageButton(Button button)
	{
		if(menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			this.menu.getTrader().sendOpenStorageMessage();
		}
	}
	
	private void PressCollectionButton(Button button)
	{
		if(menu.hasPermission(Permissions.COLLECT_COINS))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressTradeButton(Button button)
	{
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(0, tradeIndex));
		
	}
	
	private void PressBackButton(Button button)
	{
		this.menu.player.closeContainer();
		this.minecraft.setScreen(new TradingTerminalScreen());
	}
	
	/*private void PressArrowButton(Button button)
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
	}*/
	
	private int getPageInput() {
		if(this.pageInput != null)
			return TextInputUtil.getIntegerValue(this.pageInput) - 1;
		return -1;
	}
	
}
