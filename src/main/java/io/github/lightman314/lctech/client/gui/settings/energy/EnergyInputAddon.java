package io.github.lightman314.lctech.client.gui.settings.energy;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class EnergyInputAddon extends InputTabAddon {

	public static final EnergyInputAddon INSTANCE = new EnergyInputAddon();
	
	private EnergyInputAddon() {}

	EasyButton buttonDrainMode;

	@Override
	public void onOpen(SettingsSubTab settingsSubTab, ScreenArea screenArea, boolean firstOpen) {
		this.buttonDrainMode = settingsSubTab.addChild(EasyTextButton.builder()
				.position(screenArea.pos.offset(20,100))
				.width(screenArea.width - 40)
				.text(this.getOutputModeTextSource(settingsSubTab))
				.pressAction(() -> this.ToggleDrainMode(settingsSubTab))
				.addon(EasyAddonHelper.visibleCheck(this.drainable(settingsSubTab)))
				.build());
	}

	@Override
	public void renderBG(@Nonnull SettingsSubTab settingsSubTab, @Nonnull EasyGuiGraphics easyGuiGraphics) { }

	@Override
	public void renderAfterWidgets(@Nonnull SettingsSubTab settingsSubTab, @Nonnull EasyGuiGraphics easyGuiGraphics) { }

	private Supplier<Boolean> drainable(SettingsSubTab settingsSubTab) {
		return () -> {
			TraderData trader = settingsSubTab.menu.getTrader();
			return trader instanceof EnergyTraderData e && e.drainCapable();
		};
	}

	@Override
	public void tick(@Nonnull SettingsSubTab settingsSubTab) { }

	@Override
	public void onClose(@Nonnull SettingsSubTab settingsSubTab) { }
	
	private Supplier<Component> getOutputModeTextSource(SettingsSubTab settingsSubTab)
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
		if(settingsSubTab.menu.getTrader() instanceof EnergyTraderData e)
			settingsSubTab.sendMessage(settingsSubTab.builder().setInt("NewEnergyDrainMode", e.getDrainMode().index + 1));
	}
	
}
