package io.github.lightman314.lctech.network;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.network.messages.fluid_trader.*;
import io.github.lightman314.lctech.network.messages.energy_trader.*;
import io.github.lightman314.lctech.network.messages.universal_fluid_trader.*;
import io.github.lightman314.lctech.network.messages.universal_energy_trader.*;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
		 register(MessageToggleFluidIcon2.class, new MessageToggleFluidIcon2());
		 
		 //Energy Traders
		 register(MessageSetEnergyPrice.class, new MessageSetEnergyPrice());

		 //Universal Energy Traders
		 register(MessageSetEnergyPrice2.class, new MessageSetEnergyPrice2());
		 
	}
	
	private static <T> void register(Class<T> clazz, IMessage<T> message)
	{
		instance.registerMessage(nextId++, clazz, message::encode, message::decode, message::handle);
	}
	
	public static PacketTarget getTarget(PlayerEntity player)
	{
		return getTarget((ServerPlayerEntity)player);
	}
	
	public static PacketTarget getTarget(ServerPlayerEntity player)
	{
		return PacketDistributor.PLAYER.with(() -> player);
	}
	
	
}
