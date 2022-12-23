package io.github.lightman314.lctech.network.message.fluid_tank;

import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CMessageRequestTankStackSync {

    private final BlockPos tankPos;
    public CMessageRequestTankStackSync(BlockPos tankPos) { this.tankPos = tankPos; }

    public static void encode(CMessageRequestTankStackSync message, FriendlyByteBuf buffer) { buffer.writeBlockPos(message.tankPos); }

    public static CMessageRequestTankStackSync decode(FriendlyByteBuf buffer) { return new CMessageRequestTankStackSync(buffer.readBlockPos()); }

    public static void handle(CMessageRequestTankStackSync message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ServerPlayer player = supplier.get().getSender();
            if(player.level.getBlockEntity(message.tankPos) instanceof FluidTankBlockEntity tank)
                tank.sendTankStackPacket(player);
        });
        supplier.get().setPacketHandled(true);
    }

}
