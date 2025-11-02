package io.github.lightman314.lctech.common.util.icons;

import com.google.gson.JsonObject;
import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidIcon extends IconData {

    public static final Type TYPE = new Type(VersionUtil.modResource(LCTech.MODID,"fluid"),FluidIcon::loadFluid,FluidIcon::parseFluid);

    public static void register() {
        IconData.registerIconType(TYPE);
    }

    private final FluidStack fluid;

    private FluidIcon(FluidStack fluid) {
        super(TYPE);
        this.fluid = fluid;
    }

    public static FluidIcon ofFluid(FluidStack fluid) { return new FluidIcon(fluid); }

    @SuppressWarnings("unreachable")
    @Override
    public void render(EasyGuiGraphics gui, int x, int y) {
        FluidRenderUtil.drawFluidTankInGUI(this.fluid, gui, x, y, 16, 16, 1d);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("Fluid", this.fluid.writeToNBT(new CompoundTag()));
    }

    @Override
    protected void writeAdditional(JsonObject json) {
        json.add("Fluid", FluidItemUtil.convertFluidStack(this.fluid));
    }

    public boolean matches(FluidIcon other)
    {
        return this.fluid.getFluid() == other.fluid.getFluid() && FluidStack.areFluidStackTagsEqual(this.fluid, other.fluid);
    }

    private static IconData loadFluid(CompoundTag tag) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
        return new FluidIcon(fluid);
    }

    private static IconData parseFluid(JsonObject json) {
        FluidStack fluid = FluidItemUtil.parseFluidStack(GsonHelper.getAsJsonObject(json,"Fluid"));
        return new FluidIcon(fluid);
    }

}
