package io.github.lightman314.lctech.client.models.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class FluidShardModel implements BakedModel {
	
	BakedModel baseFluidTankModel;
	ItemOverrides fluidTankItemOverrideList = new FluidShardItemOverrideList();
	
	public FluidShardModel(BakedModel baseFluidTankModel)
	{
		this.baseFluidTankModel = baseFluidTankModel;
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull RandomSource rand) {
		return this.baseFluidTankModel.getQuads(state, side, rand);
	}
	
	@Nonnull
	@Override
	public ItemOverrides getOverrides()
	{
		return this.fluidTankItemOverrideList;
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return this.baseFluidTankModel.useAmbientOcclusion();
	}
	
	@Override
	public boolean isGui3d() {
		return this.baseFluidTankModel.isGui3d();
	}
	
	@Override
	public boolean usesBlockLight() {
		return this.baseFluidTankModel.usesBlockLight();
	}
	
	@Override
	public boolean isCustomRenderer() {
		return this.baseFluidTankModel.isCustomRenderer();
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleIcon() {
		return this.baseFluidTankModel.getParticleIcon();
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public ItemTransforms getTransforms() {
		return this.baseFluidTankModel.getTransforms();
	}
	
	public static class FluidShardItemOverrideList extends ItemOverrides{

		public FluidShardItemOverrideList() { super(); }
		
		@Override
		public BakedModel resolve(@Nonnull BakedModel model, @Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int light)
		{
			FluidStack tank = FluidStack.EMPTY;
			FluidRenderData renderData = FluidShardItem.RENDER_DATA;
			if(stack != null)
			{
				tank = FluidShardItem.GetFluid(stack);
				if(stack.getItem() instanceof FluidShardItem shard)
					renderData = shard.renderData;
			}
			return new FluidTankFinalizedModel(model, tank, () -> FluidType.BUCKET_VOLUME, renderData);
		}
		
	}

	
	
}
