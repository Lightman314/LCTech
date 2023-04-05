package io.github.lightman314.lctech.client.gui.settings.energy;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lightmanscurrency.client.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

public class EnergyInputAddon extends InputTabAddon {

	public static final EnergyInputAddon INSTANCE = new EnergyInputAddon();

	private EnergyInputAddon() {}

	Button buttonDrainMode;

	@Override
	public void onInit(SettingsSubTab settingsSubTab) {
		this.buttonDrainMode = settingsSubTab.addWidget(EasyButton.builder(EasyText.empty(), b -> this.ToggleDrainMode(settingsSubTab)).pos(settingsSubTab.screen.getGuiLeft() + 20, settingsSubTab.screen.getGuiTop() + 100).size(settingsSubTab.screen.getXSize() - 40, 20).build());

		this.tick(settingsSubTab);
	}

	@Override
	public void renderBG(SettingsSubTab settingsSubTab, PoseStack poseStack, int i, int i1, float v) {
		this.updateOutputModeButton(settingsSubTab);
	}

	@Override
	public void renderTooltips(SettingsSubTab settingsSubTab, PoseStack poseStack, int i, int i1) {

	}

	@Override
	public void tick(SettingsSubTab settingsSubTab) {
		TraderData trader = settingsSubTab.menu.getTrader();
		if(trader instanceof EnergyTraderData e)
			this.buttonDrainMode.visible = e.drainCapable();
		else
			this.buttonDrainMode.visible = false;
	}

	@Override
	public void onClose(SettingsSubTab settingsSubTab) {

	}

	private void updateOutputModeButton(SettingsSubTab settingsSubTab)
	{
		this.buttonDrainMode.setMessage(EasyText.translatable("gui.lctech.settings.energy.drainmode", this.getOutputModeText(settingsSubTab)));
	}

	private MutableComponent getOutputModeText(SettingsSubTab settingsSubTab)
	{
		TraderData trader = settingsSubTab.menu.getTrader();
		if(trader instanceof EnergyTraderData e)
		{
			if(e.isAlwaysDrainMode())
				return EasyText.translatable("gui.lctech.settings.energy.drainmode.full");
			else
				return EasyText.translatable("gui.lctech.settings.energy.drainmode.sales");
		}
		else
			return EasyText.literal("NULL");
	}

	private void ToggleDrainMode(SettingsSubTab settingsSubTab)
	{
		TraderData trader = settingsSubTab.menu.getTrader();
		if(trader instanceof EnergyTraderData e)
		{
			CompoundTag message = new CompoundTag();
			message.putInt("NewEnergyDrainMode", e.getDrainMode().index + 1);
			e.sendNetworkMessage(message);
		}
	}

}