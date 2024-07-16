package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.menu.traderstorage.fluid.FluidStorageTab;
import io.github.lightman314.lctech.common.util.FluidFormatUtil;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;

public class FluidStorageClientTab extends TraderStorageClientTab<FluidStorageTab> implements IScrollable, IMouseListener {
	
	public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LCTech.MODID, "textures/gui/fluid_trade_extras.png");
	
	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int TANKS = 8;

	public FluidStorageClientTab(Object screen, FluidStorageTab tab) { super(screen, tab); }

	int scroll = 0;
	
	ScrollBarWidget scrollBar;
	
	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.IRON_TANK); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_STORAGE.get(); }
	
	@Override
	public boolean tabButtonVisible() { return true; }
	
	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.addChild(this);

		this.scrollBar = this.addChild(new ScrollBarWidget(screenArea.pos.offset(X_OFFSET + (18 * TANKS), Y_OFFSET), 90, this));
		
		this.addChild(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getXSize(), 118, this));
		
	}

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(LCText.TOOLTIP_TRADER_STORAGE.get(), 8, 6, 0x404040);
		
		if(this.menu.getTrader() instanceof FluidTraderData trader)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each tank
			int index = this.scroll;
			TraderFluidStorage storage = trader.getStorage();
			int yPos = Y_OFFSET;
			for(int x = 0; x < TANKS && index < storage.getTanks(); ++x)
			{
				int xPos = X_OFFSET + x * 18;
				FluidEntry entry = storage.getContents().get(index);
				//Render the filter fluid
				gui.renderItem(FluidItemUtil.getFluidDisplayItem(entry.filter), xPos + 1, yPos);
				//Render the drain/fillable buttons
				gui.resetColor();
				if(trader.drainCapable())
				{
					gui.blit(GUI_TEXTURE, xPos + 1, yPos + 16, entry.drainable ? 0 : 8, 0, 8, 8);
					gui.blit(GUI_TEXTURE, xPos + 9, yPos + 16, entry.fillable ? 16 : 24, 0, 8, 8);
				}
				//Render the tank bg
				gui.blit(GUI_TEXTURE, xPos, yPos + 24, 0, 16, 18, 66);
				//Render the fluid in the tank
				FluidRenderUtil.drawFluidTankInGUI(entry.filter, this.screen.getCorner(), xPos + 1, yPos + 25, 16, 64, (float)entry.getStoredAmount() / (float)storage.getTankCapacity());
				//Render the tank overlay (glass)
				gui.resetColor();
				gui.blit(GUI_TEXTURE, xPos, yPos + 24, 18, 16, 18, 66);
				
				index++;
			}
			
			//Render the slot bg for the upgrade slots
			RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			gui.resetColor();
			for(Slot slot : this.commonTab.getSlots())
				gui.blit(TraderScreen.GUI_TEXTURE, slot.x - 1, slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
		}
		
	}

	@Override
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		if(this.menu.getTrader() instanceof FluidTraderData)
		{
			TraderFluidStorage storage = ((FluidTraderData)this.menu.getTrader()).getStorage();
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
				//Pending drain
				if(entry.hasPendingDrain())
					tooltips.add(TechText.TOOLTIP_FLUID_PENDING_DRAIN.get(FluidFormatUtil.formatFluidAmount(entry.getPendingDrain())));
				tooltips.add(TechText.TOOLTIP_FLUID_INTERACT.get());
				gui.renderComponentTooltip(tooltips);
			}
			Pair<Integer,Boolean> hoveredToggle = this.isMouseOverDrainFill(gui.mousePos);
			if(hoveredToggle != null)
			{
				int tank = hoveredToggle.getFirst() + this.scroll;
				boolean drainState = hoveredToggle.getSecond();
				if(tank < 0 || tank >= storage.getTanks())
					return;
				FluidEntry entry = storage.getContents().get(tank);
				if(drainState)
					gui.renderTooltip(entry.drainable ? TechText.TOOLTIP_FLUID_SETTINGS_DRAIN_ENABLED.getWithStyle(ChatFormatting.GREEN) : TechText.TOOLTIP_FLUID_SETTINGS_DRAIN_DISABLED.getWithStyle(ChatFormatting.RED));
				else
					gui.renderTooltip(entry.fillable ? TechText.TOOLTIP_FLUID_SETTINGS_FILL_ENABLED.getWithStyle(ChatFormatting.GREEN) : TechText.TOOLTIP_FLUID_SETTINGS_FILL_DISABLED.getWithStyle(ChatFormatting.RED));
			}
		}
	}
	
	private Pair<Integer,Boolean> isMouseOverDrainFill(ScreenPosition mousePos)
	{
		if(this.menu.getTrader() instanceof FluidTraderData trader)
		{
			if(!trader.drainCapable())
				return null;
			int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
			int topEdge = this.screen.getGuiTop() + Y_OFFSET + 16;
			
			if(mousePos.y < topEdge || mousePos.y >= topEdge + 8)
				return null;
			
			for(int x = 0; x < TANKS; ++x)
			{
				if(mousePos.x >= leftEdge + (x * 18) + 1 && mousePos.x < leftEdge + (x * 18) + 9)
					return Pair.of(x, true);
				else if(mousePos.x >= leftEdge + (x * 18) + 9 && mousePos.x < leftEdge + (x * 18) + 17)
					return Pair.of(x, false);
			}
		}
		return null;
	}
	
	private int isMouseOverTank(ScreenPosition mousePos) {
		
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET + 24;
		
		if(mousePos.y < topEdge || mousePos.y >= topEdge + 66)
			return -1;
		
		for(int x = 0; x < TANKS; ++x)
		{
			if(mousePos.x >= leftEdge + x * 18 && mousePos.x < leftEdge + (x * 18) + 18)
				return x;
		}
		return -1;
	}
	
	private int totalTankSlots() {
		if(this.menu.getTrader() instanceof FluidTraderData)
		{
			return ((FluidTraderData)this.menu.getTrader()).getStorage().getTanks();
		}
		return 0;
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		
		if(this.menu.getTrader() instanceof FluidTraderData trader)
		{
			ScreenPosition mousePos = ScreenPosition.of(mouseX, mouseY);
			int hoveredSlot = this.isMouseOverTank(mousePos);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll;
				this.commonTab.interactWithTank(hoveredSlot, Screen.hasShiftDown());
				return true;
			}
			Pair<Integer,Boolean> hoveredToggle = this.isMouseOverDrainFill(mousePos);
			if(hoveredToggle != null)
			{
				int tank = hoveredToggle.getFirst() + this.scroll;
				boolean drainState = hoveredToggle.getSecond();
				TraderFluidStorage storage = trader.getStorage();
				if(tank < 0 || tank >= storage.getTanks())
					return false;
				boolean currentState = drainState ? storage.getContents().get(tank).drainable : storage.getContents().get(tank).fillable;
				this.commonTab.toggleDrainFillState(tank, hoveredToggle.getSecond(), !currentState);
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

}
