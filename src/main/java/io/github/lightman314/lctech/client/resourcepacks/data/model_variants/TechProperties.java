package io.github.lightman314.lctech.client.resourcepacks.data.model_variants;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.VariantProperty;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TechProperties {

    public static final VariantProperty<FluidRenderDataList> FLUID_RENDER_DATA = new FluidRenderDataProperty();

    private static class FluidRenderDataProperty extends VariantProperty<FluidRenderDataList>
    {

        private FluidRenderDataProperty() {}

        @Override
        public FluidRenderDataList parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException {
            List<FluidRenderDataEntry> list = new ArrayList<>();
            String elementName = this.getID().toString();
            if(element.isJsonArray())
            {
                JsonArray arrray = GsonHelper.convertToJsonArray(element,elementName);
                for(int i = 0; i < arrray.size(); ++i)
                    list.add(this.parseEntry(arrray.get(i),elementName + "[" + i + "]"));
            }
            else
            {
                list.add(this.parseEntry(element,elementName));
            }
            return new FluidRenderDataList(list);
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

    public static class FluidRenderDataList
    {

        private final List<FluidRenderDataEntry> values;
        @Nullable
        public FluidRenderData get(int index) { if(index < 0 || index >= this.values.size()) return null; return this.values.get(index).get(); }
        public FluidRenderDataList(FluidRenderDataEntry value) { this(ImmutableList.of(value)); }
        public FluidRenderDataList(List<FluidRenderDataEntry> values) { this.values = values; }

        JsonArray write()
        {
            JsonArray list = new JsonArray();
            for(FluidRenderDataEntry entry : this.values)
                list.add(entry.write());
            return list;
        }
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
    }

    private record InstanceEntry(FluidRenderData data) implements FluidRenderDataEntry
    {
        @Override
        public FluidRenderData get() { return this.data; }
        @Override
        public JsonElement write() { return this.data.write(); }
    }

}
