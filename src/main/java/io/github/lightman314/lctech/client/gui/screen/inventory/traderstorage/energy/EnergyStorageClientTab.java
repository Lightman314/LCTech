package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lctech.common.menu.traderstorage.energy.EnergyStorageTab;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;

public class EnergyStorageClientTab extends TraderStorageClientTab<EnergyStorageTab> {

	public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LCTech.MODID, "textures/gui/energy_trade_extras.png");
	
	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int FRAME_HEIGHT = 90;
	private static final int ENERGY_BAR_HEIGHT = FRAME_HEIGHT - 2;
	
	public EnergyStorageClientTab(Object screen, EnergyStorageTab commonTab) { super(screen, commonTab); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(IBatteryItem.HideEnergyBar(ModItems.BATTERY_LARGE)); }

	@Override
	public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_STORAGE.get(); }

	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public void initialize(ScreenArea screenArea, boolean firstOpen) { }

	@Override
	public void renderBG(@Nonnull EasyGuiGraphics gui) {

		gui.drawString(LCText.TOOLTIP_TRADER_STORAGE.get(), 8, 6, 0x404040);
		
		if(this.menu.getTrader() instanceof EnergyTraderData trader)
		{

			//Render the slot bg for the upgrade/battery slots
			gui.resetColor();
			for(Slot slot : this.commonTab.getSlots())
				gui.blit(TraderScreen.GUI_TEXTURE, slot.x - 1, slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);

			//Render the up arrow
			gui.blit(GUI_TEXTURE, TraderMenu.SLOT_OFFSET + 11, 110, 46, 0, 10, 10);
			//Render the down arrow
			gui.blit(GUI_TEXTURE, TraderMenu.SLOT_OFFSET + 47, 110, 36, 0, 10, 10);
			
			//Render the background for the energy bar
			gui.blit(GUI_TEXTURE, X_OFFSET, Y_OFFSET, 0, 0, 18, FRAME_HEIGHT);
			
			//Render the energy bar
			double fillPercent = (double)trader.getTotalEnergy() / (double)trader.getMaxEnergy();
			int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
			int yOffset = ENERGY_BAR_HEIGHT - fillHeight + 1;
			gui.blit(GUI_TEXTURE, X_OFFSET, Y_OFFSET + yOffset, 18, yOffset, 18, fillHeight);
			
		}
		
	}

	@Override
	public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
		
		if(this.menu.getTrader() instanceof EnergyTraderData trader && this.isMouseOverEnergy(gui.mousePos))
			gui.renderComponentTooltip(EnergyTraderData.getEnergyHoverTooltip(trader));
		
	}
	
	private boolean isMouseOverEnergy(ScreenPosition mousePos) {
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		return mousePos.x >= leftEdge && mousePos.x < leftEdge + 18 && mousePos.y >= topEdge && mousePos.y < topEdge + FRAME_HEIGHT;
	}
	
}
