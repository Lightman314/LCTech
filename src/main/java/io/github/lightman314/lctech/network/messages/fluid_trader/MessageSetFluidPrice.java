package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData.FluidTradeType;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

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
	public MessageSetFluidPrice decode(FriendlyByteBuf buffer) {
		return new MessageSetFluidPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readNbt()), FluidTradeData.loadTradeType(buffer.readUtf()), buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
	}

	@Override
	public void encode(MessageSetFluidPrice message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.price.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeUtf(message.tradeType.name());
		buffer.writeInt(message.bucketCount);
		buffer.writeBoolean(message.canDrain);
		buffer.writeBoolean(message.canFill);
		
	}

	@Override
	public void handle(MessageSetFluidPrice message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof FluidTraderTileEntity)
				{
					FluidTraderTileEntity traderEntity = (FluidTraderTileEntity)blockEntity;
					FluidTradeData trade = traderEntity.getTrade(message.tradeIndex);
					
					trade.setCost(message.price);
					trade.setTradeType(message.tradeType);
					trade.setBucketQuantity(message.bucketCount);
					trade.setDrainable(message.canDrain);
					trade.setFillable(message.canFill);
					
					CompoundTag compound = traderEntity.writeTrades(new CompoundTag());
					TileEntityUtil.sendUpdatePacket(blockEntity, traderEntity.superWrite(compound));
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
