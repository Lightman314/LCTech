package io.github.lightman314.lctech.client.models.items;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.common.blocks.FluidTankBlock;
import io.github.lightman314.lctech.common.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderData;
import io.github.lightman314.lctech.common.items.FluidTankItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankModel implements IBakedModel {

	IBakedModel baseFluidTankModel;
	ItemOverrideList fluidTankItemOverrideList = new FluidTankItemOverrideList();

	public FluidTankModel(IBakedModel baseFluidTankModel)
	{
		this.baseFluidTankModel = baseFluidTankModel;
	}


	@Override
	@SuppressWarnings("deprecation")
	public @Nonnull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand)
	{
		return this.baseFluidTankModel.getQuads(state, side, rand);
	}

	@Override
	public @Nonnull ItemOverrideList getOverrides()
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

	@Override
	@SuppressWarnings("deprecation")
	public @Nonnull TextureAtlasSprite getParticleIcon() { return this.baseFluidTankModel.getParticleIcon(); }

	@Override
	@SuppressWarnings("deprecation")
	public @Nonnull ItemCameraTransforms getTransforms() {
		return this.baseFluidTankModel.getTransforms();
	}

	public static class FluidTankItemOverrideList extends ItemOverrideList{

		public FluidTankItemOverrideList() { super(); }

		@Override
		public IBakedModel resolve(@Nonnull IBakedModel model, @Nonnull ItemStack stack, @Nullable ClientWorld level, @Nullable LivingEntity entity)
		{
			FluidRenderData renderData = FluidTankBlock.RENDER_DATA;
			FluidStack tank = FluidTankItem.GetFluid(stack);
			int capacity = FluidTankItem.GetCapacity(stack);
			if(stack.getItem() instanceof BlockItem)
			{
				BlockItem bi = (BlockItem)stack.getItem();
				if(bi.getBlock() instanceof IFluidTankBlock)
				{
					IFluidTankBlock tankBlock = (IFluidTankBlock)bi.getBlock();
					renderData = tankBlock.getItemRenderData();
				}
			}
			return new FluidTankFinalizedModel(model, tank, capacity, renderData);
		}

	}

}