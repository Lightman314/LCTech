package io.github.lightman314.lctech.datagen.client;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.blocks.traderblocks.FluidTapBlock;
import io.github.lightman314.lctech.common.blocks.traderblocks.FluidTapBundleBlock;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lctech.datagen.client.generators.FluidRenderDataProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;

public class TechFluidRenderDataProvider extends FluidRenderDataProvider {

    public TechFluidRenderDataProvider(PackOutput output) { super(output, LCTech.MODID); }

    @Override
    protected void addEntries() {

        //Fluid Tank Entries
        this.addData(FluidTankBlock.DATA_SOLO, FluidRenderData.CreateFluidRender(0.1f, 1f, 0.1f, 15.8f, 14f, 15.8f));
        this.addData(FluidTankBlock.DATA_TOP, FluidRenderData.CreateFluidRender(0.1f, 0f, 0.1f, 15.8f, 15f, 15.8f));
        this.addData(FluidTankBlock.DATA_MIDDLE, FluidRenderData.CreateFluidRender(0.1f, 0f, 0.1f, 15.8f, 16f, 15.8f));
        this.addData(FluidTankBlock.DATA_BOTTOM, FluidRenderData.CreateFluidRender(0.1f, 1f, 0.1f, 15.8f, 15f, 15.8f));

        //Fluid Tap
        this.addData(FluidTapBlock.DATA,FluidRenderData.CreateFluidRender(4.1f, 0.1f, 4.1f, 7.8f, 15.8f, 7.8f));

        //Fluid Tap Bundle
        this.addData(FluidTapBundleBlock.DATA_NW,FluidRenderData.CreateFluidRender(0.1f, 0.1f, 0.1f, 7.8f, 15.8f, 7.8f));
        this.addData(FluidTapBundleBlock.DATA_NE,FluidRenderData.CreateFluidRender(8.1f, 0.1f, 0.1f, 7.8f, 15.8f, 7.8f));
        this.addData(FluidTapBundleBlock.DATA_SW,FluidRenderData.CreateFluidRender(0.1f, 0.1f, 8.1f, 7.8f, 15.8f, 7.8f));
        this.addData(FluidTapBundleBlock.DATA_SE,FluidRenderData.CreateFluidRender(8.1f, 0.1f, 8.1f, 7.8f, 15.8f, 7.8f));

        //Fluid Shard
        this.addData(FluidShardItem.DATA,FluidRenderData.CreateFluidRender(5f, 2f, 7.55f, 7f, 12f, 0.9f, FluidSides.Create(Direction.SOUTH, Direction.NORTH)));

    }

}