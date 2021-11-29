package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.container.FluidTraderContainer;
import io.github.lightman314.lctech.trader.IFluidTrader;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidTraderScreen extends ContainerScreen<FluidTraderContainer>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = FluidTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = FluidTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = FluidTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	List<FluidTradeButton> tradeButtons = new ArrayList<>();
	
	public FluidTraderScreen(FluidTraderContainer container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.xSize = FluidTraderUtil.getWidth(this.container.tileEntity);
		this.ySize = 133 + FluidTraderUtil.getTradeDisplayHeight(this.container.tileEntity);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int x, int y) {
		
		drawTraderBackground(matrix, this, this.container, this.minecraft, this.xSize, this.ySize, this.container.tileEntity);
		
	}
	
	@SuppressWarnings("deprecation")
	public static void drawTraderBackground(MatrixStack matrix, Screen screen, Container container, Minecraft minecraft, int xSize, int ySize, IFluidTrader trader)
	{
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (screen.width - xSize)/2;
		int startY = (screen.height - ySize)/2;
		
		int columnCount = FluidTraderUtil.getTradeDisplayColumnCount(trader);
		int rowCount = FluidTraderUtil.getTradeDisplayRowCount(trader);
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader);
		
		//Top-left corner
		screen.blit(matrix, startX + tradeOffset, startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, FluidTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY, 6 + FluidTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		screen.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 75, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			screen.blit(matrix, startX + tradeOffset, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 0, 17, 6, TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6, 17, FluidTradeButton.WIDTH, TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6 + FluidTradeButton.WIDTH, 17, TRADEBUTTON_HORIZ_SPACER, TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			screen.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 75, 17, 6, TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		screen.blit(matrix, startX + tradeOffset, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 0, 104, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 104, FluidTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				screen.blit(matrix, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 104, TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		screen.blit(matrix, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 75, 104, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		screen.blit(matrix, startX + FluidTraderUtil.getInventoryDisplayOffset(trader), startY + FluidTraderUtil.getTradeDisplayHeight(trader), 0, 111, 176, 133);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		drawTraderForeground(matrix, this.font, this.container.tileEntity, this.ySize,
				this.container.tileEntity.getTitle(),
				this.playerInventory.getDisplayName(),
				new TranslationTextComponent("tooltip.lightmanscurrency.credit",MoneyUtil.getStringOfValue(this.container.GetCoinValue())));
		
	}
	
	public static void drawTraderForeground(MatrixStack matrix, FontRenderer font, IFluidTrader trader, int ySize, ITextComponent title, ITextComponent inventoryTitle, ITextComponent creditText)
	{
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader);
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(trader);
		
		font.drawString(matrix, title.getString(), tradeOffset + 8f, 6f, 0x404040);
		
		font.drawString(matrix, inventoryTitle.getString(), inventoryOffset + 8f, (ySize - 94), 0x404040);
		
		font.drawString(matrix, creditText.getString(), inventoryOffset + 80f, ySize - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		if(this.container.isOwner())
		{
			
			int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(this.container.tileEntity);
			
			this.buttonShowStorage = this.addButton(new IconButton(this.guiLeft - 20 + tradeOffset, this.guiTop, this::PressStorageButton, GUI_TEXTURE, 176, 0));
			
			this.buttonCollectMoney = this.addButton(new IconButton(this.guiLeft - 20 + tradeOffset, this.guiTop + 20, this::PressCollectionButton, GUI_TEXTURE, 176 + 16, 0));
			this.buttonCollectMoney.active = false;
		}
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.container.tileEntity.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addButton(new FluidTradeButton(this.guiLeft + FluidTraderUtil.getButtonPosX(this.container.tileEntity, i), this.guiTop + FluidTraderUtil.getButtonPosY(this.container.tileEntity, i), this::PressTradeButton, i, this, this.font, () -> this.container.tileEntity, this.container)));
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		this.container.tick();
		
		if(this.buttonCollectMoney != null)
		{
			this.buttonCollectMoney.active = this.container.tileEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.container.tileEntity.isCreative();
		}
		
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrix, mouseX, mouseY);
		
		if(this.buttonShowStorage != null && this.buttonShowStorage.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.trader.openstorage"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrix, new TranslationTextComponent("tooltip.lightmanscurrency.trader.collectcoins", this.container.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrix, this, this.container.tileEntity, mouseX, mouseY, container, false);
		}
	}

	private void PressStorageButton(Button button)
	{
		if(container.isOwner())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.container.tileEntity.getPos()));
		}
		else
			LCTech.LOGGER.warn("Non-owner attempted to open the Fluid Trader's Storage");
	}
	
	private void PressCollectionButton(Button button)
	{
		if(container.isOwner())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
		else
			LCTech.LOGGER.warn("Non-owner attempted the collect the stored money.");
	}
	
	private void PressTradeButton(Button button)
	{
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		//LCTechPacketHandler.instance.sendToServer(new MessageExecuteFluidTrade(tradeIndex));
		
	}
	
}
