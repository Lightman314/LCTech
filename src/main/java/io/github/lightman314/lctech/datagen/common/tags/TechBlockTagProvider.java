package io.github.lightman314.lctech.datagen.common.tags;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.IOptionalKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TechBlockTagProvider extends BlockTagsProvider {
    public TechBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, LCTech.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {

        //Add to LC Tags
        this.cTag(LCTags.Blocks.OWNER_PROTECTED)
                .add(ModBlocks.FLUID_TAP)
                .add(ModBlocks.FLUID_TAP_BUNDLE)
                .add(ModBlocks.FLUID_NETWORK_TRADER_1)
                .add(ModBlocks.FLUID_NETWORK_TRADER_2)
                .add(ModBlocks.FLUID_NETWORK_TRADER_3)
                .add(ModBlocks.FLUID_NETWORK_TRADER_4)
                .add(ModBlocks.BATTERY_SHOP)
                .add(ModBlocks.ENERGY_NETWORK_TRADER)
                .add(ModBlocks.FLUID_TRADER_INTERFACE)
                .add(ModBlocks.ENERGY_TRADER_INTERFACE);

        //Vanilla Tags
        //Note: Don't need dragon or wither immune tags, as #lightmanscurrency:protected is added to this tag automatically.
        this.cTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.IRON_TANK)
                .add(ModBlocks.GOLD_TANK)
                .add(ModBlocks.DIAMOND_TANK)
                .add(ModBlocks.NETHERITE_TANK)
                .add(ModBlocks.VOID_TANK)
                .add(ModBlocks.FLUID_TAP)
                .add(ModBlocks.FLUID_TAP_BUNDLE)
                .add(ModBlocks.FLUID_NETWORK_TRADER_1)
                .add(ModBlocks.FLUID_NETWORK_TRADER_2)
                .add(ModBlocks.FLUID_NETWORK_TRADER_3)
                .add(ModBlocks.FLUID_NETWORK_TRADER_4)
                .add(ModBlocks.BATTERY_SHOP)
                .add(ModBlocks.ENERGY_NETWORK_TRADER)
                .add(ModBlocks.FLUID_TRADER_INTERFACE)
                .add(ModBlocks.ENERGY_TRADER_INTERFACE);

    }

    private CustomTagAppender cTag(TagKey<Block> tag) {
        return new CustomTagAppender(this.tag(tag));
    }

    private CustomTagAppender cTag(ResourceLocation tag) {
        return new CustomTagAppender(this.tag(BlockTags.create(tag)));
    }

    private record CustomTagAppender(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> appender) {

        public CustomTagAppender add(Block block) {
            this.appender.add(block);
            return this;
        }

        public CustomTagAppender add(Supplier<? extends Block> block) {
            this.appender.add(block.get());
            return this;
        }

        public CustomTagAppender addOptional(Supplier<? extends Block> block) {
            this.appender.addOptional(BuiltInRegistries.BLOCK.getKey(block.get()));
            return this;
        }

        public CustomTagAppender add(RegistryObjectBundle<? extends Block, ?> bundle) {
            bundle.forEach((key, block) -> {
                if (key instanceof IOptionalKey ok) {
                    if (ok.isModded()) {
                        this.addOptional(block);
                    } else {
                        this.add(block);
                    }
                } else {
                    this.add(block);
                }

            });
            return this;
        }

        public CustomTagAppender add(RegistryObjectBiBundle<? extends Block, ?, ?> bundle) {
            bundle.forEach((key1, key2, block) -> {
                if (key1 instanceof IOptionalKey ok1) {
                    if (ok1.isModded()) {
                        this.addOptional(block);
                    } else if (key2 instanceof IOptionalKey) {
                        IOptionalKey ok2 = (IOptionalKey)key2;
                        if (ok2.isModded()) {
                            this.addOptional(block);
                        } else {
                            this.add(block);
                        }
                    } else {
                        this.add(block);
                    }
                } else if (key2 instanceof IOptionalKey ok2x) {
                    if (ok2x.isModded()) {
                        this.addOptional(block);
                    } else {
                        this.add(block);
                    }
                } else {
                    this.add(block);
                }

            });
            return this;
        }

        public CustomTagAppender addTag(TagKey<Block> tag) {
            this.appender.addTag(tag);
            return this;
        }

        public IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> appender() {
            return this.appender;
        }
    }

}
