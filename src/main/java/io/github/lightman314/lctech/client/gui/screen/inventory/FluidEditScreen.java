package io.github.lightman314.lctech.client.gui.screen.inventory;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lctech.client.gui.widget.button.FluidTradeButton;
import io.github.lightman314.lctech.container.FluidEditContainer;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemEditScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidEditScreen extends ContainerScreen<FluidEditContainer>{
	
	public static final ResourceLocation GUI_TEXTURE = ItemEditScreen.GUI_TEXTURE;
	
	private TextFieldWidget searchField;
	
	Button buttonPageLeft;
	Button buttonPageRight;
	
	Button buttonChangeName;
	
	boolean firstTick = false;
	
	List<Button> tradePriceButtons = Lists.newArrayList();
	
	public FluidEditScreen(FluidEditContainer container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.xSize = 176;
		this.ySize = 156;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		int startX = (this.width - xSize) / 2;
		int startY = (this.height - ySize) / 2;
		
		//Render the BG
		this.blit(matrix, startX, startY, 0, 0, this.xSize, this.ySize);
		
		//Render the fake trade button
		FluidTradeButton.renderFluidTradeButton(matrix, this, font, startX, startY - FluidTradeButton.HEIGHT, this.container.tradeIndex, this.container.traderSource.get(), false, true, false);
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		this.font.drawString(matrix, new TranslationTextComponent("gui.lctech.fluid_edit.title").getString(), 8.0f, 6.0f, 0x404040);
	}
	
	protected void init() {
		super.init();
		
		//Initialize the search field
		this.searchField = new TextFieldWidget(this.font, guiLeft + 81, guiTop + 6, 79, 9, new TranslationTextComponent("gui.lightmanscurrency.item_edit.search"));
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setMaxStringLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		this.children.add(this.searchField);
		
		//Initialize thie buttons
		//Page Buttons
		this.buttonPageLeft = this.addButton(new IconButton(this.guiLeft - 20, this.guiTop, this::PressPageButton, this.font, IconData.of(GUI_TEXTURE, this.xSize, 0)));
		this.buttonPageRight = this.addButton(new IconButton(this.guiLeft + this.xSize, this.guiTop, this::PressPageButton, this.font, IconData.of(GUI_TEXTURE, this.xSize + 16, 0)));
		
		//Close Button
		this.addButton(new Button(this.guiLeft + 7, this.guiTop + 129, 162, 20, new TranslationTextComponent("gui.button.lightmanscurrency.back"), this::PressCloseButton));
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
		
		this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
		
		FluidTradeButton.tryRenderTooltip(matrixStack, this, this.container.tradeIndex, this.container.traderSource.get(), this.guiLeft, this.guiTop - FluidTradeButton.HEIGHT, mouseX, mouseY, true);
		
	}
	
	@Override
	public void tick()
	{
		
		this.searchField.tick();
		
		this.buttonPageLeft.active = this.container.getPage() > 0;
		this.buttonPageRight.active = this.container.getPage() < this.container.maxPage();
		
		if(!firstTick) {
			firstTick = true;
			this.container.refreshPage();
		}
	}
	
	@Override
	public boolean charTyped(char c, int code)
	{
		String s = this.searchField.getText();
		if(this.searchField.charTyped(c, code))
		{
			if(!Objects.equals(s, this.searchField.getText()))
			{
				container.modifySearch(this.searchField.getText());
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods)
	{
		String s = this.searchField.getText();
		if(this.searchField.keyPressed(key, scanCode, mods))
		{
			if(!Objects.equals(s,  this.searchField.getText()))
			{
				container.modifySearch(this.searchField.getText());
			}
			return true;
		}
		return this.searchField.isFocused() && this.searchField.getVisible() && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
	}
	
	private void PressPageButton(Button button)
	{
		int direction = 1;
		if(button == this.buttonPageLeft)
			direction = -1;
		
		container.modifyPage(direction);
	}
	
	private void PressCloseButton(Button button)
	{
		this.container.openTraderStorage();
	}
	

}
