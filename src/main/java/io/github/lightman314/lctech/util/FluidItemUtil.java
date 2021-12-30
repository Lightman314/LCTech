package io.github.lightman314.lctech.util;

import io.github.lightman314.lctech.items.FluidShardItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class FluidItemUtil {

	public static ItemStack getFluidDisplayItem(FluidStack fluidStack)
	{
		Fluid fluid = fluidStack.getFluid();
		if(fluid == Fluids.EMPTY)
			return new ItemStack(Items.BUCKET);
		ItemStack fluidItem = fluid.getAttributes().getBucket(fluidStack);
		if(!fluidItem.isEmpty())
			return fluidItem;
		//If the fluid has no bucket, return a fluid shard containing the fluid
		FluidStack tempStack = fluidStack.copy();
		tempStack.setAmount(FluidAttributes.BUCKET_VOLUME);
		return FluidShardItem.GetFluidShard(tempStack);
	}
	
	public static ItemStack getFluidDispayItem(Fluid fluid)
	{
		return getFluidDisplayItem(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME));
	}
	
}
