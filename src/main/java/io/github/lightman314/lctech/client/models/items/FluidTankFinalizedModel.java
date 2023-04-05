package io.github.lightman314.lctech.client.models.items;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.client.util.FluidRenderUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidTankFinalizedModel implements BakedModel{
	
	private final BakedModel parentModel;
	private final FluidStack tank;
	private final int capacity;
	private final FluidRenderData renderData;
	
	public FluidTankFinalizedModel(BakedModel parentModel, FluidStack tank, int capacity, FluidRenderData renderData)
	{
		this.parentModel = parentModel;
		this.tank = tank;
		this.capacity = capacity;
		this.renderData = renderData;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull RandomSource rand) {
		if(side != null)
		{
			return parentModel.getQuads(state, side, rand);
		}
		
		List<BakedQuad> combinedQuadsList = new ArrayList<>(parentModel.getQuads(state, side, rand));
		combinedQuadsList.addAll(FluidRenderUtil.getBakedFluidQuads(this.tank, this.capacity, this.renderData));
		return combinedQuadsList;
	}

	@Override
	public boolean useAmbientOcclusion() { return parentModel.useAmbientOcclusion(); }

	@Override
	public boolean isGui3d() { return parentModel.isGui3d(); }

	@Override
	public boolean usesBlockLight() { return false; }

	@Override
	public boolean isCustomRenderer() { return parentModel.isCustomRenderer(); }
	
	@Nonnull
	@Override
	public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleIcon() { return parentModel.getParticleIcon(); }
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public ItemTransforms getTransforms() { return this.parentModel.getTransforms(); }

}
