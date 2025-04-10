package io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.client.util.FluidSides;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidRenderDataManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final FluidRenderDataManager INSTANCE = new FluidRenderDataManager();

    private final Map<ResourceLocation,FluidRenderData> fluidRenderData = new HashMap<>();
    public static FluidRenderData getDataOrEmpty(ResourceLocation id) { return INSTANCE.fluidRenderData.getOrDefault(id,FluidRenderData.CreateFluidRender(0,0,0,0,0,0, FluidSides.Create())); }
    private FluidRenderDataManager() { super(GSON,"lctech/fluid_render_data"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller filler) {
        this.fluidRenderData.clear();
        map.forEach((id,json) -> {
            try {
                FluidRenderData data = FluidRenderData.parse(GsonHelper.convertToJsonObject(json,"top element"));
                this.fluidRenderData.put(id,data);
            } catch (JsonSyntaxException | IllegalArgumentException exception) {
                LCTech.LOGGER.error("Parsing error loading fluid render data {}", id, exception);
            }
        });
    }

}