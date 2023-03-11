package io.github.lightman314.lctech.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

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
	
	public static JsonObject convertFluidStack(FluidStack fluid) {
		JsonObject json = new JsonObject();
		json.addProperty("id", fluid.getFluid().getRegistryName().toString());
		json.addProperty("amount", fluid.getAmount());
		if(fluid.hasTag())
		{
			String tag = fluid.getTag().getAsString();
			json.addProperty("tag", tag);
		}
		return json;
	}
	
	public static FluidStack parseFluidStack(JsonObject json) throws Exception {
		String id = json.get("id").getAsString();
		int amount = json.get("amount").getAsInt();
		FluidStack result = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id)), amount);
		try {
			if(json.has("tag"))
			{
				JsonElement tag = json.get("tag");
				if(tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString())
				{
					//Parse the compound tag
					CompoundNBT compound = JsonToNBT.parseTag(tag.getAsString());
					result.setTag(compound);
				}
				else
				{
					CompoundNBT compound = JsonToNBT.parseTag(FileUtil.GSON.toJson(tag));
					result.setTag(compound);
				}
			}
		} catch(Exception e) { LCTech.LOGGER.error("Error parsing fluid tag data.", e); }
		return result;
	}
	
}
