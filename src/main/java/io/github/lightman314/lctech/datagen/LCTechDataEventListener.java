package io.github.lightman314.lctech.datagen;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lctech.datagen.client.TechFluidRenderDataProvider;
import io.github.lightman314.lctech.datagen.client.TechVariantProvider;
import io.github.lightman314.lctech.datagen.client.language.TechEnglishProvider;
import io.github.lightman314.lctech.datagen.common.crafting.TechRecipeProvider;
import io.github.lightman314.lctech.datagen.common.loot.TechBlockLootProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechBlockTagProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = LCTech.MODID)
public class LCTechDataEventListener {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event)
    {

        TechConfig.COMMON.confirmSetup();

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

        //Loot Tables
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(TechBlockLootProvider::new, LootContextParamSets.BLOCK)),lookupHolder));

        //Language
        generator.addProvider(event.includeClient(), new TechEnglishProvider(output));

        //Fluid Render Data
        generator.addProvider(event.includeClient(), new TechFluidRenderDataProvider(output));

        //Tech Variant Providers
        generator.addProvider(event.includeClient(), new TechVariantProvider(output));

    }
}
