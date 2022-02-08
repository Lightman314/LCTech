package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.container.FluidTraderContainerCR;
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
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidTraderScreenCR extends ContainerScreen<FluidTraderContainerCR>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = FluidTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = FluidTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = FluidTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	//Button buttonShowStorage;
	Button buttonCollectMoney;
	
	Button buttonLeft;
	Button buttonRight;
	
	TextFieldWidget pageInput;
	Button buttonSkipToPage;
	
	List<FluidTradeButton> tradeButtons = new ArrayList<>();
	
	public FluidTraderScreenCR(FluidTraderContainerCR container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.xSize = FluidTraderUtil.getWidth(this.container.tileEntity);
		this.ySize = 133 + FluidTraderUtil.getTradeDisplayHeight(this.container.tileEntity);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int x, int y) {
		
		FluidTraderScreen.drawTraderBackground(matrix, this, this.container, this.minecraft, this.xSize, this.ySize, this.container.tileEntity);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		FluidTraderScreen.drawTraderForeground(matrix, this.font, this.container.tileEntity, this.ySize,
				new TranslationTextComponent("gui.lightmanscurrency.trading.title", this.container.tileEntity.getName(), new TranslationTextComponent("gui.lightmanscurrency.trading.list", this.container.getThisIndex() + 1, this.container.getTotalCount())),
				this.playerInventory.getDisplayName(),
				new TranslationTextComponent("tooltip.lightmanscurrency.credit",MoneyUtil.getStringOfValue(this.container.GetCoinValue())));
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(this.container.tileEntity);
		
		if(this.container.cashRegister.getPairedTraderSize() > 1)
		{
			this.buttonLeft = this.addButton(new IconButton(this.guiLeft + tradeOffset - 20, this.guiTop, this::PressArrowButton, this.font, IconData.of(GUI_TEXTURE, 176, 16)));
			this.buttonRight = this.addButton(new IconButton(this.guiLeft + this.xSize - tradeOffset, this.guiTop, this::PressArrowButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 16)));
			
			this.pageInput = new TextFieldWidget(this.font, this.guiLeft + 50, this.guiTop - 19, this.xSize - 120, 18, ITextComponent.getTextComponentOrEmpty(""));
			this.pageInput.setMaxStringLength(9);
			this.pageInput.setText(String.valueOf(this.container.getThisIndex() + 1));
			this.children.add(this.pageInput);
			
			this.buttonSkipToPage = this.addButton(new IconButton(this.guiLeft + this.xSize - 68, this.guiTop - 20, this::PressPageSkipButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 16)));
			this.buttonSkipToPage.active = false;
			
			
			
		}
		
		this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft - 20 + tradeOffset, this.guiTop + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, this.xSize + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.container.hasPermission(Permissions.COLLECT_COINS) && !this.container.tileEntity.getCoreSettings().hasBankAccount();
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.container.tileEntity.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addButton(new FluidTradeButton(this.guiLeft + FluidTraderUtil.getButtonPosX(this.container.tileEntity, i), this.guiTop + FluidTraderUtil.getButtonPosY(this.container.tileEntity, i), this::PressTradeButton, i, this, this.font, () -> this.container.tileEntity, () -> this.container.GetCoinValue(), () -> this.container.getBucketItem())));
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		this.container.tick();
		
		if(this.container.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.container.tileEntity.getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.container.tileEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.container.tileEntity.getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
		if(this.buttonSkipToPage != null)
		{
			this.buttonSkipToPage.active = this.getPageInput() >= 0 && this.getPageInput() < this.container.getTotalCount() && this.getPageInput() != this.container.getThisIndex();
		}
		
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		
		if(this.pageInput != null)
			this.pageInput.render(matrix, mouseX, mouseY, partialTicks);
		
		this.renderHoveredTooltip(matrix, mouseX, mouseY);
		
		if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrix, this, this.container.tileEntity, mouseX, mouseY, false);
		}
	}
	
	private void PressCollectionButton(Button button)
	{
		if(this.container.hasPermission(Permissions.COLLECT_COINS))
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
