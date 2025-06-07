package io.github.lightman314.lctech.datagen.client;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.resourcepacks.data.model_variants.TechProperties;
import io.github.lightman314.lctech.client.resourcepacks.data.model_variants.properties.FluidRenderDataList;
import io.github.lightman314.lctech.common.blocks.traderblocks.FluidTapBundleBlock;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.datagen.client.generators.ModelVariantProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class TechVariantProvider extends ModelVariantProvider {

    public TechVariantProvider(PackOutput output) {
        super(output, LCTech.MODID);
    }

    @Override
    protected void addEntries() {

        /*
        Map<Direction, List<FluidRenderDataList.FluidRenderDataEntry>> map = new HashMap<>();
        for(int i = 0; i < 4; ++i)
        {
            Direction side = Direction.from2DDataValue(i);
            List<FluidRenderDataList.FluidRenderDataEntry> list = new ArrayList<>();
            List<ResourceLocation> ids = FluidTapBundleBlock.getRenderID(side);
            for(ResourceLocation id : ids)
                list.add(FluidRenderDataList.FluidRenderDataEntry.create(id));
            map.put(side,list);
        }

        this.add("examples/sided_fluid_inputs", ModelVariant.builder()
                .withTarget(ModBlocks.FLUID_TAP_BUNDLE)
                .withProperty(TechProperties.FLUID_RENDER_DATA, new FluidRenderDataList(map))
                .build());//*/

    }

}
