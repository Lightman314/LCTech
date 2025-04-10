package io.github.lightman314.lctech.datagen.client.generators;

import com.google.gson.JsonObject;
import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class FluidRenderDataProvider implements DataProvider {

    protected final String modid;
    protected final PackOutput output;
    private final PackOutput.PathProvider pathProvider;
    protected FluidRenderDataProvider(PackOutput output, String modid)
    {
        this.modid = modid;
        this.output = output;
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,"lctech/fluid_render_data");
    }

    private final Map<ResourceLocation, FluidRenderData> renderData = new HashMap<>();

    protected abstract void addEntries();

    protected final void addData(String id,FluidRenderData data) { this.addData(VersionUtil.modResource(this.modid,Objects.requireNonNull(id,"ID cannot be null!")),data); }
    protected final void addData(ResourceLocation id, FluidRenderData data) {
        this.renderData.put(Objects.requireNonNull(id,"ID cannot be null!"),Objects.requireNonNull(data,"Data cannot be null!"));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.renderData.clear();
        this.addEntries();
        List<CompletableFuture<?>> results = new ArrayList<>();
        this.renderData.forEach((id,data) -> {
            JsonObject json = data.write();
            Path path = this.pathProvider.json(id);
            if(path == null)
                results.add(CompletableFuture.completedFuture(null));
            else
                results.add(DataProvider.saveStable(cache,json,path));
        });
        return CompletableFuture.allOf(results.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() { return "LC Tech Fluid Render Data: " + this.modid; }

}