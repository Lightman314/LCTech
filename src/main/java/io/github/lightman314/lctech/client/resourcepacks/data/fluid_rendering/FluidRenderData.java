package io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidSides;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FluidRenderData
{

	public final float x;
	public final float y;
	public final float z;
	public final float width;
	public final float height;
	public final float getHeight()
	{
		return height * fillPercent;
	}
	public final float depth;
	public final FluidSides sides;

	private float fillPercent = 1f;
	public float getFillPercent() { return this.fillPercent; }
	public void setFillPercent(float fillPercent) { this.fillPercent = MathUtil.clamp(fillPercent, 0f, 1f); }

	private FluidRenderData(float x, float y, float z, float width, float height, float depth, FluidSides sides)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.sides = sides;
	}

	public FluidRenderData withSides(FluidSides sidesOverride) { return new FluidRenderData(this.x, this.y, this.z, this.width, this.height, this.depth, sidesOverride); }
	public FluidRenderData withSides(boolean shouldOverride, FluidSides sidesOverride) { return shouldOverride ? this.withSides(sidesOverride) : this; }
	public FluidRenderData withSides(boolean overrideFlag, FluidSides sidesOverride1, FluidSides sidesOverride2) { return overrideFlag ? this.withSides(sidesOverride1) : this.withSides(sidesOverride2); }

	public static FluidRenderData CreateFluidRender(float x, float y, float z, float width, float height, float depth)
	{
		return CreateFluidRender(x, y, z, width, height, depth, FluidSides.ALL);
	}

	public static FluidRenderData CreateFluidRender(float x, float y, float z, float width, float height, float depth, FluidSides sides)
	{
		return new FluidRenderData(x/16f, y/16f, z/16f, width/16f, height/16f, depth/16f, sides);
	}

	public static FluidRenderData parse(@Nonnull JsonObject json) throws JsonSyntaxException, IllegalArgumentException
	{
		float x = GsonHelper.getAsFloat(json,"x");
		float y = GsonHelper.getAsFloat(json,"y");
		float z = GsonHelper.getAsFloat(json,"z");
		float width = GsonHelper.getAsFloat(json,"width");
		float height = GsonHelper.getAsFloat(json,"height");
		float depth = GsonHelper.getAsFloat(json,"depth");
		FluidSides sides = FluidSides.ALL;
		if(json.has("sides"))
		{
			JsonArray sidesList = GsonHelper.getAsJsonArray(json,"sides");
			List<Direction> result = new ArrayList<>();
			for(int i = 0; i < sidesList.size(); ++i)
			{
				String entry = GsonHelper.convertToString(sidesList.get(i),"sides[" + i + "]");
				Direction entryValue = EnumUtil.enumFromString(entry,Direction.values(),null);
				if(entryValue == null)
					LCTech.LOGGER.warn("Could not parse {} as a Direction",entry);
				else if(result.contains(entryValue))
					LCTech.LOGGER.warn("Duplicate side {}",entry);
				else
					result.add(entryValue);
			}
			sides = FluidSides.Create(result::contains);
		}
		return new FluidRenderData(x,y,z,width,height,depth,sides);
	}

	public JsonObject write()
	{
		JsonObject json = new JsonObject();
		json.addProperty("x",this.x);
		json.addProperty("y",this.y);
		json.addProperty("z",this.z);
		json.addProperty("width",this.width);
		json.addProperty("height",this.height);
		json.addProperty("depth",this.depth);
		if(!this.sides.equals(FluidSides.ALL))
		{
			JsonArray sideList = new JsonArray();
			for(Direction side : Direction.values())
			{
				if(this.sides.test(side))
					sideList.add(side.toString());
			}
			json.add("sides",sideList);
		}
		return json;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FluidRenderData other)
			return this.x == other.x && this.y == other.y && this.z == other.z && this.width == other.width && this.height == other.height && this.depth == other.depth && other.sides.equals(this.sides);
		return false;
	}

}