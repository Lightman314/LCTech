package io.github.lightman314.lctech.network.message.fluid_tank;

import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CMessageRequestTankStackSync {

    private final BlockPos tankPos;
    public CMessageRequestTankStackSync(BlockPos tankPos) { this.tankPos = tankPos; }

    public static void encode(CMessageRequestTankStackSync message, PacketBuffer buffer) { buffer.writeBlockPos(message.tankPos); }

    public static CMessageRequestTankStackSync decode(PacketBuffer buffer) { return new CMessageRequestTankStackSync(buffer.readBlockPos()); }

    public static void handle(CMessageRequestTankStackSync message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ServerPlayerEntity player = supplier.get().getSender();
            if(player.level.getBlockEntity(message.tankPos) instanceof FluidTankBlockEntity)
            {
                FluidTankBlockEntity tank = (FluidTankBlockEntity)player.level.getBlockEntity(message.tankPos);
                tank.sendTankStackPacket(player);
            }
        });
        supplier.get().setPacketHandled(true);
    }

}