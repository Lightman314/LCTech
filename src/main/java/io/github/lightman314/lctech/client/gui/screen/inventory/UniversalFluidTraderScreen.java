package io.github.lightman314.lctech.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.common.FluidTraderUtil;
import io.github.lightman314.lctech.menu.UniversalFluidTraderMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public class UniversalFluidTraderScreen extends AbstractContainerScreen<UniversalFluidTraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/container/fluid_trader.png");
	
	public static final int TRADEBUTTON_VERT_SPACER = FluidTraderUtil.TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_VERTICALITY = FluidTraderUtil.TRADEBUTTON_VERTICALITY;
	public static final int TRADEBUTTON_HORIZ_SPACER = FluidTraderUtil.TRADEBUTTON_HORIZ_SPACER;
	public static final int TRADEBUTTON_HORIZONTAL = FluidTraderUtil.TRADEBUTTON_HORIZONTAL;
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	
	Button buttonBack;
	
	List<FluidTradeButton> tradeButtons = new ArrayList<>();
	
	public UniversalFluidTraderScreen(UniversalFluidTraderMenu container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.imageWidth = FluidTraderUtil.getWidth(this.menu.getData());
		this.imageHeight = 133 + FluidTraderUtil.getTradeDisplayHeight(this.menu.getData());
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int x, int y) {
		
		FluidTraderScreen.drawTraderBackground(poseStack, this, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.getData());
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		
		FluidTraderScreen.drawTraderForeground(poseStack, this.font, this.menu.getData(), this.imageHeight,
				this.menu.getData().getTitle(),
				this.playerInventoryTitle,
				new TranslatableComponent("tooltip.lightmanscurrency.credit",MoneyUtil.getStringOfValue(this.menu.GetCoinValue())));
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = FluidTraderUtil.getTradeDisplayOffset(this.menu.getData());
		int tradeHeight = FluidTraderUtil.getTradeDisplayHeight(this.menu.getData());
		
		this.buttonBack = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos + tradeHeight - 20, this::PressBackButton, this.font, IconData.of(GUI_TEXTURE, 176 + 32, 0)));
		
		this.buttonShowStorage = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos, this::PressStorageButton, this.font, IconData.of(Items.CHEST)));
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE);
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getData().getCoreSettings().hasBankAccount();
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.menu.getData().getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addRenderableWidget(new FluidTradeButton(this.leftPos + FluidTraderUtil.getButtonPosX(this.menu.getData(), i), this.topPos + FluidTraderUtil.getButtonPosY(this.menu.getData(), i), this::PressTradeButton, i, this, this.font, () -> this.menu.getData(), () -> this.menu.GetCoinValue(), () -> this.menu.getBucketItem())));
		}
	}
	
	@Override
	public void containerTick()
	{
		this.menu.tick();
		
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE);
		
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.menu.getData().getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.menu.getData().getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.getData().getCoreSettings().isCreative();
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
			this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.getData().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonBack != null && this.buttonBack.active && this.buttonBack.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(poseStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.universaltrader.back"), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(poseStack, this, this.menu.getData(), mouseX, mouseY, false);
		}
	}

	private void PressStorageButton(Button button)
	{
		if(menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.menu.getData().getTraderID()));
		}
		else
			LCTech.LOGGER.warn("Non-owner attempted to open the Fluid Trader's Storage");
	}
	
	private void PressCollectionButton(Button button)
	{
		if(menu.hasPermission(Permissions.COLLECT_COINS))
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
	
	private void PressBackButton(Button button)
	{
		this.minecraft.setScreen(new TradingTerminalScreen());
	}
	
}
