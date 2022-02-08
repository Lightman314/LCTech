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
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FluidTraderScreen extends AbstractContainerScreen<FluidTraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = FluidTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = FluidTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = FluidTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	List<FluidTradeButton> tradeButtons = new ArrayList<>();
	
	public FluidTraderScreen(FluidTraderMenu container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.imageWidth = FluidTraderUtil.getWidth(this.menu.tileEntity);
		this.imageHeight = 133 + FluidTraderUtil.getTradeDisplayHeight(this.menu.tileEntity);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int x, int y) {
		
		drawTraderBackground(poseStack, this, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.tileEntity);
		
	}
	
	public static void drawTraderBackground(PoseStack poseStack, Screen screen, AbstractContainerMenu menu, Minecraft minecraft, int xSize, int ySize, IFluidTrader trader)
	{
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		int startX = (screen.width - xSize)/2;
		int startY = (screen.height - ySize)/2;
		
		int columnCount = FluidTraderUtil.getTradeDisplayColumnCount(trader);
		int rowCount = FluidTraderUtil.getTradeDisplayRowCount(trader);
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader);
		
		//Top-left corner
		screen.blit(poseStack, startX + tradeOffset, startY, 0, 0, 6, 17);
		for(int x = 0; x < columnCount; x++)
		{
			//Top of each button
			screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY, 6, 0, FluidTradeButton.WIDTH, 17);
			//Top spacer of each button
			if(x < columnCount - 1)
				screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY, 6 + FluidTradeButton.WIDTH, 0, TRADEBUTTON_HORIZ_SPACER, 17);
		}
		//Top-right corner
		screen.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY, 75, 0, 6, 17);
		
		//Draw the bg & spacer of each button
		for(int y = 0; y < rowCount; y++)
		{
			//Left edge
			screen.blit(poseStack, startX + tradeOffset, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 0, 17, 6, TRADEBUTTON_VERTICALITY);
			for(int x = 0; x < columnCount; x++)
			{
				//Button BG
				screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6, 17, FluidTradeButton.WIDTH, TRADEBUTTON_VERTICALITY);
				//Right spacer for the trade button
				if(x < columnCount - 1)
					screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 6 + FluidTradeButton.WIDTH, 17, TRADEBUTTON_HORIZ_SPACER, TRADEBUTTON_VERTICALITY);
			}
			//Right edge
			screen.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (y * TRADEBUTTON_VERTICALITY), 75, 17, 6, TRADEBUTTON_VERTICALITY);
		}
		
		//Bottom-left corner
		screen.blit(poseStack, startX + tradeOffset, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 0, 104, 6, 7);
		for(int x = 0; x < columnCount; x++)
		{
			//Bottom of each button
			screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 104, FluidTradeButton.WIDTH, 7);
			//Bottom spacer of each button
			if(x < columnCount - 1)
				screen.blit(poseStack, startX + tradeOffset + (x * TRADEBUTTON_HORIZONTAL) + FluidTradeButton.WIDTH + 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 6, 104, TRADEBUTTON_HORIZ_SPACER, 7);
		}
		//Bottom-right corner
		screen.blit(poseStack, startX + tradeOffset + FluidTraderUtil.getTradeDisplayWidth(trader) - 6, startY + 17 + (rowCount * TRADEBUTTON_VERTICALITY), 75, 104, 6, 7);
		
		//Draw the bottom (player inventory/coin slots)
		screen.blit(poseStack, startX + FluidTraderUtil.getInventoryDisplayOffset(trader), startY + FluidTraderUtil.getTradeDisplayHeight(trader), 0, 111, 176, 133);
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		drawTraderForeground(poseStack, this.font, this.menu.tileEntity, this.imageHeight,
				this.menu.tileEntity.getTitle(),
				this.playerInventoryTitle,
				new TranslatableComponent("tooltip.lightmanscurrency.credit",MoneyUtil.getStringOfValue(this.menu.GetCoinValue())));
		
	}
	
	public static void drawTraderForeground(PoseStack poseStack, Font font, IFluidTrader trader, int ySize, Component title, Component inventoryTitle, Component creditText)
	{
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(trader);
		int inventoryOffset = FluidTraderUtil.getInventoryDisplayOffset(trader);
		
		font.draw(poseStack, title, tradeOffset + 8f, 6f, 0x404040);
		
		font.draw(poseStack, inventoryTitle, inventoryOffset + 8f, (ySize - 94), 0x404040);
		
		font.draw(poseStack, creditText, inventoryOffset + 80f, ySize - 124f, 0x404040);
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(this.menu.tileEntity);
		
		this.buttonShowStorage = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos, this::PressStorageButton, this.font, IconData.of(GUI_TEXTURE, 176, 0)));
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE);
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
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
		
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE);
		
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.menu.tileEntity.getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.menu.tileEntity.getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.tileEntity.getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(poseStack, mouseX, mouseY);
		
		if(this.buttonShowStorage != null && this.buttonShowStorage.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.openstorage"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.tileEntity.getStoredMoney().getString()), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(poseStack, this, this.menu.tileEntity, mouseX, mouseY, false);
		}
	}

	private void PressStorageButton(Button button)
	{
		if(menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.menu.tileEntity.getBlockPos()));
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
	
}
