package io.github.lightman314.lctech.common.util.icons;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidIcon extends IconData {

    public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID,"fluid");

    public static void register() {
        IconData.registerIconType(TYPE, t -> new FluidIcon(FluidStack.loadFluidStackFromNBT(t.getCompound("Fluid"))));
    }

    private final FluidStack fluid;

    private FluidIcon(@Nonnull FluidStack fluid) {
        super(TYPE);
        this.fluid = fluid;
    }

    @SuppressWarnings("unreachable")
    @Override
    public void render(@Nonnull EasyGuiGraphics gui, int x, int y) {
        FluidRenderUtil.drawFluidTankInGUI(this.fluid, gui, x, y, 16, 16, 1d);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        tag.put("Fluid", this.fluid.writeToNBT(new CompoundTag()));
    }

    public static FluidIcon of(@Nonnull FluidStack fluid) { return new FluidIcon(fluid); }

    public boolean matches(@Nonnull FluidIcon other)
    {
        return this.fluid.getFluid() == other.fluid.getFluid() && FluidStack.areFluidStackTagsEqual(this.fluid, other.fluid);
    }

}
