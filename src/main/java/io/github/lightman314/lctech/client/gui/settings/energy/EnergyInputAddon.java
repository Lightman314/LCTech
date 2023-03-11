package io.github.lightman314.lctech.client.gui.settings.energy;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;

public class EnergyInputAddon extends InputTabAddon {

	public static final EnergyInputAddon INSTANCE = new EnergyInputAddon();
	
	private EnergyInputAddon() {}
	
	Button buttonDrainMode;
	
	@Override
	public void onInit(TraderSettingsScreen screen) {
		
		this.buttonDrainMode = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 100, screen.xSize - 40, 20, EasyText.empty(), b -> this.ToggleDrainMode(screen)));
		
		this.tick(screen);
		
	}
	
	@Override
	public void preRender(TraderSettingsScreen screen, MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.updateOutputModeButton(screen);
		
	}

	@Override
	public void postRender(TraderSettingsScreen screen, MatrixStack pose, int mouseX, int mouseY, float partialTicks) {}

	@Override
	public void tick(TraderSettingsScreen screen) {
		TraderData trader = screen.getTrader();
		if(trader instanceof EnergyTraderData)
		{
			EnergyTraderData e = (EnergyTraderData)trader;
			this.buttonDrainMode.visible = e.drainCapable();
		}
		else
			this.buttonDrainMode.visible = false;
	}
	
	private void updateOutputModeButton(TraderSettingsScreen screen)
	{
		this.buttonDrainMode.setMessage(EasyText.translatable("gui.lctech.settings.energy.drainmode", this.getOutputModeText(screen)));
	}
	
	private IFormattableTextComponent getOutputModeText(TraderSettingsScreen screen)
	{
		TraderData trader = screen.getTrader();
		if(trader instanceof EnergyTraderData)
		{
			EnergyTraderData e = (EnergyTraderData)trader;
			if(e.isAlwaysDrainMode())
				return EasyText.translatable("gui.lctech.settings.energy.drainmode.full");
			else
				return EasyText.translatable("gui.lctech.settings.energy.drainmode.sales");
		}
		else
			return EasyText.literal("NULL");
	}
	
	@Override
	public void onClose(TraderSettingsScreen screen) {}
	
	private void ToggleDrainMode(TraderSettingsScreen screen)
	{
		TraderData trader = screen.getTrader();
		if(trader instanceof EnergyTraderData)
		{
			EnergyTraderData e = (EnergyTraderData)trader;
			CompoundNBT message = new CompoundNBT();
			message.putInt("NewEnergyDrainMode", e.getDrainMode().index + 1);
			e.sendNetworkMessage(message);
		}
	}
	
}