package io.github.lightman314.lctech.datagen;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.datagen.client.TechFluidRenderDataProvider;
import io.github.lightman314.lctech.datagen.client.language.TechEnglishProvider;
import io.github.lightman314.lctech.datagen.common.crafting.TechRecipeProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechBlockTagProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = LCTech.MODID)
public class LCTechDataEventListener {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupHolder = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        //Recipes
        generator.addProvider(event.includeServer(), new TechRecipeProvider(output, lookupHolder));

        //Tags
        TechBlockTagProvider blockTagProvider = new TechBlockTagProvider(output, lookupHolder, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new TechItemTagProvider(output, lookupHolder, blockTagProvider.contentsGetter(), existingFileHelper));

        //Language
        generator.addProvider(event.includeClient(), new TechEnglishProvider(output));

        //Fluid Render Data
        generator.addProvider(event.includeClient(), new TechFluidRenderDataProvider(output));

    }
}
