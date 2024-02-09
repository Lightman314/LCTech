package io.github.lightman314.lctech.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.items.FluidShardItem;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

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
	
	public static JsonObject convertFluidStack(FluidStack fluid) {
		JsonObject json = new JsonObject();
		json.addProperty("id", ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString());
		json.addProperty("amount", fluid.getAmount());
		if(fluid.hasTag())
		{
			String tag = fluid.getTag().getAsString();
			json.addProperty("tag", tag);
		}
		return json;
	}
	
	public static FluidStack parseFluidStack(JsonObject json) throws JsonSyntaxException, ResourceLocationException {
		String id = GsonHelper.getAsString(json, "id");
		int amount = GsonHelper.getAsInt(json,"amount");
		FluidStack result = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id)), amount);
		try {
			if(json.has("tag"))
			{
				JsonElement tag = json.get("tag");
				if(tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString())
				{
					//Parse the compound tag
					CompoundTag compound = TagParser.parseTag(tag.getAsString());
					result.setTag(compound);
				}
				else
				{
					CompoundTag compound = TagParser.parseTag(FileUtil.GSON.toJson(tag));
					result.setTag(compound);
				}
			}
		} catch(Exception e) { LCTech.LOGGER.error("Error parsing fluid tag data.", e); }
		return result;
	}
	
}
