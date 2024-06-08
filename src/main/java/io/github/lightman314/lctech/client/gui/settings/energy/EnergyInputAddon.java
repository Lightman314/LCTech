package io.github.lightman314.lctech.client.gui.settings.energy;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class EnergyInputAddon extends InputTabAddon {

	public static final EnergyInputAddon INSTANCE = new EnergyInputAddon();
	
	private EnergyInputAddon() {}

	EasyButton buttonDrainMode;

	@Override
	public void onOpen(SettingsSubTab settingsSubTab, ScreenArea screenArea, boolean firstOpen) {
		this.buttonDrainMode = settingsSubTab.addChild(new EasyTextButton(screenArea.pos.offset(20, 100), settingsSubTab.screen.getXSize() - 40, 20, this.getOutputModeTextSource(settingsSubTab), b -> this.ToggleDrainMode(settingsSubTab)));

		this.tick(settingsSubTab);
	}

	@Override
	public void renderBG(@Nonnull SettingsSubTab settingsSubTab, @Nonnull EasyGuiGraphics easyGuiGraphics) { }

	@Override
	public void renderAfterWidgets(@Nonnull SettingsSubTab settingsSubTab, @Nonnull EasyGuiGraphics easyGuiGraphics) { }

	@Override
	public void tick(SettingsSubTab settingsSubTab) {
		TraderData trader = settingsSubTab.menu.getTrader();
		if(trader instanceof EnergyTraderData e)
			this.buttonDrainMode.visible = e.drainCapable();
		else
			this.buttonDrainMode.visible = false;
	}

	@Override
	public void onClose(@Nonnull SettingsSubTab settingsSubTab) { }
	
	private NonNullSupplier<Component> getOutputModeTextSource(SettingsSubTab settingsSubTab)
	{
		return () -> TechText.GUI_SETTINGS_ENERGY_DRAINMODE.get(this.getOutputModeText(settingsSubTab));
	}
	
	private MutableComponent getOutputModeText(SettingsSubTab settingsSubTab)
	{
		TraderData trader = settingsSubTab.menu.getTrader();
		if(trader instanceof EnergyTraderData e)
		{
			if(e.isAlwaysDrainMode())
				return TechText.GUI_SETTINGS_ENERGY_DRAINMODE_FULL.get();
			else
				return TechText.GUI_SETTINGS_ENERGY_DRAINMODE_SALES.get();
		}
		else
			return EasyText.literal("NULL");
	}
	
	private void ToggleDrainMode(SettingsSubTab settingsSubTab)
	{
		TraderData trader = settingsSubTab.menu.getTrader();
		if(trader instanceof EnergyTraderData e)
			settingsSubTab.sendMessage(LazyPacketData.simpleInt("NewEnergyDrainMode", e.getDrainMode().index + 1));
	}
	
}
