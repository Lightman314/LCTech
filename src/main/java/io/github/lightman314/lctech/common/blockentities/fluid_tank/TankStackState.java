package io.github.lightman314.lctech.common.blockentities.fluid_tank;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum TankStackState implements IStringSerializable { SOLO, BOTTOM, MIDDLE, TOP;
    @Override
    public @Nonnull String getSerializedName() { return this.name().toLowerCase(); }
    public final boolean isSolo() { return this == SOLO; }
    public final boolean isBottom() { return this == BOTTOM; }
    public final boolean isTop() { return this == TOP; }
}