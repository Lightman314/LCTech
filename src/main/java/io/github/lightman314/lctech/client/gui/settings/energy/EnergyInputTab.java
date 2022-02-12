package io.github.lightman314.lctech.client.gui.settings.energy;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lctech.core.ModItems;
import io.github.lightman314.lctech.trader.permissions.EnergyPermissions;
import io.github.lightman314.lctech.trader.settings.EnergyTraderSettings;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class EnergyInputTab extends SettingsTab{

	public static final EnergyInputTab INSTANCE = new EnergyInputTab();
	
	private EnergyInputTab() { }
	
	DirectionalSettingsWidget inputWidget;
	DirectionalSettingsWidget outputWidget;
	
	private final int textColor = 0xD0D0D0;
	
	@Override
	public int getColor() { return 0x00FFFF; }
	
	@Override
	public IconData getIcon() { return IconData.of(ModItems.BATTERY); }
	
	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lctech.settings.energyinput"); }
	
	@Override
	public ImmutableList<String> requiredPermissions() { return ImmutableList.of(EnergyPermissions.EDIT_INPUTS); }
	
	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.inputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 20, screen.guiTop() + 25, () -> this.getSetting(EnergyTraderSettings.class).getInputSides(), this::ToggleInputSide, screen::addRenderableTabWidget);
		this.outputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 120, screen.guiTop() + 25, () -> this.getSetting(EnergyTraderSettings.class).getOutputSides(), this::ToggleOutputSide, screen::addRenderableTabWidget);
		
	}
	
	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		
		TraderSettingsScreen screen = this.getScreen();
		
		//Side Widget Labels
		this.getFont().drawString(pose, new TranslationTextComponent("gui.lctech.settings.energyinput.side").getString(), screen.guiLeft() + 20, screen.guiTop() + 7, textColor);
		this.getFont().drawString(pose, new TranslationTextComponent("gui.lctech.settings.energyoutput.side").getString(), screen.guiLeft() + 120, screen.guiTop() + 7, textColor);
		
	}
	
	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		
		this.inputWidget.renderTooltips(pose, mouseX, mouseY, this.getScreen());
		this.outputWidget.renderTooltips(pose, mouseX, mouseY, this.getScreen());
		
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
		EnergyTraderSettings settings = this.getSetting(EnergyTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleInputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
	private void ToggleOutputSide(Direction side)
	{
		EnergyTraderSettings settings = this.getSetting(EnergyTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleOutputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
}
