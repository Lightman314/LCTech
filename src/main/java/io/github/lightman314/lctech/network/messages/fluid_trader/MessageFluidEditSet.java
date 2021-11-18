package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.container.FluidEditContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageFluidEditSet implements IMessage<MessageFluidEditSet>{
	
	private ItemStack item;
	
	public MessageFluidEditSet() {};
	
	public MessageFluidEditSet(ItemStack item)
	{
		this.item = item;
	}
	
	@Override
	public MessageFluidEditSet decode(PacketBuffer buffer) {
		return new MessageFluidEditSet(buffer.readItemStack());
	}

	@Override
	public void encode(MessageFluidEditSet message, PacketBuffer buffer) {
		buffer.writeItemStack(message.item);
	}

	@Override
	public void handle(MessageFluidEditSet message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				if(player.openContainer instanceof FluidEditContainer)
				{
					FluidEditContainer container = (FluidEditContainer)player.openContainer;
					container.setFluid(message.item);
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
