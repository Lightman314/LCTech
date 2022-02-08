package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData.FluidTradeType;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetFluidPrice implements IMessage<MessageSetFluidPrice>{
	
	BlockPos pos;
	int tradeIndex;
	CoinValue price;
	FluidTradeType tradeType;
	boolean canDrain;
	boolean canFill;
	int bucketCount;
	
	public MessageSetFluidPrice() {};
	
	public MessageSetFluidPrice(BlockPos pos, int tradeIndex, CoinValue price, FluidTradeType tradeType, int bucketCount, boolean canDrain, boolean canFill)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.price = price;
		this.tradeType = tradeType;
		this.bucketCount = bucketCount;
		this.canDrain = canDrain;
		this.canFill = canFill;
	}
	
	@Override
	public MessageSetFluidPrice decode(PacketBuffer buffer) {
		return new MessageSetFluidPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readCompoundTag()), FluidTradeData.loadTradeType(buffer.readString(FluidTradeData.MaxTradeTypeStringLength())), buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
	}

	@Override
	public void encode(MessageSetFluidPrice message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeCompoundTag(message.price.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
		buffer.writeString(message.tradeType.name());
		buffer.writeInt(message.bucketCount);
		buffer.writeBoolean(message.canDrain);
		buffer.writeBoolean(message.canFill);
		
	}

	@Override
	public void handle(MessageSetFluidPrice message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				TileEntity tileEntity = player.world.getTileEntity(message.pos);
				if(tileEntity instanceof FluidTraderTileEntity)
				{
					FluidTraderTileEntity traderEntity = (FluidTraderTileEntity)tileEntity;
					FluidTradeData trade = traderEntity.getTrade(message.tradeIndex);
					
					trade.setCost(message.price);
					trade.setTradeType(message.tradeType);
					trade.setBucketQuantity(message.bucketCount);
					trade.setDrainable(message.canDrain);
					trade.setFillable(message.canFill);
					
					traderEntity.markTradesDirty();
					
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
