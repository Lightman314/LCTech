package io.github.lightman314.lctech.network.messages.fluid_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lctech.tileentities.FluidTraderTileEntity;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

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
	public MessageSetFluidTradeRules decode(FriendlyByteBuf buffer) {
		return new MessageSetFluidTradeRules(buffer.readBlockPos(), TradeRule.readRules(buffer.readNbt()), buffer.readInt());
	}

	@Override
	public void encode(MessageSetFluidTradeRules message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeNbt(TradeRule.writeRules(new CompoundTag(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public void handle(MessageSetFluidTradeRules message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			Player player = source.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof FluidTraderTileEntity)
				{
					FluidTraderTileEntity traderEntity = (FluidTraderTileEntity)blockEntity;
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
