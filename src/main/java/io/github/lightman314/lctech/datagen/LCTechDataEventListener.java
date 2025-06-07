package io.github.lightman314.lctech.datagen;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.datagen.client.TechFluidRenderDataProvider;
import io.github.lightman314.lctech.datagen.client.TechVariantProvider;
import io.github.lightman314.lctech.datagen.client.language.TechEnglishProvider;
import io.github.lightman314.lctech.datagen.common.crafting.TechRecipeProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechBlockTagProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LCTech.MODID)
public class LCTechDataEventListener {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupHolder = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        //Recipes
        generator.addProvider(event.includeServer(), new TechRecipeProvider(output));

        //Tags
        TechBlockTagProvider blockTagProvider = new TechBlockTagProvider(output, lookupHolder, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new TechItemTagProvider(output, lookupHolder, blockTagProvider.contentsGetter(), existingFileHelper));

        //Language
        generator.addProvider(event.includeClient(), new TechEnglishProvider(output));

        //Fluid Render Data
        generator.addProvider(event.includeClient(), new TechFluidRenderDataProvider(output));

        //Tech Variant Providers
        generator.addProvider(event.includeClient(), new TechVariantProvider(output));

    }
}
