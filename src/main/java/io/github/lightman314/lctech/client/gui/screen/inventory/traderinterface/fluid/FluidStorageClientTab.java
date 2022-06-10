package io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.fluid;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.menu.traderinterface.fluid.FluidStorageTab;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.trader.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.trader.settings.directional.DirectionalSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class FluidStorageClientTab extends TraderInterfaceClientTab<FluidStorageTab> implements IScrollListener, IScrollable {
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/fluid_trade_extras.png");
	
	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int TANKS = 8;
	
	private static final int WIDGET_OFFSET = 72;
	
	public static final int ENABLED_COLOR = 0x00FF00;
	public static final int DISABLED_COLOR = 0xFF0000;
	
	ScrollBarWidget scrollBar;
	
	DirectionalSettingsWidget inputSettings;
	DirectionalSettingsWidget outputSettings;
	
	public FluidStorageClientTab(TraderInterfaceScreen screen, FluidStorageTab commonTab) { super(screen, commonTab); }

	int scroll = 0;
	
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.IRON_TANK); }
	
	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.interface.storage"); }
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	private DirectionalSettings getInputSettings() {
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
			return ((FluidTraderInterfaceBlockEntity)this.menu.getBE()).getFluidHandler().getInputSides();
		return new DirectionalSettings();
	}
	
	private DirectionalSettings getOutputSettings() { 
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
			return ((FluidTraderInterfaceBlockEntity)this.menu.getBE()).getFluidHandler().getOutputSides();
		return new DirectionalSettings();
	}
	
	@Override
	public void onOpen() {
		
		this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * TANKS), this.screen.getGuiTop() + Y_OFFSET, 53, this));
		this.scrollBar.smallKnob = true;
		
		this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getXSize(), 118, this));
		
		this.inputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this::getInputSettings, this::ToggleInputSide, this.screen::addRenderableTabWidget);
		this.outputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 116, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this::getOutputSettings, this::ToggleOutputSide, this.screen::addRenderableTabWidget);
		
	}
	
	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.font.draw(pose, Component.translatable("tooltip.lightmanscurrency.interface.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		this.scrollBar.beforeWidgetRender(mouseY);
		
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each tank
			int index = this.scroll;
			FluidTraderInterfaceBlockEntity be = (FluidTraderInterfaceBlockEntity)this.menu.getBE();
			TraderFluidStorage storage = be.getFluidBuffer();
			int yPos = this.screen.getGuiTop() + Y_OFFSET;
			for(int x = 0; x < TANKS && index < storage.getTanks(); ++x)
			{
				int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
				FluidEntry entry = storage.getContents().get(index);
				//Render the filter fluid
				//ItemRenderUtil.drawItemStack(this.screen, this.font, FluidItemUtil.getFluidDisplayItem(entry.filter), xPos + 1, yPos);
				//Render the tank bg
				RenderSystem.setShaderTexture(0, GUI_TEXTURE);
				this.screen.blit(pose, xPos, yPos, 36, 16, 18, 53);
				//Render the fluid in the tank
				FluidRenderUtil.drawFluidTankInGUI(entry.filter, xPos + 1, yPos + 1, 16, 51, (double)entry.getStoredAmount() / (double)storage.getTankCapacity());
				//Render the tank overlay (glass)
				RenderSystem.setShaderTexture(0, GUI_TEXTURE);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				this.screen.blit(pose, xPos, yPos, 54, 16, 18, 53);
				
				index++;
			}
			
			//Render the slot bg for the upgrade slots
			RenderSystem.setShaderTexture(0, TraderInterfaceScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			for(Slot slot : this.commonTab.getSlots())
			{
				this.screen.blit(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
			}
			
			//Render the input/output labels
			this.font.draw(pose, Component.translatable("gui.lctech.settings.fluidinput.side"), this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040);
			int textWidth = this.font.width(Component.translatable("gui.lctech.settings.fluidoutput.side"));
			this.font.draw(pose, Component.translatable("gui.lctech.settings.fluidoutput.side"), this.screen.getGuiLeft() + 173 - textWidth, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040);
			
		}
		
	}
	
	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		
		this.inputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
		this.outputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
		
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
		{
			TraderFluidStorage storage = ((FluidTraderInterfaceBlockEntity)this.menu.getBE()).getFluidBuffer();
			int hoveredSlot = this.isMouseOverTank(mouseX, mouseY);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll;
				if(hoveredSlot < 0 || hoveredSlot >= storage.getTanks())
					return;
				FluidEntry entry = storage.getContents().get(hoveredSlot);
				//Fluid Name
				List<Component> tooltips = new ArrayList<>();
				tooltips.add(FluidFormatUtil.getFluidName(entry.filter));
				//'amount'/'capacity'mB
				tooltips.add(Component.literal(FluidFormatUtil.formatFluidAmount(entry.getStoredAmount()) + "mB/" + FluidFormatUtil.formatFluidAmount(storage.getTankCapacity()) + "mB").withStyle(ChatFormatting.GRAY));
				tooltips.add(Component.translatable("tooltip.lctech.trader.fluid.fill_tank"));
				this.screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
			}
		}
	}
	
	private int isMouseOverTank(double mouseX, double mouseY) {
		
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		
		if(mouseY < topEdge || mouseY >= topEdge + 53)
			return -1;
		
		for(int x = 0; x < TANKS; ++x)
		{
			if(mouseX >= leftEdge + x * 18 && mouseX < leftEdge + (x * 18) + 18)
				return x;
		}
		return -1;
	}
	
	@Override
	public void tick() {
		this.inputSettings.tick();
		this.outputSettings.tick();
	}
	
	private int totalTankSlots() {
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
		{
			return ((FluidTraderInterfaceBlockEntity)this.menu.getBE()).getFluidBuffer().getTanks();
		}
		return 0;
	}
	
	private void validateScroll() {
		if(this.scroll < 0)
			this.scroll = 0;
		if(this.scroll > this.getMaxScroll())
			this.scroll = this.getMaxScroll();
	}
	
	private boolean canScrollDown() { return this.totalTankSlots() - this.scroll > TANKS; }	
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(this.canScrollDown())
				this.scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.scroll > 0)
				scroll--;
			else
				return false;
		}
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
		{
			int hoveredSlot = this.isMouseOverTank(mouseX, mouseY);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll;
				this.commonTab.interactWithTank(hoveredSlot, Screen.hasShiftDown());
				return true;
			}
		}
		this.scrollBar.onMouseClicked(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.scrollBar.onMouseReleased(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public int getMaxScroll() {
		return Math.max(0, this.totalTankSlots() - TANKS);
	}

	@Override
	public void setScroll(int newScroll) {
		this.scroll = newScroll;
		this.validateScroll();
	}
	
	private void ToggleInputSide(Direction side) {
		this.commonTab.toggleInputSlot(side);
	}
	
	private void ToggleOutputSide(Direction side) {
		this.commonTab.toggleOutputSlot(side);
	}
	
}
