package io.github.lightman314.lctech.client.models.items;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankFinalizedModel implements IBakedModel{
	
	IBakedModel parentModel;
	FluidStack tank;
	int capacity;
	FluidRenderData renderData;
	
	public FluidTankFinalizedModel(IBakedModel parentModel, FluidStack tank, int capacity, FluidRenderData renderData)
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
	public boolean isAmbientOcclusion() {
		return parentModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return parentModel.isGui3d();
	}

	@Override
	public boolean isSideLit() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return parentModel.isBuiltInRenderer();
	}
	
	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleTexture() {
		return parentModel.getParticleTexture();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public ItemCameraTransforms getItemCameraTransforms() {
		return this.parentModel.getItemCameraTransforms();
	}

}
