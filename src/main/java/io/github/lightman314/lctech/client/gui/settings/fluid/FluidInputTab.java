package io.github.lightman314.lctech.client.gui.settings.fluid;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.trader.permissions.FluidPermissions;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidInputTab extends SettingsTab{

	public static final FluidInputTab INSTANCE = new FluidInputTab();
	
	private FluidInputTab() { }
	
	DirectionalSettingsWidget inputWidget;
	DirectionalSettingsWidget outputWidget;
	
	private final int textColor = 0xD0D0D0;
	
	@Override
	public int getColor() { return 0x007FFF; }
	
	@Override
	public IconData getIcon() { return IconData.of(Items.WATER_BUCKET); }
	
	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lctech.settings.fluidinput"); }
	
	@Override
	public ImmutableList<String> requiredPermissions() { return ImmutableList.of(FluidPermissions.EDIT_INPUTS); }
	
	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.inputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 20, screen.guiTop() + 25, () -> this.getSetting(FluidTraderSettings.class).getInputSides(), this::ToggleInputSide, screen::addRenderableTabWidget);
		this.outputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 120, screen.guiTop() + 25, () -> this.getSetting(FluidTraderSettings.class).getOutputSides(), this::ToggleOutputSide, screen::addRenderableTabWidget);
		
	}
	
	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		
		TraderSettingsScreen screen = this.getScreen();
		
		//Side Widget Labels
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lctech.settings.fluidinput.side").getString(), screen.guiLeft() + 20, screen.guiTop() + 7, textColor);
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lctech.settings.fluidoutput.side").getString(), screen.guiLeft() + 120, screen.guiTop() + 7, textColor);
		
	}
	
	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		
		this.inputWidget.renderTooltips(matrix, mouseX, mouseY, this.getScreen());
		this.outputWidget.renderTooltips(matrix, mouseX, mouseY, this.getScreen());
		
	}
	
	@Override
	public void tick()
	{
		this.inputWidget.tick();
		this.outputWidget.tick();
	}
	
	@Override
	public void closeTab() {
		
	}
	
	private void ToggleInputSide(Direction side)
	{
		FluidTraderSettings settings = this.getSetting(FluidTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleInputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
	private void ToggleOutputSide(Direction side)
	{
		FluidTraderSettings settings = this.getSetting(FluidTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleOutputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
}
