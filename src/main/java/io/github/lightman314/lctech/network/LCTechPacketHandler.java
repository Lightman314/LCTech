package io.github.lightman314.lctech.network;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.network.message.fluid_tank.CMessageRequestTankStackSync;
import io.github.lightman314.lctech.network.message.fluid_tank.SMessageSyncTankStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class LCTechPacketHandler {
	
	public static final String PROTOCOL_VERSION = "1";
	
	public static SimpleChannel instance;
	private static int nextId = 0;
	
	public static void init()
	{
		 instance = NetworkRegistry.ChannelBuilder
				 .named(new ResourceLocation(LCTech.MODID,"network"))
				 .networkProtocolVersion(() -> PROTOCOL_VERSION)
				 .clientAcceptedVersions(PROTOCOL_VERSION::equals)
				 .serverAcceptedVersions(PROTOCOL_VERSION::equals)
				 .simpleChannel();

		 //Fluid Tanks
		register(CMessageRequestTankStackSync.class, CMessageRequestTankStackSync::encode, CMessageRequestTankStackSync::decode, CMessageRequestTankStackSync::handle);
		register(SMessageSyncTankStack.class, SMessageSyncTankStack::encode, SMessageSyncTankStack::decode, SMessageSyncTankStack::handle);

	}

	private static <T> void register(Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler)
	{
		instance.registerMessage(nextId++, clazz, encoder, decoder, handler);
	}

	
}
