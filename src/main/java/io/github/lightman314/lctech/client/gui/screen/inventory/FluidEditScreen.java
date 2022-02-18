package io.github.lightman314.lctech.client.gui.screen.inventory;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.menu.FluidEditMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemEditScreen;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidEditScreen extends AbstractContainerScreen<FluidEditMenu>{
	
	public static final ResourceLocation GUI_TEXTURE = ItemEditScreen.GUI_TEXTURE;
	
	private EditBox searchField;
	
	Button buttonPageLeft;
	Button buttonPageRight;
	
	Button buttonChangeName;
	
	boolean firstTick = false;
	
	List<Button> tradePriceButtons = Lists.newArrayList();
	
	public FluidEditScreen(FluidEditMenu container, Inventory inventory, Component title) {
		super(container, inventory, title);
		this.width = 176;
		this.height = 156;
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		if(this.menu.getTrader() == null)
			return;
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		int startX = (this.width - width) / 2;
		int startY = (this.height - height) / 2;
		
		//Render the BG
		this.blit(poseStack, startX, startY, 0, 0, this.width, this.height);
		
		//Render the fake trade button
		FluidTradeButton.renderFluidTradeButton(poseStack, this, font, startX, startY - FluidTradeButton.HEIGHT, this.menu.tradeIndex, this.menu.traderSource.get(), false, true, false);
		
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
	{
		this.font.draw(poseStack, new TranslatableComponent("gui.lctech.fluid_edit.title").getString(), 8.0f, 6.0f, 0x404040);
	}
	
	protected void init() {
		super.init();
		
		//Initialize the search field
		this.searchField = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 81, this.topPos + 6, 79, 9, new TranslatableComponent("gui.lightmanscurrency.item_edit.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		//Initialize the buttons
		//Page Buttons
		this.buttonPageLeft = this.addRenderableWidget(IconAndButtonUtil.leftButton(this.leftPos - 20, this.topPos, this::PressPageButton));
		this.buttonPageRight = this.addRenderableWidget(IconAndButtonUtil.rightButton(this.leftPos + this.width, this.topPos, this::PressPageButton));
		
		//Close Button
		this.addRenderableWidget(new Button(this.leftPos + 7, this.topPos + 129, 162, 20, new TranslatableComponent("gui.button.lightmanscurrency.back"), this::PressCloseButton));
		
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(poseStack, mouseX, mouseY);
		
		FluidTradeButton.tryRenderTooltip(poseStack, this, this.menu.tradeIndex, this.menu.traderSource.get(), this.leftPos, this.topPos - FluidTradeButton.HEIGHT, mouseX, mouseY, true);
		
	}
	
	@Override
	public void containerTick()
	{
		
		if(this.menu.getTrader() == null)
		{
			this.menu.player.closeContainer();
			return;
		}
		
		this.searchField.tick();
		
		this.buttonPageLeft.active = this.menu.getPage() > 0;
		this.buttonPageRight.active = this.menu.getPage() < this.menu.maxPage();
		
		if(!firstTick) {
			firstTick = true;
			this.menu.refreshPage();
		}
	}
	
	@Override
	public boolean charTyped(char c, int code)
	{
		String s = this.searchField.getValue();
		if(this.searchField.charTyped(c, code))
		{
			if(!Objects.equals(s, this.searchField.getValue()))
			{
				menu.modifySearch(this.searchField.getValue());
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods)
	{
		String s = this.searchField.getValue();
		if(this.searchField.keyPressed(key, scanCode, mods))
		{
			if(!Objects.equals(s,  this.searchField.getValue()))
			{
				menu.modifySearch(this.searchField.getValue());
			}
			return true;
		}
		return this.searchField.isFocused() && this.searchField.visible && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
	}
	
	private void PressPageButton(Button button)
	{
		int direction = 1;
		if(button == this.buttonPageLeft)
			direction = -1;
		
		menu.modifyPage(direction);
	}
	
	private void PressCloseButton(Button button)
	{
		this.menu.openTraderStorage();
	}
	

}
