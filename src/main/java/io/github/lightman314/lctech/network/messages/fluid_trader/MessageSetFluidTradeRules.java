package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetFluidTradeRules implements IMessage<MessageSetFluidTradeRules>{
	
	BlockPos pos;
	int tradeIndex;
	List<TradeRule> rules;
	
	public MessageSetFluidTradeRules() {};
	
	public MessageSetFluidTradeRules(BlockPos pos, List<TradeRule> rules)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = -1;
	}
	
	public MessageSetFluidTradeRules(BlockPos pos, List<TradeRule> rules, int tradeIndex)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = tradeIndex;
	}
	
	@Override
	public MessageSetFluidTradeRules decode(PacketBuffer buffer) {
		return new MessageSetFluidTradeRules(buffer.readBlockPos(), TradeRule.readRules(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetFluidTradeRules message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeCompoundTag(TradeRule.writeRules(new CompoundNBT(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageSetFluidTradeRules message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			PlayerEntity player = source.get().getSender();
			if(player != null)
			{
				TileEntity tileEntity = player.world.getTileEntity(message.pos);
				if(tileEntity instanceof FluidTraderTileEntity)
				{
					FluidTraderTileEntity traderEntity = (FluidTraderTileEntity)tileEntity;
					if(message.tradeIndex < 0)
					{
						traderEntity.setRules(message.rules);
						traderEntity.markRulesDirty();
					}
					else
					{
						ITradeRuleHandler trade = traderEntity.getTrade(message.tradeIndex);
						if(trade != null)
							trade.setRules(message.rules);
						traderEntity.markTradesDirty();
					}
				}
			}
		});
		source.get().setPacketHandled(true);
	}

	
	
}
