package io.github.lightman314.lctech.common.blockentities.fluid_tank;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lctech.network.message.fluid_tank.SMessageSyncTankStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TankStackCache {

    public static final TankStackCache DUMMY = new TankStackCache(new ArrayList<>());

    private final ImmutableList<FluidTankBlockEntity> tanksBottomToTop;
    private final ImmutableList<FluidTankBlockEntity> tanksTopToBottom;

    private TankStackCache(FluidTankBlockEntity tank) { this(ImmutableList.of(tank)); }

    private TankStackCache(List<FluidTankBlockEntity> tanks) { this.tanksBottomToTop = ImmutableList.copyOf(tanks); this.tanksTopToBottom = invertList(this.tanksBottomToTop); }

    private static ImmutableList<FluidTankBlockEntity> invertList(ImmutableList<FluidTankBlockEntity> tanks) {
        List<FluidTankBlockEntity> result = new ArrayList<>();
        for(int i = tanks.size() - 1; i >= 0; --i)
            result.add(tanks.get(i));
        return ImmutableList.copyOf(result);
    }

    public final List<FluidTankBlockEntity> getOrderedTanks() { return this.isLighterThanAir() ? this.tanksTopToBottom : this.tanksBottomToTop; }
    public final List<FluidTankBlockEntity> getOrderedTanks(FluidStack fluid) { return fluid.getFluid().getFluidType().isLighterThanAir() ? this.tanksTopToBottom : this.tanksBottomToTop; }

    public final boolean isLighterThanAir() { return this.getRelevantFluid().getFluid().getFluidType().isLighterThanAir(); }

    private FluidStack getRelevantFluid() {
        for(FluidTankBlockEntity tank : this.tanksBottomToTop)
        {
            FluidStack fluid = tank.getTankContents();
            if(!fluid.isEmpty())
                return fluid.copy();
        }
        return FluidStack.EMPTY;
    }

    @Nullable
    public final FluidTankBlockEntity getTankAbove(FluidTankBlockEntity tank) {
        List<FluidTankBlockEntity> tanks = this.getOrderedTanks();
        int index = tanks.indexOf(tank);
        if(index >= 0 && index < tanks.size() - 1)
            return tanks.get(index + 1);
        return null;
    }

    public final TankStackCache init(boolean isServer) {
        for(FluidTankBlockEntity tank : this.tanksBottomToTop)
            tank.setTankStack(this);
        //Don't need to rebalance the tank contents if this is solo or somehow 'null'
        if(this.tanksBottomToTop.size() <= 1)
            return this;
        FluidStack totalContents = FluidStack.EMPTY;
        for(FluidTankBlockEntity tank : this.tanksBottomToTop)
        {
            FluidStack tankContents = tank.getTankContents();
            if(tankContents.isEmpty())
                continue;
            if(totalContents.isEmpty())
                totalContents = tankContents.copy();
            else if(FluidStack.isSameFluidSameComponents(totalContents,tankContents))
                totalContents.grow(tankContents.getAmount());
            else
                LightmansCurrency.LogError("Tank in new Tank Stack doesn't contain a matching fluid. " + tankContents.getAmount() + "mB of " + BuiltInRegistries.FLUID.getKey(tankContents.getFluid()) + " will be lost to the void!");
        }
        this.tanksBottomToTop.getFirst().handler.setTankContents(totalContents);
        if(isServer)
            this.syncToClients();
        return this;
    }

    public final void refactorExcluded(TankStackCache newCache) {
        for(FluidTankBlockEntity tank : tanksBottomToTop)
        {
            if(!newCache.tanksBottomToTop.contains(tank)) //Enqueue the refactor so that if any other "excluded" tanks from the list include it in the new stack, the refactoring won't be done more than once.
                tank.enqueTankStackRefactor();
        }
    }

    public static TankStackCache create(Level level, BlockPos corePosition, int bottomY, int topY) {
        List<FluidTankBlockEntity> tanks = new ArrayList<>();
        for(int y = bottomY; y <= topY; ++y)
        {
            BlockPos pos = corePosition.atY(y);
            if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank)
                tanks.add(tank);
        }
        return new TankStackCache(tanks);
    }

    public static TankStackCache solo(FluidTankBlockEntity tank) { return new TankStackCache(tank); }

    public final SMessageSyncTankStack getSyncPacket() { return new SMessageSyncTankStack(new PacketBuilder(this)); }

    private void syncToClients() {
        if(!this.tanksBottomToTop.isEmpty())
        {
            FluidTankBlockEntity bottomTank = this.tanksBottomToTop.getFirst();
            if(bottomTank.getLevel() instanceof ServerLevel level)
                this.getSyncPacket().sendToPlayersTrackingChunk(level, new ChunkPos(bottomTank.getBlockPos()));
        }
    }

    public static PacketBuilder decode(FriendlyByteBuf buffer) { return new PacketBuilder(buffer); }

    public static class PacketBuilder {

        private final BlockPos startPos;
        private final List<Integer> yPos;

        private PacketBuilder(TankStackCache parent) {
            if(parent.tanksBottomToTop.isEmpty())
            {
                this.startPos = new BlockPos(0,0,0);
                this.yPos = new ArrayList<>();
            }
            else {
                this.startPos = parent.tanksBottomToTop.getFirst().getBlockPos();
                this.yPos = new ArrayList<>(parent.tanksBottomToTop.size());
                for(FluidTankBlockEntity tank : parent.tanksBottomToTop)
                    this.yPos.add(tank.getBlockPos().getY());
            }
        }

        private PacketBuilder(FriendlyByteBuf buffer) {
            this.startPos = buffer.readBlockPos();
            this.yPos = new ArrayList<>();
            int posCount = buffer.readInt();
            while(posCount-- > 0)
                this.yPos.add(buffer.readInt());
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(this.startPos);
            buffer.writeInt(this.yPos.size());
            for(int pos : this.yPos)
                buffer.writeInt(pos);
        }

        public TankStackCache build(Level level) {
            List<FluidTankBlockEntity> tanks = new ArrayList<>();
            for(int y : this.yPos)
            {
                if(level.getBlockEntity(this.startPos.atY(y)) instanceof FluidTankBlockEntity tank)
                    tanks.add(tank);
            }
            return new TankStackCache(tanks);
        }

    }

}
