package io.github.lightman314.lctech.client.models.items;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lctech.blocks.FluidTankBlock;
import io.github.lightman314.lctech.blocks.IFluidTankBlock;
import io.github.lightman314.lctech.client.util.FluidRenderUtil.FluidRenderData;
import io.github.lightman314.lctech.items.FluidTankItem;
import io.github.lightman314.lctech.tileentities.FluidTankTileEntity;
import net.minecraft.block.Block;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
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
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
	{
		return this.baseFluidTankModel.getQuads(state, side, rand);
	}
	
	@Override
	public ItemOverrideList getOverrides()
	{
		return this.fluidTankItemOverrideList;
	}
	
	@Override
	public boolean isAmbientOcclusion() {
		return this.baseFluidTankModel.isAmbientOcclusion();
	}
	
	@Override
	public boolean isGui3d() {
		return this.baseFluidTankModel.isGui3d();
	}
	
	@Override
	public boolean isSideLit() {
		return this.baseFluidTankModel.isSideLit();
	}
	
	@Override
	public boolean isBuiltInRenderer() {
		return this.baseFluidTankModel.isBuiltInRenderer();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleTexture() {
		return this.baseFluidTankModel.getParticleTexture();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public ItemCameraTransforms getItemCameraTransforms() {
		return this.baseFluidTankModel.getItemCameraTransforms();
	}
	
	@Override
	@Nonnull
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
		throw new AssertionError("FluidTankModel::getQuads(IModelData) should never be called");
	}
	
	@Override
	@Nonnull
	public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		throw new AssertionError("FluidTankModel::getModelData should never be called");
	}
	
	public class FluidTankItemOverrideList extends ItemOverrideList{

		public FluidTankItemOverrideList() { super(); }
		
		@Override
		public IBakedModel getOverrideModel(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity)
		{
			FluidStack tank = FluidStack.EMPTY;
			int capacity = FluidTankTileEntity.DEFAULT_CAPACITY;
			FluidRenderData renderData = FluidTankBlock.RENDER_DATA;
			if(stack != null)
			{
				tank = FluidTankItem.GetFluid(stack);
				capacity = FluidTankItem.GetCapacity(stack);
				if(stack.getItem() instanceof BlockItem)
				{
					Block block = ((BlockItem)stack.getItem()).getBlock();
					if(block instanceof IFluidTankBlock)
					{
						renderData = ((IFluidTankBlock)block).getRenderData();
					}
				}
			}
			return new FluidTankFinalizedModel(originalModel, tank, capacity, renderData);
		}
		
	}
	
}
