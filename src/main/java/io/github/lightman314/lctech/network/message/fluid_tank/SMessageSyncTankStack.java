package io.github.lightman314.lctech.network.message.fluid_tank;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SMessageSyncTankStack {

    private final TankStackCache.PacketBuilder data;
    public SMessageSyncTankStack(TankStackCache.PacketBuilder data) { this.data = data; }

    public static void encode(SMessageSyncTankStack message, PacketBuffer buffer) { message.data.encode(buffer); }

    public static SMessageSyncTankStack decode(PacketBuffer buffer) { return new SMessageSyncTankStack(TankStackCache.decode(buffer)); }

    public static void handle(SMessageSyncTankStack message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> LCTech.PROXY.handleTankStackPacket(message.data));
        supplier.get().setPacketHandled(true);
    }

}