package io.github.lightman314.lctech.datagen.common.crafting;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechTags;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.crafting.condition.TechCraftingConditions;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.LCCraftingConditions;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TechRecipeProvider extends RecipeProvider {

    public TechRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) { super(output,lookup); }

    @Override
    protected void buildRecipes(@Nonnull RecipeOutput output) {

        //Battery Recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BATTERY.get())
                .unlockedBy("redstone", LazyTrigger(Tags.Items.DUSTS_REDSTONE))
                .unlockedBy("batteries", LazyTrigger(TechTags.Items.BATTERIES))
                .unlockedBy("energy_trader", LazyTrigger(TechTags.Items.TRADER_ENERGY))
                .pattern(" i ")
                .pattern("crc")
                .pattern("crc")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.INGOTS_COPPER)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .save(output.withConditions(TechCraftingConditions.Batteries.INSTANCE), ID("batteries/battery"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BATTERY_LARGE.get())
                .unlockedBy("redstone", LazyTrigger(Tags.Items.DUSTS_REDSTONE))
                .unlockedBy("batteries", LazyTrigger(TechTags.Items.BATTERIES))
                .unlockedBy("energy_trader", LazyTrigger(TechTags.Items.TRADER_ENERGY))
                .pattern(" i ")
                .pattern("crc")
                .pattern("crc")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.INGOTS_COPPER)
                .define('r', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .save(output.withConditions(TechCraftingConditions.Batteries.INSTANCE),ID("batteries/large_battery"));

        //Fluid Tanks
        GenerateTankRecipe(output, Tags.Items.INGOTS_IRON, ModBlocks.IRON_TANK);
        GenerateTankRecipe(output, Tags.Items.INGOTS_GOLD, ModBlocks.GOLD_TANK);
        GenerateTankRecipe(output, Tags.Items.GEMS_DIAMOND, ModBlocks.DIAMOND_TANK);
        SmithingTransformRecipeBuilder.smithing(
                    Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                    Ingredient.of(ModBlocks.DIAMOND_TANK.get()),
                    Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                    RecipeCategory.MISC,
                    ModBlocks.NETHERITE_TANK.get().asItem())
                .unlocks("material", LazyTrigger(Tags.Items.INGOTS_NETHERITE))
                .unlocks("glass", LazyTrigger(Tags.Items.GLASS_BLOCKS_COLORLESS))
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModBlocks.DIAMOND_TANK))
                .save(output.withConditions(TechCraftingConditions.FluidTank.INSTANCE),ItemID("fluid_tanks/", ModBlocks.NETHERITE_TANK));

        //Fluid traders
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_TAP.get())
                .unlockedBy("bucket", LazyTrigger(Items.BUCKET))
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .pattern("c")
                .pattern("t")
                .pattern("n")
                .define('c', TradingCore())
                .define('t', ModBlocks.IRON_TANK.get())
                .define('n', Tags.Items.NUGGETS_IRON)
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE),ID("traders/fluid/fluid_tap"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_TAP_BUNDLE.get())
                .unlockedBy("bucket", LazyTrigger(Items.BUCKET))
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .unlockedBy("tap", LazyTrigger(ModBlocks.FLUID_TAP))
                .pattern(" t ")
                .pattern("txt")
                .pattern(" i ")
                .define('x', ModBlocks.FLUID_TAP.get())
                .define('t', ModBlocks.IRON_TANK.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE),ID("traders/fluid/fluid_tap_bundle"));

        //Fluid Network Traders
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_NETWORK_TRADER_1.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .group("fluid_network_trader")
                .pattern("iii")
                .pattern("txt")
                .pattern("iei")
                .define('x', TradingCore())
                .define('e', Items.ENDER_EYE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('t', ModBlocks.IRON_TANK.get())
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE,LCCraftingConditions.NETWORK_TRADER),ID("traders/network/fluid_network_trader_1"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_NETWORK_TRADER_2.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .group("fluid_network_trader")
                .pattern("txt")
                .define('t', ModBlocks.IRON_TANK.get())
                .define('x', ModBlocks.FLUID_NETWORK_TRADER_1.get())
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE,LCCraftingConditions.NETWORK_TRADER),ID("traders/network/fluid_network_trader_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_NETWORK_TRADER_3.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .group("fluid_network_trader")
                .pattern("txt")
                .define('t', ModBlocks.IRON_TANK.get())
                .define('x', ModBlocks.FLUID_NETWORK_TRADER_2.get())
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE,LCCraftingConditions.NETWORK_TRADER),ID("traders/network/fluid_network_trader_3"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_NETWORK_TRADER_4.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .group("fluid_network_trader")
                .pattern("txt")
                .define('t', ModBlocks.IRON_TANK.get())
                .define('x', ModBlocks.FLUID_NETWORK_TRADER_3.get())
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE,LCCraftingConditions.NETWORK_TRADER),ID("traders/network/fluid_network_trader_4"));

        //Energy Traders
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BATTERY_SHOP.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .pattern("ibi")
                .pattern("ici")
                .define('c', TradingCore())
                .define('b', ModItems.BATTERY.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .save(output.withConditions(TechCraftingConditions.EnergyTrader.INSTANCE),ID("traders/energy/battery_shop"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ENERGY_NETWORK_TRADER.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .pattern("ibi")
                .pattern("ixi")
                .pattern("iei")
                .define('x', TradingCore())
                .define('e', Items.ENDER_EYE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('b', ModItems.BATTERY.get())
                .save(output.withConditions(TechCraftingConditions.EnergyTrader.INSTANCE, LCCraftingConditions.NETWORK_TRADER),ID("traders/network/energy_network_trader"));

        //Trader Interfaces
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_TRADER_INTERFACE.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .pattern("ici")
                .pattern("iti")
                .pattern("ici")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('t', LCTags.Items.NETWORK_TERMINAL)
                .define('c', ModBlocks.IRON_TANK.get())
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE, LCCraftingConditions.TRADER_INTERFACE),ItemID(ModBlocks.FLUID_TRADER_INTERFACE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ENERGY_TRADER_INTERFACE.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .pattern("ici")
                .pattern("iti")
                .pattern("ici")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('t', LCTags.Items.NETWORK_TERMINAL)
                .define('c', ModItems.BATTERY.get())
                .save(output.withConditions(TechCraftingConditions.EnergyTrader.INSTANCE, LCCraftingConditions.TRADER_INTERFACE),ItemID(ModBlocks.ENERGY_TRADER_INTERFACE));

        //Fluid Capacity Upgrades
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(Items.BUCKET),
                Ingredient.of(Tags.Items.INGOTS_IRON),
                RecipeCategory.MISC,
                ModItems.FLUID_CAPACITY_UPGRADE_1.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE),ItemID("upgrades/",ModItems.FLUID_CAPACITY_UPGRADE_1));
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.FLUID_CAPACITY_UPGRADE_1.get()),
                Ingredient.of(Tags.Items.INGOTS_GOLD),
                RecipeCategory.MISC,
                ModItems.FLUID_CAPACITY_UPGRADE_2.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModItems.FLUID_CAPACITY_UPGRADE_1))
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE),ItemID("upgrades/",ModItems.FLUID_CAPACITY_UPGRADE_2));
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.FLUID_CAPACITY_UPGRADE_2.get()),
                Ingredient.of(Tags.Items.GEMS_DIAMOND),
                RecipeCategory.MISC,
                ModItems.FLUID_CAPACITY_UPGRADE_3.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModItems.FLUID_CAPACITY_UPGRADE_2))
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE),ItemID("upgrades/",ModItems.FLUID_CAPACITY_UPGRADE_3));
        SmithingTransformRecipeBuilder.smithing(
                        SmithingTemplate(),
                        Ingredient.of(ModItems.FLUID_CAPACITY_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.FLUID_CAPACITY_UPGRADE_4.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModItems.FLUID_CAPACITY_UPGRADE_3))
                .save(output.withConditions(TechCraftingConditions.FluidTrader.INSTANCE),ItemID("upgrades/",ModItems.FLUID_CAPACITY_UPGRADE_4));

        //Energy Capacity Upgrades
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.BATTERY.get()),
                Ingredient.of(Tags.Items.INGOTS_IRON),
                RecipeCategory.MISC,
                ModItems.ENERGY_CAPACITY_UPGRADE_1.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .save(output.withConditions(TechCraftingConditions.EnergyTrader.INSTANCE),ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_1));

        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.ENERGY_CAPACITY_UPGRADE_1.get()),
                Ingredient.of(Tags.Items.INGOTS_GOLD),
                RecipeCategory.MISC,
                ModItems.ENERGY_CAPACITY_UPGRADE_2.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .unlocks("previous", LazyTrigger(ModItems.ENERGY_CAPACITY_UPGRADE_1))
                .save(output.withConditions(TechCraftingConditions.EnergyTrader.INSTANCE),ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_2));

        SmithingTransformRecipeBuilder.smithing(
                        SmithingTemplate(),
                        Ingredient.of(ModItems.ENERGY_CAPACITY_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.GEMS_DIAMOND),
                        RecipeCategory.MISC,
                        ModItems.ENERGY_CAPACITY_UPGRADE_3.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .unlocks("previous", LazyTrigger(ModItems.ENERGY_CAPACITY_UPGRADE_2))
                .save(output.withConditions(TechCraftingConditions.EnergyTrader.INSTANCE),ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_3));

        SmithingTransformRecipeBuilder.smithing(
                        SmithingTemplate(),
                        Ingredient.of(ModItems.ENERGY_CAPACITY_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.ENERGY_CAPACITY_UPGRADE_4.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .unlocks("previous", LazyTrigger(ModItems.ENERGY_CAPACITY_UPGRADE_3))
                .save(output.withConditions(TechCraftingConditions.EnergyTrader.INSTANCE),ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_4));


        //0.2.1.3
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VOID_TANK.get())
                .pattern("mgm")
                .pattern("geg")
                .pattern("mgm")
                .unlockedBy("material", LazyTrigger(Tags.Items.OBSIDIANS))
                .unlockedBy("glass", LazyTrigger(Tags.Items.GLASS_BLOCKS_COLORLESS))
                .unlockedBy("bucket", LazyTrigger(Items.BUCKET))
                .unlockedBy("pearl", LazyTrigger(Tags.Items.ENDER_PEARLS))
                .define('m', Tags.Items.OBSIDIANS)
                .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('e', Tags.Items.ENDER_PEARLS)
                .save(output.withConditions(TechCraftingConditions.VoidTank.INSTANCE),ID("fluid_tanks/void_tank"));

    }

    private static void GenerateTankRecipe(@Nonnull RecipeOutput output, @Nonnull TagKey<Item> ingredient, @Nonnull Supplier<? extends ItemLike> result)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get())
                .pattern("mgm")
                .pattern("g g")
                .pattern("mgm")
                .unlockedBy("material", LazyTrigger(ingredient))
                .unlockedBy("glass", LazyTrigger(Tags.Items.GLASS_BLOCKS_COLORLESS))
                .unlockedBy("bucket", LazyTrigger(Items.BUCKET))
                .define('m', ingredient)
                .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .save(output.withConditions(TechCraftingConditions.FluidTank.INSTANCE),ItemID("fluid_tanks/", result));
    }

    private static String ItemPath(ItemLike item) { return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath(); }
    private static String ItemPath(Supplier<? extends ItemLike> item) { return ItemPath(item.get()); }
    private static ResourceLocation ItemID(String prefix, ItemLike item) { return ID(prefix + ItemPath(item)); }
    private static ResourceLocation ItemID(Supplier<? extends ItemLike> item) { return ID(ItemPath(item)); }
    private static ResourceLocation ItemID(String prefix, Supplier<? extends ItemLike> item) { return ID(prefix + ItemPath(item)); }
    private static ResourceLocation ID(String path) { return ResourceLocation.fromNamespaceAndPath(LCTech.MODID, path); }


    private static ItemLike TradingCore() { return io.github.lightman314.lightmanscurrency.common.core.ModItems.TRADING_CORE.get(); }
    private static Ingredient SmithingTemplate() { return Ingredient.of(io.github.lightman314.lightmanscurrency.common.core.ModItems.UPGRADE_SMITHING_TEMPLATE.get()); }

    private static Criterion<?> TraderKnowledge() { return LazyTrigger(LCTags.Items.TRADER); }
    private static Criterion<?> TerminalKnowledge() { return LazyTrigger(LCTags.Items.NETWORK_TERMINAL); }

    private static Criterion<?> LazyTrigger(ItemLike item) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item).build()); }
    private static Criterion<?> LazyTrigger(List<? extends ItemLike> items) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items.toArray(new ItemLike[0])).build()); }
    private static Criterion<?> LazyTrigger(Supplier<? extends ItemLike> item) { return LazyTrigger(item.get()); }
    private static Criterion<?> LazyTrigger(TagKey<Item> tag) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build()); }

}
