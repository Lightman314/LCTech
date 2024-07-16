package io.github.lightman314.lctech.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;

public class FluidItemUtil {

	public static ItemStack getFluidDisplayItem(FluidStack fluidStack)
	{
		Fluid fluid = fluidStack.getFluid();
		if(fluid == Fluids.EMPTY)
			return new ItemStack(Items.BUCKET);
		ItemStack fluidItem = fluid.getFluidType().getBucket(fluidStack);
		if(!fluidItem.isEmpty())
			return fluidItem;
		//If the fluid has no bucket, return a fluid shard containing the fluid
		FluidStack tempStack = fluidStack.copy();
		tempStack.setAmount(FluidType.BUCKET_VOLUME);
		return FluidShardItem.GetFluidShard(tempStack);
	}
	
	public static ItemStack getFluidDispayItem(Fluid fluid)
	{
		return getFluidDisplayItem(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
	}
	
	public static JsonElement convertFluidStack(@Nonnull FluidStack fluid, @Nonnull HolderLookup.Provider lookup) {
		return FluidStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,lookup),fluid).getOrThrow();
	}
	
	public static FluidStack parseFluidStack(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
		return FluidStack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,lookup),json).getOrThrow(JsonSyntaxException::new).getFirst();
	}
	
}
