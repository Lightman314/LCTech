package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.menu.FluidTraderMenuCR;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRNextTrader;
import io.github.lightman314.lightmanscurrency.network.message.cashregister.MessageCRSkipTo;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidTraderScreenCR extends AbstractContainerScreen<FluidTraderMenuCR>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = FluidTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = FluidTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = FluidTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	//Button buttonShowStorage;
	Button buttonCollectMoney;
	
	Button buttonLeft;
	Button buttonRight;
	
	EditBox pageInput;
	Button buttonSkipToPage;
	
	List<FluidTradeButton> tradeButtons = new ArrayList<>();
	
	public FluidTraderScreenCR(FluidTraderMenuCR container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.imageWidth = FluidTraderUtil.getWidth(this.menu.tileEntity);
		this.imageHeight = 133 + FluidTraderUtil.getTradeDisplayHeight(this.menu.tileEntity);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int x, int y) {
		
		FluidTraderScreen.drawTraderBackground(poseStack, this, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.tileEntity);
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		FluidTraderScreen.drawTraderForeground(poseStack, this.font, this.menu.tileEntity, this.imageHeight,
				new TranslatableComponent("gui.lightmanscurrency.trading.title", this.menu.tileEntity.getName(), new TranslatableComponent("gui.lightmanscurrency.trading.list", this.menu.getThisIndex() + 1, this.menu.getTotalCount())),
				this.playerInventoryTitle,
				new TranslatableComponent("tooltip.lightmanscurrency.credit",MoneyUtil.getStringOfValue(this.menu.GetCoinValue())));
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(this.menu.tileEntity);
		
		if(this.menu.cashRegister.getPairedTraderSize() > 1)
		{
			this.buttonLeft = this.addRenderableWidget(new IconButton(this.leftPos + tradeOffset - 20, this.topPos, this::PressArrowButton, this.font, IconData.of(GUI_TEXTURE, 176, 16)));
			this.buttonRight = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - tradeOffset, this.topPos, this::PressArrowButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 16)));
			
			this.pageInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 50, this.topPos - 19, this.imageWidth - 120, 18, new TextComponent("")));
			this.pageInput.setMaxLength(9);
			this.pageInput.setValue(String.valueOf(this.menu.getThisIndex() + 1));
			
			this.buttonSkipToPage = this.addRenderableWidget(new IconButton(this.leftPos + this.imageWidth - 68, this.topPos - 20, this::PressPageSkipButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 16)));
			this.buttonSkipToPage.active = false;
		}
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, this.imageWidth + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.tileEntity.getCoreSettings().hasBankAccount();
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.menu.tileEntity.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addRenderableWidget(new FluidTradeButton(this.leftPos + FluidTraderUtil.getButtonPosX(this.menu.tileEntity, i), this.topPos + FluidTraderUtil.getButtonPosY(this.menu.tileEntity, i), this::PressTradeButton, i, this, this.font, () -> this.menu.tileEntity, () -> this.menu.GetCoinValue(), () -> this.menu.getBucketItem())));
		}
	}
	
	@Override
	public void containerTick()
	{
		
		this.menu.tick();
		
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.menu.tileEntity.getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.menu.tileEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.tileEntity.getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
		if(this.buttonSkipToPage != null)
		{
			this.buttonSkipToPage.active = this.getPageInput() >= 0 && this.getPageInput() < this.menu.getTotalCount() && this.getPageInput() != this.menu.getThisIndex();
		}
		
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(poseStack, mouseX, mouseY);
		
		if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(poseStack, this, this.menu.tileEntity, mouseX, mouseY, false);
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
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		//LCTechPacketHandler.instance.sendToServer(new MessageExecuteFluidTrade(tradeIndex));
		
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
	
	private int getPageInput() {
		if(this.pageInput != null)
			return TextInputUtil.getIntegerValue(this.pageInput) - 1;
		return -1;
	}
	
}
