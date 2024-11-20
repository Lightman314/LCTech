package io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.energy;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lctech.common.menu.traderinterface.energy.EnergyStorageTab;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;

public class EnergyStorageClientTab extends TraderInterfaceClientTab<EnergyStorageTab> {

	public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LCTech.MODID, "textures/gui/energy_trade_extras.png");
	
	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int FRAME_HEIGHT = 90;
	private static final int ENERGY_BAR_HEIGHT = FRAME_HEIGHT - 2;
	
	private static final int WIDGET_OFFSET = 43;
	
	DirectionalSettingsWidget inputSettings;
	DirectionalSettingsWidget outputSettings;
	
	public EnergyStorageClientTab(Object screen, EnergyStorageTab commonTab) { super(screen, commonTab); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(IBatteryItem.HideEnergyBar(ModItems.BATTERY_LARGE)); }
	
	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_INTERFACE_STORAGE.get(); }
	
	@Override
	public boolean blockInventoryClosing() { return false; }
	
	private DirectionalSettings getInputSettings() {
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity)
			return ((EnergyTraderInterfaceBlockEntity)this.menu.getBE()).getEnergyHandler().getInputSides();
		return new DirectionalSettings();
	}
	
	private DirectionalSettings getOutputSettings() { 
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity)
			return ((EnergyTraderInterfaceBlockEntity)this.menu.getBE()).getEnergyHandler().getOutputSides();
		return new DirectionalSettings();
	}
	
	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) {

		this.inputSettings = this.addChild(DirectionalSettingsWidget.builder()
				.position(screenArea.pos.offset(33,WIDGET_OFFSET + 9))
				.currentValue(this.getInputSettings()::get)
				.ignore(this.getInputSettings().ignoreSides)
				.handler(this::ToggleInputSide)
				.build());
		this.outputSettings = this.addChild(DirectionalSettingsWidget.builder()
				.position(screenArea.pos.offset(116,WIDGET_OFFSET + 9))
				.currentValue(this.getOutputSettings()::get)
				.ignore(this.getOutputSettings().ignoreSides)
				.handler(this::ToggleOutputSide)
				.build());
		
	}
	
	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(LCText.TOOLTIP_INTERFACE_STORAGE.get(), 8, 6, 0x404040);
		
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be)
		{

			//Render the slot bg for the upgrade/battery slots
			gui.resetColor();
			for(Slot slot : this.commonTab.getSlots())
				gui.blit(TraderInterfaceScreen.GUI_TEXTURE, slot.x - 1, slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);

			//Render the up arrow
			gui.blit(GUI_TEXTURE, TraderMenu.SLOT_OFFSET + 11, 110, 46, 0, 10, 10);
			//Render the down arrow
			gui.blit(GUI_TEXTURE, TraderMenu.SLOT_OFFSET + 47, 110, 36, 0, 10, 10);
			
			//Render the background for the energy bar
			gui.blit(GUI_TEXTURE, X_OFFSET, Y_OFFSET, 0, 0, 18, FRAME_HEIGHT);
			
			//Render the energy bar
			double fillPercent = (double)be.getStoredEnergy() / (double)be.getMaxEnergy();
			int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
			int yOffset = ENERGY_BAR_HEIGHT - fillHeight + 1;
			gui.blit(GUI_TEXTURE, X_OFFSET, Y_OFFSET + yOffset, 18, yOffset, 18, fillHeight);
			
			//Render the input/output labels
			gui.drawString(LCText.GUI_SETTINGS_INPUT_SIDE.get(), 33, WIDGET_OFFSET, 0x404040);
			Component text = LCText.GUI_SETTINGS_OUTPUT_SIDE.get();
			int textWidth = gui.font.width(text);
			gui.drawString(text, 173 - textWidth, WIDGET_OFFSET, 0x404040);
			
		}
		
	}
	
	@Override
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity be && this.isMouseOverEnergy(gui.mousePos))
			gui.renderTooltip(EasyText.literal(EnergyUtil.formatEnergyAmount(be.getStoredEnergy()) + "/" + EnergyUtil.formatEnergyAmount(be.getMaxEnergy())).withStyle(ChatFormatting.AQUA));
	}
	
	private boolean isMouseOverEnergy(ScreenPosition mousePos) {
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		return mousePos.x >= leftEdge && mousePos.x < leftEdge + 18 && mousePos.y >= topEdge && mousePos.y < topEdge + FRAME_HEIGHT;
	}
	
	private void ToggleInputSide(Direction side) {
		this.commonTab.toggleInputSlot(side);
	}
	
	private void ToggleOutputSide(Direction side) {
		this.commonTab.toggleOutputSlot(side);
	}
	
}
