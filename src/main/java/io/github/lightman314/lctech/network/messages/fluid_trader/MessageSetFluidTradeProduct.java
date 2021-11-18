package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetFluidTradeProduct implements IMessage<MessageSetFluidTradeProduct>{
	
	BlockPos pos;
	int tradeIndex;
	FluidStack fluid;
	
	public MessageSetFluidTradeProduct() {};
	
	public MessageSetFluidTradeProduct(BlockPos pos, int tradeIndex, FluidStack fluid)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.fluid = fluid;
	}
	
	@Override
	public MessageSetFluidTradeProduct decode(PacketBuffer buffer) {
		return new MessageSetFluidTradeProduct(buffer.readBlockPos(), buffer.readInt(), FluidStack.readFromPacket(buffer));
	}

	@Override
	public void encode(MessageSetFluidTradeProduct message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		message.fluid.writeToPacket(buffer);
	}

	@Override
	public void handle(MessageSetFluidTradeProduct message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				TileEntity tileEntity = player.world.getTileEntity(message.pos);
				if(tileEntity instanceof FluidTraderTileEntity)
				{
					FluidTraderTileEntity traderEntity = (FluidTraderTileEntity)tileEntity;
					FluidTradeData trade = traderEntity.getTrade(message.tradeIndex);
					trade.setProduct(message.fluid);
					traderEntity.markTradesDirty();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
