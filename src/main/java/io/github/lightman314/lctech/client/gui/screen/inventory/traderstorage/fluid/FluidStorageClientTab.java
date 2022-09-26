package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.fluid;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lctech.core.ModBlocks;
import io.github.lightman314.lctech.menu.traderstorage.fluid.FluidStorageTab;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lctech.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class FluidStorageClientTab extends TraderStorageClientTab<FluidStorageTab> implements IScrollListener, IScrollable{
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/fluid_trade_extras.png");
	
	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int TANKS = 8;
	
	public static final int ENABLED_COLOR = 0x00FF00;
	public static final int DISABLED_COLOR = 0xFF0000;
	
	public FluidStorageClientTab(TraderStorageScreen screen, FluidStorageTab tab) { super(screen, tab); }

	int scroll = 0;
	
	ScrollBarWidget scrollBar;
	
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.IRON_TANK); }

	@Override
	public MutableComponent getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.trader.storage"); }
	
	@Override
	public boolean tabButtonVisible() { return true; }
	
	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public void onOpen() {
		
		this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * TANKS), this.screen.getGuiTop() + Y_OFFSET, 90, this));
		
		this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getXSize(), 118, this));
		
	}

	@Override
	public void renderBG(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.font.draw(pose, new TranslatableComponent("gui.lightmanscurrency.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		this.scrollBar.beforeWidgetRender(mouseY);
		
		if(this.menu.getTrader() instanceof FluidTraderData)
		{
			//Validate the scroll
			this.validateScroll();
			//Render each tank
			int index = this.scroll;
			FluidTraderData trader = (FluidTraderData)this.menu.getTrader();
			TraderFluidStorage storage = trader.getStorage();
			int yPos = this.screen.getGuiTop() + Y_OFFSET;
			for(int x = 0; x < TANKS && index < storage.getTanks(); ++x)
			{
				int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
				FluidEntry entry = storage.getContents().get(index);
				//Render the filter fluid
				ItemRenderUtil.drawItemStack(this.screen, this.font, FluidItemUtil.getFluidDisplayItem(entry.filter), xPos + 1, yPos);
				//Render the drain/fillable buttons
				RenderSystem.setShaderTexture(0, GUI_TEXTURE);
				if(trader.drainCapable())
				{
					this.screen.blit(pose, xPos + 1, yPos + 16, entry.drainable ? 0 : 8, 0, 8, 8);
					this.screen.blit(pose, xPos + 9, yPos + 16, entry.fillable ? 16 : 24, 0, 8, 8);
				}
				//Render the tank bg
				this.screen.blit(pose, xPos, yPos + 24, 0, 16, 18, 66);
				//Render the fluid in the tank
				FluidRenderUtil.drawFluidTankInGUI(entry.filter, xPos + 1, yPos + 25, 16, 64, (double)entry.getStoredAmount() / (double)storage.getTankCapacity());
				//Render the tank overlay (glass)
				RenderSystem.setShaderTexture(0, GUI_TEXTURE);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				this.screen.blit(pose, xPos, yPos + 24, 18, 16, 18, 66);
				
				index++;
			}
			
			//Render the slot bg for the upgrade slots
			RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			for(Slot slot : this.commonTab.getSlots())
			{
				this.screen.blit(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
			}
		}
		
	}

	@Override
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY) {
		if(this.menu.getTrader() instanceof FluidTraderData)
		{
			TraderFluidStorage storage = ((FluidTraderData)this.menu.getTrader()).getStorage();
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
				tooltips.add(new TextComponent(FluidFormatUtil.formatFluidAmount(entry.getStoredAmount()) + "mB/" + FluidFormatUtil.formatFluidAmount(storage.getTankCapacity()) + "mB").withStyle(ChatFormatting.GRAY));
				//Pending drain
				if(entry.hasPendingDrain())
					tooltips.add(new TranslatableComponent("gui.lctech.fluidtrade.pending_drain", FluidFormatUtil.formatFluidAmount(entry.getPendingDrain())));
				tooltips.add(new TranslatableComponent("tooltip.lctech.trader.fluid.fill_tank"));
				this.screen.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
				
			}
			Pair<Integer,Boolean> hoveredToggle = this.isMouseOverDrainFill(mouseX, mouseY);
			if(hoveredToggle != null)
			{
				int tank = hoveredToggle.getFirst() + this.scroll;
				boolean drainState = hoveredToggle.getSecond();
				if(tank < 0 || tank >= storage.getTanks())
					return;
				FluidEntry entry = storage.getContents().get(tank);
				if(drainState)
					this.screen.renderTooltip(pose, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.drain." + (entry.drainable ? "enabled" : "disabled")).withStyle(Style.EMPTY.withColor(entry.drainable ? ENABLED_COLOR : DISABLED_COLOR)), mouseX, mouseY);
				else
					this.screen.renderTooltip(pose, new TranslatableComponent("tooltip.lctech.trader.fluid_settings.fill." + (entry.fillable ? "enabled" : "disabled")).withStyle(Style.EMPTY.withColor(entry.fillable ? ENABLED_COLOR : DISABLED_COLOR)), mouseX, mouseY);
			}
		}
	}
	
	private void validateScroll() {
		if(this.scroll < 0)
			this.scroll = 0;
		if(this.scroll > this.getMaxScroll())
			this.scroll = this.getMaxScroll();
	}
	
	private Pair<Integer,Boolean> isMouseOverDrainFill(double mouseX, double mouseY)
	{
		if(this.menu.getTrader() instanceof FluidTraderData)
		{
			FluidTraderData trader = (FluidTraderData)this.menu.getTrader();
			if(!trader.drainCapable())
				return null;
			int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
			int topEdge = this.screen.getGuiTop() + Y_OFFSET + 16;
			
			if(mouseY < topEdge || mouseY >= topEdge + 8)
				return null;
			
			for(int x = 0; x < TANKS; ++x)
			{
				if(mouseX >= leftEdge + (x * 18) + 1 && mouseX < leftEdge + (x * 18) + 9)
					return Pair.of(x, true);
				else if(mouseX >= leftEdge + (x * 18) + 9 && mouseX < leftEdge + (x * 18) + 17)
					return Pair.of(x, false);
			}
		}
		return null;
	}
	
	private int isMouseOverTank(double mouseX, double mouseY) {
		
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET + 24;
		
		if(mouseY < topEdge || mouseY >= topEdge + 66)
			return -1;
		
		for(int x = 0; x < TANKS; ++x)
		{
			if(mouseX >= leftEdge + x * 18 && mouseX < leftEdge + (x * 18) + 18)
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
		
		if(this.menu.getTrader() instanceof FluidTraderData)
		{
			int hoveredSlot = this.isMouseOverTank(mouseX, mouseY);
			if(hoveredSlot >= 0)
			{
				hoveredSlot += this.scroll;
				this.commonTab.interactWithTank(hoveredSlot, Screen.hasShiftDown());
				return true;
			}
			Pair<Integer,Boolean> hoveredToggle = this.isMouseOverDrainFill(mouseX, mouseY);
			if(hoveredToggle != null)
			{
				int tank = hoveredToggle.getFirst() + this.scroll;
				boolean drainState = hoveredToggle.getSecond();
				TraderFluidStorage storage = ((FluidTraderData)this.menu.getTrader()).getStorage();
				if(tank < 0 || tank >= storage.getTanks())
					return false;
				boolean currentState = drainState ? storage.getContents().get(tank).drainable : storage.getContents().get(tank).fillable;
				this.commonTab.toggleDrainFillState(tank, hoveredToggle.getSecond(), !currentState);
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

}