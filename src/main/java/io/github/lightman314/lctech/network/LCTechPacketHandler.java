package io.github.lightman314.lctech.network;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.network.messages.energy_trader.*;
import io.github.lightman314.lctech.network.messages.fluid_trader.*;
import io.github.lightman314.lctech.network.messages.universal_energy_trader.*;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.*;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

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
		 
		 //Fluid Traders
		 register(MessageSetFluidTradeProduct.class, new MessageSetFluidTradeProduct());
		 register(MessageFluidTradeTankInteraction.class, new MessageFluidTradeTankInteraction());
		 register(MessageSetFluidPrice.class, new MessageSetFluidPrice());
		 register(MessageFluidEditOpen.class, new MessageFluidEditOpen());
		 register(MessageFluidEditClose.class, new MessageFluidEditClose());
		 register(MessageFluidEditSet.class, new MessageFluidEditSet());
		 register(MessageToggleFluidIcon.class, new MessageToggleFluidIcon());
		 
		 //Universal Fluid Traders
		 register(MessageSetFluidTradeProduct2.class, new MessageSetFluidTradeProduct2());
		 register(MessageSetFluidPrice2.class, new MessageSetFluidPrice2());
		 
		 //Energy Traders
		 register(MessageSetEnergyPrice.class, new MessageSetEnergyPrice());
		 
		 //Universal Energy Traders
		 register(MessageSetEnergyPrice2.class, new MessageSetEnergyPrice2());
		 
	}
	
	private static <T> void register(Class<T> clazz, IMessage<T> message)
	{
		instance.registerMessage(nextId++, clazz, message::encode, message::decode, message::handle);
	}
	
	public static PacketTarget getTarget(Player player)
	{
		return getTarget((ServerPlayer)player);
	}
	
	public static PacketTarget getTarget(ServerPlayer player)
	{
		return PacketDistributor.PLAYER.with(() -> player);
	}
	
	
}
