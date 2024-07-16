package io.github.lightman314.lctech.common.util.icons;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidIcon extends IconData {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(LCTech.MODID,"fluid");

    public static void register() {
        IconData.registerIconType(TYPE, (t,l) -> new FluidIcon(FluidStack.parseOptional(l,t.getCompound("Fluid"))));
    }

    private final FluidStack fluid;

    private FluidIcon(@Nonnull FluidStack fluid) {
        super(TYPE);
        this.fluid = fluid;
    }

    @Override
    public void render(@Nonnull EasyGuiGraphics gui, int x, int y) {
        FluidRenderUtil.drawFluidTankInGUI(this.fluid, gui, x, y, 16, 16, 1f);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
        tag.put("Fluid", this.fluid.saveOptional(lookup));
    }

    public static FluidIcon of(@Nonnull FluidStack fluid) { return new FluidIcon(fluid); }

    public boolean matches(@Nonnull FluidIcon other)
    {
        return FluidStack.isSameFluidSameComponents(this.fluid, other.fluid);
    }

}
