package io.github.lightman314.lctech.common.util.icons;

import com.google.gson.JsonObject;
import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.common.util.FluidItemUtil;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidIcon extends IconData {

    public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath(LCTech.MODID,"fluid"),FluidIcon::loadFluid,FluidIcon::parseFluid);

    public static void register() {
        IconData.registerIconType(TYPE);
    }

    private final FluidStack fluid;

    private FluidIcon(FluidStack fluid) {
        super(TYPE);
        this.fluid = fluid;
    }

    public static FluidIcon ofFluid(FluidStack fluid) { return new FluidIcon(fluid); }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y) {
        FluidRenderUtil.drawFluidTankInGUI(this.fluid, gui, x, y, 16, 16, 1f);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.put("Fluid", this.fluid.saveOptional(lookup));
    }

    @Override
    protected void writeAdditional(JsonObject json, HolderLookup.Provider provider) {
        json.add("Fluid", FluidItemUtil.convertFluidStack(this.fluid,provider));
    }

    public boolean matches(FluidIcon other) { return FluidStack.isSameFluidSameComponents(this.fluid, other.fluid); }

    private static IconData loadFluid(CompoundTag tag, HolderLookup.Provider lookup)
    {
        FluidStack fluid = FluidStack.parseOptional(lookup,tag.getCompound("Fluid"));
        return new FluidIcon(fluid);
    }

    private static IconData parseFluid(JsonObject json, HolderLookup.Provider lookup)
    {
        FluidStack fluid = FluidItemUtil.parseFluidStack(GsonHelper.getAsJsonObject(json,"Fluid"),lookup);
        return new FluidIcon(fluid);
    }

}
