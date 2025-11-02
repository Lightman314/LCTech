package io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.fluid;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.gui.TechSprites;
import io.github.lightman314.lctech.common.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.common.blockentities.handler.FluidInterfaceHandler;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.menu.traderinterface.fluid.FluidStorageTab;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.settings.client.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidStorageClientTab extends TraderInterfaceClientTab<FluidStorageTab> implements IScrollable, IMouseListener {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int TANKS = 8;
	
	private static final int WIDGET_OFFSET = 72;
	
	ScrollBarWidget scrollBar;
	
	public FluidStorageClientTab(Object screen, FluidStorageTab commonTab) { super(screen, commonTab); }

	int scroll = 0;

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.IRON_TANK); }
	
	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_STORAGE.get(); }

	private FluidInterfaceHandler getFluidData() {
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity be)
			return be.getFluidHandler();
		return null;
	}
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.addChild(this);

		this.scrollBar = this.addChild(ScrollBarWidget.builder()
				.position(screenArea.pos.offset(X_OFFSET + (18 * TANKS),53))
				.scrollable(this)
				.smallKnob()
				.build());
		
		this.addChild(ScrollListener.builder()
				.position(screenArea.pos)
				.size(screenArea.width, 118)
				.listener(this)
				.build());

		this.addChild(DirectionalSettingsWidget.builder()
				.position(screenArea.pos.offset(screenArea.width / 2,WIDGET_OFFSET + 9))
				.object(this::getFluidData)
				.handlers(this::ToggleSide)
				.build());

	}
	
	@Override
	public void renderBG(EasyGuiGraphics gui) {

		gui.drawString(LCText.TOOLTIP_INTERFACE_STORAGE.get(), 8, 6, 0x404040);
		
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity be)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each tank
			int index = this.scroll;
			TraderFluidStorage storage = be.getFluidBuffer();
			int yPos = Y_OFFSET;
			for(int x = 0; x < TANKS && index < storage.getTanks(); ++x)
			{
				int xPos = X_OFFSET + x * 18;
				FluidEntry entry = storage.getContents().get(index);
				//Render the filter fluid
				//ItemRenderUtil.drawItemStack(this.screen, this.font, FluidItemUtil.getFluidDisplayItem(entry.filter), xPos + 1, yPos);
				//Render the tank bg
				gui.resetColor();
                TechSprites.TANK_BACKGROUND.render(gui,xPos,yPos,53);
				//Render the fluid in the tank
				FluidRenderUtil.drawFluidTankInGUI(entry.filter, this.screen.getCorner(), xPos + 1, yPos + 1, 16, 51, (float)entry.getStoredAmount() / (float)storage.getTankCapacity());
				//Render the tank overlay (glass)
				gui.resetColor();
                TechSprites.TANK_FOREGROUND.render(gui,xPos,yPos,53);
				
				index++;
			}
			
			//Render the slot bg for the upgrade slots
			gui.resetColor();
			for(Slot slot : this.commonTab.getSlots())
                gui.renderSlot(this.screen,slot);

			//Render the input/output labels
			TextRenderUtil.drawCenteredText(gui,LCText.GUI_SETTINGS_INPUT_SIDE.get(),this.screen.getXSize() / 2, WIDGET_OFFSET, 0x404040);
			
		}
		
	}
	
	@Override
	public void renderAfterWidgets(EasyGuiGraphics gui) {
		
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity be)
		{
			TraderFluidStorage storage = be.getFluidBuffer();
			int hoveredSlot = this.isMouseOverTank(gui.mousePos);
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
				tooltips.add(EasyText.literal(FluidFormatUtil.formatFluidAmount(entry.getStoredAmount()) + "mB/" + FluidFormatUtil.formatFluidAmount(storage.getTankCapacity()) + "mB").withStyle(ChatFormatting.GRAY));
				tooltips.add(TechText.TOOLTIP_FLUID_INTERACT.get());
				gui.renderComponentTooltip(tooltips);
			}
		}
	}
	
	private int isMouseOverTank(ScreenPosition mousePos) {
		
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		
		if(mousePos.y < topEdge || mousePos.y >= topEdge + 53)
			return -1;
		
		for(int x = 0; x < TANKS; ++x)
		{
			if(mousePos.x >= leftEdge + x * 18 && mousePos.x < leftEdge + (x * 18) + 18)
				return x;
		}
		return -1;
	}
	
	private int totalTankSlots() {
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
		{
			return ((FluidTraderInterfaceBlockEntity)this.menu.getBE()).getFluidBuffer().getTanks();
		}
		return 0;
	}
	
	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		
		if(this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity)
		{
			int hoveredSlot = this.isMouseOverTank(ScreenPosition.of(mouseX, mouseY));
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll;
				this.commonTab.interactWithTank(hoveredSlot, Screen.hasShiftDown());
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }
	
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
	
	private void ToggleSide(Direction side, boolean inverse)
	{
		FluidInterfaceHandler data = this.getFluidData();
		if(data != null)
		{
			DirectionalSettingsState state = data.getSidedState(side);
			if(inverse)
				state = state.getPrevious(data);
			else
				state = state.getNext(data);
			this.commonTab.toggleSide(side,state);
		}
	}

	@Nullable
	@Override
	public Pair<FluidStack, ScreenArea> getHoveredFluid(ScreenPosition mousePos) {

		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;

		if(mousePos.y < topEdge || mousePos.y >= topEdge + 53)
			return null;

		int tankIndex = -1;
		for(int x = 0; x < TANKS; ++x)
		{
			if(mousePos.x >= leftEdge + x * 18 && mousePos.x < leftEdge + (x * 18) + 18)
				tankIndex = x;
		}
		if(tankIndex >= 0 && this.menu.getBE() instanceof FluidTraderInterfaceBlockEntity be)
		{
			FluidStack contents = be.getFluidBuffer().getFluidInTank(tankIndex + this.scroll);
			if(contents.isEmpty())
				return null;
			return Pair.of(contents,ScreenArea.of(leftEdge + (tankIndex * 18),topEdge, 18,53));
		}
		return super.getHoveredFluid(mousePos);
	}
}
