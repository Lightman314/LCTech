package io.github.lightman314.lctech.client.models.items;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankFinalizedModel implements BakedModel{
	
	BakedModel parentModel;
	FluidStack tank;
	int capacity;
	FluidRenderData renderData;
	
	public FluidTankFinalizedModel(BakedModel parentModel, FluidStack tank, int capacity, FluidRenderData renderData)
	{
		this.parentModel = parentModel;
		this.tank = tank;
		this.capacity = capacity;
		this.renderData = renderData;
	}

	@Override
	@SuppressWarnings("deprecation")
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
		if(side != null)
		{
			return parentModel.getQuads(state, side, rand);
		}
		
		List<BakedQuad> combinedQuadsList = Lists.newArrayList(parentModel.getQuads(state, side, rand));
		combinedQuadsList.addAll(FluidRenderUtil.getBakedFluidQuads(this.tank, this.capacity, this.renderData));
		return combinedQuadsList;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return parentModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return parentModel.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return parentModel.isCustomRenderer();
	}
	
	@Override
	public ItemOverrides getOverrides() {
		return null;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleIcon() {
		return parentModel.getParticleIcon();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public ItemTransforms getTransforms() {
		return this.parentModel.getTransforms();
	}

}
