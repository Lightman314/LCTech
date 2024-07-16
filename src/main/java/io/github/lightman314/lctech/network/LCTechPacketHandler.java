package io.github.lightman314.lctech.network;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.network.message.fluid_tank.CMessageRequestTankStackSync;
import io.github.lightman314.lctech.network.message.fluid_tank.SMessageSyncTankStack;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.packet.CustomPacket;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = LCTech.MODID)
public class LCTechPacketHandler {
	
	public static final String PROTOCOL_VERSION = "1";
	
	private static PayloadRegistrar registrar = null;

	@SubscribeEvent
	public static void onPayloadRegister(RegisterPayloadHandlersEvent event) {

		registrar = event.registrar(PROTOCOL_VERSION);
		 //Fluid Tanks
		registerC2S(CMessageRequestTankStackSync.HANDLER);
		registerS2C(SMessageSyncTankStack.HANDLER);

	}

	private static <T extends ServerToClientPacket> void registerS2C(CustomPacket.AbstractHandler<T> handler)
	{
		registrar.playToClient(handler.type, handler.codec, handler);
	}

	private static <T extends ClientToServerPacket> void registerC2S(CustomPacket.AbstractHandler<T> handler)
	{
		registrar.playToServer(handler.type, handler.codec, handler);
	}

	
}
