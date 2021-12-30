package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;

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
	public MessageSetFluidTradeProduct decode(FriendlyByteBuf buffer) {
		return new MessageSetFluidTradeProduct(buffer.readBlockPos(), buffer.readInt(), FluidStack.readFromPacket(buffer));
	}

	@Override
	public void encode(MessageSetFluidTradeProduct message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		message.fluid.writeToPacket(buffer);
	}

	@Override
	public void handle(MessageSetFluidTradeProduct message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof FluidTraderTileEntity)
				{
					FluidTraderTileEntity traderEntity = (FluidTraderTileEntity)blockEntity;
					FluidTradeData trade = traderEntity.getTrade(message.tradeIndex);
					trade.setProduct(message.fluid);
					traderEntity.markTradesDirty();
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
