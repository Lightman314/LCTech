package io.github.lightman314.lctech.client.gui.screen.inventory.traderinterface.energy;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lctech.common.menu.traderinterface.energy.EnergyStorageTab;
import io.github.lightman314.lctech.common.util.EnergyUtil;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class EnergyStorageClientTab extends TraderInterfaceClientTab<EnergyStorageTab> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LCTech.MODID, "textures/gui/energy_trade_extras.png");
	
	private static final int X_OFFSET = 13;
	private static final int Y_OFFSET = 17;
	private static final int FRAME_HEIGHT = 90;
	private static final int ENERGY_BAR_HEIGHT = FRAME_HEIGHT - 2;
	
	private static final int WIDGET_OFFSET = 53;
	
	DirectionalSettingsWidget inputSettings;
	DirectionalSettingsWidget outputSettings;
	
	public EnergyStorageClientTab(TraderInterfaceScreen screen, EnergyStorageTab commonTab) { super(screen, commonTab); }

	@Override
	public @Nonnull IconData getIcon() { return IconData.of(IBatteryItem.getFullBattery(ModItems.BATTERY_LARGE.get())); }
	
	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.interface.storage"); }
	
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
	public void onOpen() {
		
		this.inputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this.getInputSettings()::get, this.getInputSettings().ignoreSides, this::ToggleInputSide, this.screen::addRenderableTabWidget);
		this.outputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 116, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this.getOutputSettings()::get, this.getOutputSettings().ignoreSides, this::ToggleOutputSide, this.screen::addRenderableTabWidget);
		
	}
	
	@Override
	public void renderBG(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.font.draw(pose, EasyText.translatable("tooltip.lightmanscurrency.interface.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040);
		
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity)
		{
			
			EnergyTraderInterfaceBlockEntity be = (EnergyTraderInterfaceBlockEntity)this.menu.getBE();
			
			//Render the slot bg for the upgrade/battery slots
			RenderUtil.bindTexture(TraderInterfaceScreen.GUI_TEXTURE);
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
			double fillPercent = (double)be.getStoredEnergy() / (double)be.getMaxEnergy();
			int fillHeight = MathUtil.clamp((int)(ENERGY_BAR_HEIGHT * fillPercent), 0, ENERGY_BAR_HEIGHT);
			int yOffset = ENERGY_BAR_HEIGHT - fillHeight + 1;
			this.screen.blit(pose, this.screen.getGuiLeft() + X_OFFSET, this.screen.getGuiTop() + Y_OFFSET + yOffset, 18, yOffset, 18, fillHeight);
			
			//Render the input/output labels
			this.font.draw(pose, EasyText.translatable("gui.lctech.settings.fluidinput.side"), this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040);
			int textWidth = this.font.width(EasyText.translatable("gui.lctech.settings.fluidoutput.side"));
			this.font.draw(pose, EasyText.translatable("gui.lctech.settings.fluidoutput.side"), this.screen.getGuiLeft() + 173 - textWidth, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040);
			
		}
		
	}
	
	@Override
	public void renderTooltips(MatrixStack pose, int mouseX, int mouseY) {
		
		if(this.menu.getBE() instanceof EnergyTraderInterfaceBlockEntity && this.isMouseOverEnergy(mouseX, mouseY))
		{
			EnergyTraderInterfaceBlockEntity be = (EnergyTraderInterfaceBlockEntity)this.menu.getBE();
			this.screen.renderTooltip(pose, EasyText.literal(EnergyUtil.formatEnergyAmount(be.getStoredEnergy()) + "/" + EnergyUtil.formatEnergyAmount(be.getMaxEnergy())).withStyle(TextFormatting.AQUA), mouseX, mouseY);
		}
		
		this.inputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
		this.outputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
		
	}
	
	private boolean isMouseOverEnergy(int mouseX, int mouseY) {
		int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
		int topEdge = this.screen.getGuiTop() + Y_OFFSET;
		return mouseX >= leftEdge && mouseX < leftEdge + 18 && mouseY >= topEdge && mouseY < topEdge + FRAME_HEIGHT;
	}
	
	@Override
	public void tick() {
		this.inputSettings.tick();
		this.outputSettings.tick();
	}
	
	private void ToggleInputSide(Direction side) {
		this.commonTab.toggleInputSlot(side);
	}
	
	private void ToggleOutputSide(Direction side) {
		this.commonTab.toggleOutputSlot(side);
	}
	
}
