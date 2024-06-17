package io.github.lightman314.lctech.datagen.common.tags;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechTags;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.IOptionalKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TechItemTagProvider extends ItemTagsProvider {

    public TechItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, LCTech.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {

        //Battery Tag
        this.cTag(TechTags.Items.BATTERIES)
                .add(ModItems.BATTERY)
                .add(ModItems.BATTERY_LARGE);

        //Tank Tag
        this.cTag(TechTags.Items.FLUID_TANK)
                .add(ModBlocks.IRON_TANK)
                .add(ModBlocks.GOLD_TANK)
                .add(ModBlocks.DIAMOND_TANK)
                .add(ModBlocks.NETHERITE_TANK);

        //Trader Tags
        //Add Tech tags to LC Tags
        this.cTag(LCTags.Items.TRADER)
                .addTag(TechTags.Items.TRADER_FLUID)
                .addTag(TechTags.Items.TRADER_ENERGY);
        this.cTag(LCTags.Items.TRADER_NETWORK)
                .addTag(TechTags.Items.TRADER_NETWORK_FLUID)
                .addTag(TechTags.Items.TRADER_NETWORK_ENERGY);
        this.cTag(LCTags.Items.TRADER_INTERFACE)
                .add(ModBlocks.FLUID_TRADER_INTERFACE)
                .add(ModBlocks.ENERGY_TRADER_INTERFACE);
        //Add to personal tags
        this.cTag(TechTags.Items.TRADER_FLUID)
                .add(ModBlocks.FLUID_TAP.get())
                .add(ModBlocks.FLUID_TAP_BUNDLE.get());
        this.cTag(TechTags.Items.TRADER_NETWORK_FLUID)
                .add(ModBlocks.FLUID_NETWORK_TRADER_1)
                .add(ModBlocks.FLUID_NETWORK_TRADER_2)
                .add(ModBlocks.FLUID_NETWORK_TRADER_3)
                .add(ModBlocks.FLUID_NETWORK_TRADER_4);
        this.cTag(TechTags.Items.TRADER_ENERGY)
                .add(ModBlocks.BATTERY_SHOP);
        this.cTag(TechTags.Items.TRADER_NETWORK_ENERGY)
                .add(ModBlocks.ENERGY_NETWORK_TRADER);



    }

    private CustomTagAppender cTag(TagKey<Item> tag) { return new CustomTagAppender(this.tag(tag)); }
    private CustomTagAppender cTag(ResourceLocation tag) { return new CustomTagAppender(this.tag(ItemTags.create(tag))); }

    private record CustomTagAppender(IntrinsicTagAppender<Item> appender) {

        public CustomTagAppender add(ItemLike item) { this.appender.add(item.asItem()); return this; }
        public CustomTagAppender add(RegistryObject<? extends ItemLike> item) { this.add(item.get()); return this; }
        public CustomTagAppender addOptional(RegistryObject<? extends ItemLike> item) { this.appender.addOptional(item.getId()); return this; }
        public CustomTagAppender add(RegistryObjectBundle<? extends ItemLike,?> bundle) {
            bundle.forEach((key,item) -> {
                if(key instanceof IOptionalKey ok)
                {
                    if(ok.isModded())
                        this.addOptional(item);
                    else
                        this.add(item);
                }
                else
                    this.add(item);
            });
            return this;
        }
        public <T> CustomTagAppender add(RegistryObjectBiBundle<? extends ItemLike,T,?> bundle, @Nonnull T key) {
            bundle.forEach((key1,key2,item) -> {
                if(key1 == key)
                {
                    if(key1 instanceof IOptionalKey ok1)
                    {
                        if(ok1.isModded())
                            this.addOptional(item);
                        else if(key2 instanceof IOptionalKey ok2)
                        {
                            if(ok2.isModded())
                                this.addOptional(item);
                            else
                                this.add(item);
                        }
                    }
                    else if(key2 instanceof IOptionalKey ok2)
                    {
                        if(ok2.isModded())
                            this.addOptional(item);
                        else
                            this.add(item);
                    }
                    else
                        this.add(item);
                }
            });
            return this;
        }
        public CustomTagAppender add(RegistryObjectBiBundle<? extends ItemLike,?,?> bundle) {
            bundle.forEach((key1,key2,item) -> {
                if(key1 instanceof IOptionalKey ok1)
                {
                    if(ok1.isModded())
                        this.addOptional(item);
                    else if(key2 instanceof IOptionalKey ok2)
                    {
                        if(ok2.isModded())
                            this.addOptional(item);
                        else
                            this.add(item);
                    }
                    else
                        this.add(item);
                }
                else if(key2 instanceof IOptionalKey ok2)
                {
                    if(ok2.isModded())
                        this.addOptional(item);
                    else
                        this.add(item);
                }
                else
                    this.add(item);
            });
            return this;
        }
        public CustomTagAppender addTag(TagKey<Item> tag) { this.appender.addTag(tag); return this; }
        public CustomTagAppender addTags(List<TagKey<Item>> tags) { for(TagKey<Item> tag : tags) { this.addTag(tag); } return this; }

    }
}
