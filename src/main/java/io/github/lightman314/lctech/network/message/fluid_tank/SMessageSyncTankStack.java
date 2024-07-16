package io.github.lightman314.lctech.network.message.fluid_tank;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SMessageSyncTankStack extends ServerToClientPacket {

    private static final CustomPacketPayload.Type<SMessageSyncTankStack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LCTech.MODID,"s_tank_stack_sync"));
    public static final Handler<SMessageSyncTankStack> HANDLER = new H();

    private final TankStackCache.PacketBuilder data;
    public SMessageSyncTankStack(TankStackCache.PacketBuilder data) { super(TYPE); this.data = data; }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SMessageSyncTankStack message) { message.data.encode(buffer); }

    @Nonnull
    private static SMessageSyncTankStack decode(@Nonnull FriendlyByteBuf buffer) { return new SMessageSyncTankStack(TankStackCache.decode(buffer)); }

    private static class H extends Handler<SMessageSyncTankStack>
    {

        protected H() { super(TYPE, easyCodec(SMessageSyncTankStack::encode,SMessageSyncTankStack::decode)); }
        @Override
        protected void handle(@Nonnull SMessageSyncTankStack message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            LCTech.PROXY.handleTankStackPacket(message.data);
        }
    }

}
