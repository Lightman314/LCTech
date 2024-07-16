package io.github.lightman314.lctech.network.message.fluid_tank;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CMessageRequestTankStackSync extends ClientToServerPacket {

    private static final Type<CMessageRequestTankStackSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LCTech.MODID,"c_tank_stack_sync_request"));
    public static final Handler<CMessageRequestTankStackSync> HANDLER = new H();

    private final BlockPos tankPos;
    public CMessageRequestTankStackSync(BlockPos tankPos) {
        super(TYPE);
        this.tankPos = tankPos;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CMessageRequestTankStackSync message) { buffer.writeBlockPos(message.tankPos); }
    private static CMessageRequestTankStackSync decode(@Nonnull FriendlyByteBuf buffer) { return new CMessageRequestTankStackSync(buffer.readBlockPos()); }

    private static final class H extends Handler<CMessageRequestTankStackSync>
    {
        private H() { super(TYPE, easyCodec(CMessageRequestTankStackSync::encode,CMessageRequestTankStackSync::decode)); }
        @Override
        protected void handle(@Nonnull CMessageRequestTankStackSync message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            if(player.level().getBlockEntity(message.tankPos) instanceof FluidTankBlockEntity tank)
                tank.sendTankStackPacket(player);
        }
    }

}
