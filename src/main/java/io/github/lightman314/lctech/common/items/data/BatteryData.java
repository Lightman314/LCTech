package io.github.lightman314.lctech.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record BatteryData(int energy, boolean tooltipVisible, boolean energyBarVisible) {

    public static final Codec<BatteryData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.INT.fieldOf("Energy").forGetter(BatteryData::energy),
                    Codec.BOOL.optionalFieldOf("Tooltip",true).forGetter(BatteryData::tooltipVisible),
                    Codec.BOOL.optionalFieldOf("Visible",true).forGetter(BatteryData::energyBarVisible)).apply(builder,BatteryData::new));

    public static final StreamCodec<FriendlyByteBuf,BatteryData> STREAM_CODEC = StreamCodec.of(
            (b,d) -> { b.writeInt(d.energy); b.writeBoolean(d.tooltipVisible); b.writeBoolean(d.energyBarVisible); },
            b -> new BatteryData(b.readInt(),b.readBoolean(),b.readBoolean()));

    public static final BatteryData EMPTY = new BatteryData(0,true,true);

    public BatteryData withEnergy(int energy) { return new BatteryData(energy,this.tooltipVisible,this.energyBarVisible); }
    public BatteryData withTooltipVisible(boolean tooltipVisible) { return new BatteryData(this.energy,tooltipVisible,this.energyBarVisible); }
    public BatteryData withEnergyBarVisible(boolean energyBarVisible) { return new BatteryData(this.energy,this.tooltipVisible,energyBarVisible); }

    @Override
    public int hashCode() { return Objects.hash(this.energy,this.tooltipVisible,this.energyBarVisible); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BatteryData other)
            return other.energy == this.energy && other.tooltipVisible == this.tooltipVisible && other.energyBarVisible == this.energyBarVisible;
        return false;
    }
}
