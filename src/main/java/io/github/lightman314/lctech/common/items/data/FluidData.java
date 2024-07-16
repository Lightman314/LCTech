package io.github.lightman314.lctech.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class FluidData {

    public static final Codec<FluidData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(FluidStack.OPTIONAL_CODEC.fieldOf("Fluid").forGetter(FluidData::getFluid),
                    Codec.BOOL.optionalFieldOf("Tooltip",true).forGetter(d -> d.showTooltip))
                    .apply(builder,FluidData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,FluidData> STREAM_CODEC = StreamCodec.of(
            (b,d) -> { FluidStack.OPTIONAL_STREAM_CODEC.encode(b,d.fluid); b.writeBoolean(d.showTooltip);},
            b -> new FluidData(FluidStack.OPTIONAL_STREAM_CODEC.decode(b), b.readBoolean()));

    public static final FluidData EMPTY = new FluidData(FluidStack.EMPTY,true,true);

    private final FluidStack fluid;
    public FluidStack getFluid() { return this.fluid.copy(); }
    public final boolean showTooltip;
    public FluidData(@Nonnull FluidStack fluid) { this.fluid = fluid.copy(); this.showTooltip = true; }
    public FluidData(@Nonnull FluidStack fluid, boolean showTooltip) { this.fluid = fluid.copy(); this.showTooltip = showTooltip; }
    private FluidData(@Nonnull FluidStack fluid, boolean showTooltip, boolean ignored) { this.fluid = fluid; this.showTooltip = showTooltip; }

    @Nonnull
    public FluidData withFluid(@Nonnull FluidStack fluid) { return new FluidData(fluid,this.showTooltip); }
    public FluidData withTooltipVisibility(boolean showTooltip) { return new FluidData(this.fluid,showTooltip,false); }

    @Override
    public int hashCode() { return Objects.hash(this.fluid,this.showTooltip); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FluidData other)
            return FluidStack.isSameFluidSameComponents(this.fluid,other.fluid) && this.fluid.getAmount() == other.fluid.getAmount() && this.showTooltip == other.showTooltip;
        return false;
    }
}
