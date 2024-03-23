package io.github.lightman314.lctech.datagen;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.datagen.common.crafting.TechRecipeProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechBlockTagProvider;
import io.github.lightman314.lctech.datagen.common.tags.TechItemTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LCTech.MODID)
public class LCTechDataEventListener {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        //Recipes
        generator.addProvider(event.includeServer(), new TechRecipeProvider(generator));

        //Tags
        TechBlockTagProvider blockTagProvider = new TechBlockTagProvider(generator, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new TechItemTagProvider(generator, blockTagProvider, existingFileHelper));

    }
}