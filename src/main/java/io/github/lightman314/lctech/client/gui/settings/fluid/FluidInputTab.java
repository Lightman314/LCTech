package io.github.lightman314.lctech.client.gui.settings.fluid;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.trader.permissions.FluidPermissions;
import io.github.lightman314.lctech.trader.settings.FluidTraderSettings;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

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
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lctech.settings.fluidinput"); }
	
	@Override
	public boolean canOpen() { return this.hasPermissions(FluidPermissions.EDIT_INPUTS); }
	
	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.inputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 20, screen.guiTop() + 25, () -> this.getSetting(FluidTraderSettings.class).getInputSides(), this::ToggleInputSide, screen::addRenderableTabWidget);
		this.outputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 120, screen.guiTop() + 25, () -> this.getSetting(FluidTraderSettings.class).getOutputSides(), this::ToggleOutputSide, screen::addRenderableTabWidget);
		
	}
	
	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		
		TraderSettingsScreen screen = this.getScreen();
		
		//Side Widget Labels
		this.getFont().draw(pose, Component.translatable("gui.lctech.settings.fluidinput.side"), screen.guiLeft() + 20, screen.guiTop() + 7, textColor);
		this.getFont().draw(pose, Component.translatable("gui.lctech.settings.fluidoutput.side"), screen.guiLeft() + 120, screen.guiTop() + 7, textColor);
		
	}
	
	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks)
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
		FluidTraderSettings settings = this.getSetting(FluidTraderSettings.class);
		CompoundTag updateInfo = settings.toggleInputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
	private void ToggleOutputSide(Direction side)
	{
		FluidTraderSettings settings = this.getSetting(FluidTraderSettings.class);
		CompoundTag updateInfo = settings.toggleOutputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
}
