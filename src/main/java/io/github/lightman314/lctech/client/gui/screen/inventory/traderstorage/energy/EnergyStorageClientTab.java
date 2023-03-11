package io.github.lightman314.lctech.client.gui.screen.inventory.traderstorage.energy;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lctech.common.menu.traderstorage.energy.EnergyStorageTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class EnergyStorageClientTab extends TraderStorageClientTab<EnergyStorageTab>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/energy_trade_extras.png");
	
	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int FRAME_HEIGHT = 90;
	private static final int ENERGY_BAR_HEIGHT = FRAME_HEIGHT - 2;
	
	public EnergyStorageClientTab(TraderStorageScreen screen, EnergyStorageTab commonTab) { super(screen, commonTab); }

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(IBatteryItem.getFullBattery(ModItems.BATTERY_LARGE.get())); }

	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.storage"); }
	
	@Override
	public boolean tabButtonVisible() { return true; }

	@Override
	public boolean blockInventoryClosing() { return false; }

	@Override
	public void onOpen() { }

	@Override
	public void renderBG(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		if(this.menu.getTrader() instanceof EnergyTraderData)
		{
			
			EnergyTraderData trader = (EnergyTraderData)this.menu.getTrader();
			
			//Render the slot bg for the upgrade/battery slots
			RenderUtil.bindTexture(TraderScreen.GUI_TEXTURE);
			RenderUtil.color4f(1f, 1f, 1f, 1f);
			for(Slot slot : this.commonTab.getSlots())
			{
				this.screen.blit(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
			}

			RenderUtil.bindTexture(GUI_TEXTURE);
			//Render the arrow between the arrow slots
			this.screen.blit(pose, this.screen.getGuiLeft() + TraderMenu.SLOT_OFFSET + 25, this.screen.getGuiTop() + 121, 36, 0, 18, 18);
			
			//Render the background for the energy bar
			this.screen.blit(pose, this.screen.getGuiLeft() + X_OFFSET, this.screen.getGuiTop() + Y_OFFSET, 0, 0, 18, FRAME_HEIGHT);
			
			//Render the energy bar
			double fillPercent = (double)trader.getTotalEnergy() / (double)trader.getMaxEnergy();
			int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
			int yOffset = ENERGY_BAR_HEIGHT - fillHeight + 1;
			this.screen.blit(pose, this.screen.getGuiLeft() + X_OFFSET, this.screen.getGuiTop() + Y_OFFSET + yOffset, 18, yOffset, 18, fillHeight);
			
		}
		
	}

	@Override
	public void renderTooltips(MatrixStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getTrader() instanceof EnergyTraderData && this.isMouseOverEnergy(mouseX, mouseY))
		{
			this.screen.renderComponentTooltip(pose, EnergyTraderData.getEnergyHoverTooltip((EnergyTraderData)this.menu.getTrader()), mouseX, mouseY);
		}
		
	}
	
	private boolean isMouseOverEnergy(int mouseX, int mouseY) {
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		return mouseX >= leftEdge && mouseX < leftEdge + 18 && mouseY >= topEdge && mouseY < topEdge + FRAME_HEIGHT;
	}
	
}