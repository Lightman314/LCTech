package io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.energy;

import io.github.lightman314.lctech.client.gui.TechSprites;
import io.github.lightman314.lctech.common.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.blockentities.handler.EnergyInterfaceHandler;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lctech.common.menu.traderinterface.energy.EnergyStorageTab;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.settings.client.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyStorageClientTab extends TraderInterfaceClientTab<EnergyStorageTab> {

	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int FRAME_HEIGHT = 90;
	private static final int ENERGY_BAR_HEIGHT = FRAME_HEIGHT - 2;
	
	private static final int WIDGET_OFFSET = 43;
	
	public EnergyStorageClientTab(Object screen, EnergyStorageTab commonTab) { super(screen, commonTab); }

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(IBatteryItem.HideEnergyBar(ModItems.BATTERY_LARGE)); }
	
	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_STORAGE.get(); }

	private EnergyInterfaceHandler getEnergyData() {
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be)
			return be.getEnergyHandler();
		return null;
	}

	@Override
	public boolean blockInventoryClosing() { return false; }
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.addChild(DirectionalSettingsWidget.builder()
				.position(screenArea.pos.offset(screenArea.width / 2,WIDGET_OFFSET + 9))
				.object(this::getEnergyData)
				.handlers(this::ToggleSide)
				.build());
		
	}
	
	@Override
	public void renderBG(EasyGuiGraphics gui) {

		gui.drawString(LCText.TOOLTIP_INTERFACE_STORAGE.get(), 8, 6, 0x404040);
		
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be)
		{

			//Render the slot bg for the upgrade/battery slots
			gui.resetColor();
			for(Slot slot : this.commonTab.getSlots())
                gui.renderSlot(this.screen,slot);

			//Render the up arrow
            SpriteUtil.SMALL_ARROW_UP.render(gui,TraderMenu.SLOT_OFFSET + 11, 110);
			//Render the down arrow
            SpriteUtil.SMALL_ARROW_DOWN.render(gui,TraderMenu.SLOT_OFFSET + 47, 110);
			
			//Render the background for the energy bar
            TechSprites.BATTERY_BACKGROUND.render(gui,X_OFFSET,Y_OFFSET,FRAME_HEIGHT);
			
			//Render the energy bar
			double fillPercent = (double)be.getStoredEnergy() / (double)be.getMaxEnergy();
			int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
			int yOffset = ENERGY_BAR_HEIGHT - fillHeight + 1;
            TechSprites.BATTERY_FILLER.render(gui,X_OFFSET + 1,Y_OFFSET + yOffset,fillHeight);

			//Render the input/output labels
			TextRenderUtil.drawCenteredText(gui,LCText.GUI_SETTINGS_INPUT_SIDE.get(),this.screen.getXSize() / 2, WIDGET_OFFSET, 0x404040);
			
		}
		
	}
	
	@Override
	public void renderAfterWidgets(EasyGuiGraphics gui) {
		
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be && this.isMouseOverEnergy(gui.mousePos))
			gui.renderTooltip(EasyText.literal(EnergyUtil.formatEnergyAmount(be.getStoredEnergy()) + "/" + EnergyUtil.formatEnergyAmount(be.getMaxEnergy())).withStyle(ChatFormatting.AQUA));
	}
	
	private boolean isMouseOverEnergy(ScreenPosition mousePos) {
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		return mousePos.x >= leftEdge && mousePos.x < leftEdge + 18 && mousePos.y >= topEdge && mousePos.y < topEdge + FRAME_HEIGHT;
	}
	
	private void ToggleSide(Direction side, boolean inverse)
	{
		EnergyInterfaceHandler data = this.getEnergyData();
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
	
}
