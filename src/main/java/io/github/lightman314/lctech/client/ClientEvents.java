package io.github.lightman314.lctech.client;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lctech.common.traders.energy.EnergyTraderData;
import io.github.lightman314.lctech.common.traders.energy.client.ClientEnergyAttachment;
import io.github.lightman314.lctech.common.traders.fluid.FluidTraderData;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterClientTraderAttachmentsEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.client.ClientTraderAttachment;
import io.github.lightman314.lightmanscurrency.common.traders.input.client.ClientInputSubtraderAttachment;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    private static final ClientTraderAttachment FLUID_TRADER = new ClientInputSubtraderAttachment(ItemIcon.ofItem(Items.WATER_BUCKET), TechText.TOOLTIP_SETTINGS_INPUT_FLUID.get());

    @SubscribeEvent
    public static void registerClientAttachments(RegisterClientTraderAttachmentsEvent event)
    {
        TraderData trader = event.getTrader();
        if(trader instanceof EnergyTraderData)
            event.register(ClientEnergyAttachment.INSTANCE);
        if(trader instanceof FluidTraderData)
            event.register(FLUID_TRADER);
    }


}
