package io.github.lightman314.lctech.common.traders.energy.client;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.client.gui.settings.energy.EnergyInputAddon;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.items.IBatteryItem;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.common.traders.input.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.input.client.ClientInputSubtraderAttachment;

import java.util.List;

public class ClientEnergyAttachment extends ClientInputSubtraderAttachment {

    public static final ClientEnergyAttachment INSTANCE = new ClientEnergyAttachment();
    private ClientEnergyAttachment() { super(ItemIcon.ofItem(IBatteryItem.HideEnergyBar(ModItems.BATTERY)), TechText.TOOLTIP_SETTINGS_INPUT_ENERGY.get()); }

    @Override
    public List<? extends InputTabAddon> getInputSettingsAddons(InputTraderData trader) { return ImmutableList.of(EnergyInputAddon.INSTANCE); }

}