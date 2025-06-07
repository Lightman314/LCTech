package io.github.lightman314.lctech.client.resourcepacks.data.model_variants.properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.*;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidRenderDataList {

    public static VariantProperty<FluidRenderDataList> PROPERTY = new FluidRenderDataProperty();

    private final Map<Direction,List<FluidRenderDataEntry>> values;
    @Nullable
    public FluidRenderData getSided(Direction side, int index) {
        List<FluidRenderDataEntry> list = this.values.getOrDefault(side,ImmutableList.of());
        if(index < 0 || index >= list.size())
            return null;
        return list.get(index).get();
    }
    @Nullable
    @Deprecated
    public FluidRenderData get(int index) { return this.getSided(Direction.NORTH,index); }
    public FluidRenderDataList(FluidRenderDataEntry value) { this(ImmutableList.of(value)); }
    public FluidRenderDataList(List<FluidRenderDataEntry> values) { this(sidelessMap(values)); }
    public FluidRenderDataList(Map<Direction,List<FluidRenderDataEntry>> values) {
        ImmutableMap.Builder<Direction,List<FluidRenderDataEntry>> builder = ImmutableMap.builderWithExpectedSize(4);
        for(int i = 0; i < 4; ++i)
        {
            Direction side = Direction.from2DDataValue(i);
            builder.put(side,ImmutableList.copyOf(values.getOrDefault(side,ImmutableList.of())));
        }
        this.values = builder.buildKeepingLast();
    }

    private static Map<Direction,List<FluidRenderDataEntry>> sidelessMap(List<FluidRenderDataEntry> list)
    {
        Map<Direction,List<FluidRenderDataEntry>> map = new HashMap<>();
        for(int i = 0; i < 4; ++i)
            map.put(Direction.from2DDataValue(i),list);
        return map;
    }

    JsonElement write()
    {
        if(this.sidesEqual())
        {
            JsonArray list = new JsonArray();
            for(FluidRenderDataEntry entry : this.values.get(Direction.NORTH))
                list.add(entry.write());
            if(list.size() == 1)
                return list.get(0);
            return list;
        }
        else
        {
            JsonObject json = new JsonObject();
            for(Direction side : this.values.keySet())
            {
                JsonArray list = new JsonArray();
                for(FluidRenderDataEntry entry : this.values.get(side))
                    list.add(entry.write());
                if(list.size() == 1)
                    json.add(side.toString(),list.get(0));
                else
                    json.add(side.toString(),list);
            }
            return json;
        }
    }

    private boolean sidesEqual()
    {
        List<FluidRenderDataEntry> firstList = this.values.getOrDefault(Direction.from2DDataValue(0),ImmutableList.of());
        for(int i = 1; i < 4; ++i)
        {
            if(!this.values.getOrDefault(Direction.from2DDataValue(i),ImmutableList.of()).equals(firstList))
                return false;
        }
        return true;
    }

    public interface FluidRenderDataEntry
    {
        FluidRenderData get();
        JsonElement write();
        static FluidRenderDataEntry create(ResourceLocation renderDataID) { return new IDEntry(renderDataID); }
        static FluidRenderDataEntry create(FluidRenderData data) { return new InstanceEntry(data); }
    }

    private record IDEntry(ResourceLocation renderDataID) implements FluidRenderDataEntry
    {
        @Override
        public FluidRenderData get() { return FluidRenderDataManager.getDataOrEmpty(this.renderDataID); }
        @Override
        public JsonElement write() { return new JsonPrimitive(this.renderDataID.toString()); }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IDEntry other)
                return other.renderDataID.equals(this.renderDataID);
            return false;
        }
    }

    private record InstanceEntry(FluidRenderData data) implements FluidRenderDataEntry
    {
        @Override
        public FluidRenderData get() { return this.data; }
        @Override
        public JsonElement write() { return this.data.write(); }
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof InstanceEntry other)
                return other.data.equals(this.data);
            return false;
        }
    }

    private static class FluidRenderDataProperty extends VariantProperty<FluidRenderDataList>
    {

        private FluidRenderDataProperty() {}

        @Override
        public FluidRenderDataList parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException {

            String elementName = this.getID().toString();
            if(element.isJsonObject())
            {
                JsonObject object = GsonHelper.convertToJsonObject(element,elementName);
                Map<Direction,List<FluidRenderDataEntry>> map = new HashMap<>();
                for(int i = 0; i < 4; ++i)
                {
                    Direction side = Direction.from2DDataValue(i);
                    if(object.has(side.toString()))
                        map.put(side,parseList(object.get(side.toString()),side.toString()));
                }
                if(!map.isEmpty())
                    return new FluidRenderDataList(map);
            }
            return new FluidRenderDataList(this.parseList(element,elementName));
        }

        private List<FluidRenderDataEntry> parseList(JsonElement element, String name)
        {
            if(element.isJsonArray())
            {
                JsonArray array = GsonHelper.convertToJsonArray(element,name);
                List<FluidRenderDataEntry> list = new ArrayList<>();
                for(int i = 0; i < array.size(); ++i)
                    list.add(this.parseEntry(array.get(i),name + "[" + i + "]"));
                return list;
            }
            else //Assume single-entry field
            {
                return Lists.newArrayList(this.parseEntry(element,name));
            }
        }

        private FluidRenderDataEntry parseEntry(JsonElement element, String name)
        {
            if(element.isJsonPrimitive())
            {
                ResourceLocation dataID = VersionUtil.parseResource(GsonHelper.convertToString(element,name));
                return new IDEntry(dataID);
            }
            else
                return new InstanceEntry(FluidRenderData.parse(GsonHelper.convertToJsonObject(element,name)));
        }

        @Override
        public JsonElement write(Object value) {
            if(value instanceof FluidRenderDataList data)
                return data.write();
            else
                throw new IllegalArgumentException("Value must be a FluidRenderDataEntry element!");
        }

    }

}
