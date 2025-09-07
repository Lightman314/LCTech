package io.github.lightman314.lctech.common.blockentities.fluid_tank;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum TankStackState implements StringRepresentable { SOLO, BOTTOM, MIDDLE, TOP;
    @Override
    public @NotNull String getSerializedName() { return this.name().toLowerCase(Locale.ENGLISH); }
    public final boolean isSolo() { return this == SOLO; }
    public final boolean isBottom() { return this == BOTTOM; }
    public final boolean isTop() { return this == TOP; }
}
