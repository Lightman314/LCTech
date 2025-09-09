package io.github.lightman314.lctech.datagen.common.crafting;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.TechTags;
import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lctech.common.core.ModItems;
import io.github.lightman314.lctech.common.crafting.condition.TechCraftingConditions;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.LCCraftingConditions;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TechRecipeProvider extends EasyRecipeProvider {

    public TechRecipeProvider(PackOutput output) { super(output); }

    private static final String ADV_PREFIX = "recipes/misc/";

    @Override
    protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        
        LCCraftingConditions.register();

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
                .save(makeConditional(ID("batteries/battery"), consumer, TechCraftingConditions.BATTERIES));

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
                .save(makeConditional(ID("batteries/large_battery"), consumer, TechCraftingConditions.BATTERIES));

        //Fluid Tanks
        GenerateTankRecipe(consumer, Tags.Items.INGOTS_IRON, ModBlocks.IRON_TANK);
        GenerateTankRecipe(consumer, Tags.Items.INGOTS_GOLD, ModBlocks.GOLD_TANK);
        GenerateTankRecipe(consumer, Tags.Items.GEMS_DIAMOND, ModBlocks.DIAMOND_TANK);
        SmithingTransformRecipeBuilder.smithing(
                    Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                    Ingredient.of(ModBlocks.DIAMOND_TANK.get()),
                    Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                    RecipeCategory.MISC,
                    ModBlocks.NETHERITE_TANK.get().asItem())
                .unlocks("material", LazyTrigger(Tags.Items.INGOTS_NETHERITE))
                .unlocks("glass", LazyTrigger(Tags.Items.GLASS_COLORLESS))
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModBlocks.DIAMOND_TANK))
                .save(makeConditional(ItemID("fluid_tanks/", ModBlocks.NETHERITE_TANK), consumer, TechCraftingConditions.FLUID_TANK), ItemID("fluid_tanks/", ModBlocks.NETHERITE_TANK));

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
                .save(makeConditional(ID("traders/fluid/fluid_tap"), consumer, TechCraftingConditions.FLUID_TRADER));

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
                .save(makeConditional(ID("traders/fluid/fluid_tap_bundle"), consumer, TechCraftingConditions.FLUID_TRADER));

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
                .save(makeConditional(ID("traders/network/fluid_network_trader_1"), consumer, TechCraftingConditions.FLUID_TRADER, LCCraftingConditions.NETWORK_TRADER));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_NETWORK_TRADER_2.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .group("fluid_network_trader")
                .pattern("txt")
                .define('t', ModBlocks.IRON_TANK.get())
                .define('x', ModBlocks.FLUID_NETWORK_TRADER_1.get())
                .save(makeConditional(ID("traders/network/fluid_network_trader_2"), consumer, TechCraftingConditions.FLUID_TRADER, LCCraftingConditions.NETWORK_TRADER));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_NETWORK_TRADER_3.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .group("fluid_network_trader")
                .pattern("txt")
                .define('t', ModBlocks.IRON_TANK.get())
                .define('x', ModBlocks.FLUID_NETWORK_TRADER_2.get())
                .save(makeConditional(ID("traders/network/fluid_network_trader_3"), consumer, TechCraftingConditions.FLUID_TRADER, LCCraftingConditions.NETWORK_TRADER));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_NETWORK_TRADER_4.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .unlockedBy("tank", LazyTrigger(TechTags.Items.FLUID_TANK))
                .group("fluid_network_trader")
                .pattern("txt")
                .define('t', ModBlocks.IRON_TANK.get())
                .define('x', ModBlocks.FLUID_NETWORK_TRADER_3.get())
                .save(makeConditional(ID("traders/network/fluid_network_trader_4"), consumer, TechCraftingConditions.FLUID_TRADER, LCCraftingConditions.NETWORK_TRADER));

        //Energy Traders
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BATTERY_SHOP.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .pattern("ibi")
                .pattern("ici")
                .define('c', TradingCore())
                .define('b', ModItems.BATTERY.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .save(makeConditional(ID("traders/energy/battery_shop"), consumer, TechCraftingConditions.ENERGY_TRADER));

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
                .save(makeConditional(ID("traders/network/energy_network_trader"), consumer, TechCraftingConditions.ENERGY_TRADER, LCCraftingConditions.NETWORK_TRADER));

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
                .save(makeConditional(ItemID(ModBlocks.FLUID_TRADER_INTERFACE), consumer, TechCraftingConditions.FLUID_TRADER, LCCraftingConditions.TRADER_INTERFACE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ENERGY_TRADER_INTERFACE.get())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", TerminalKnowledge())
                .pattern("ici")
                .pattern("iti")
                .pattern("ici")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('t', LCTags.Items.NETWORK_TERMINAL)
                .define('c', ModItems.BATTERY.get())
                .save(makeConditional(ItemID(ModBlocks.ENERGY_TRADER_INTERFACE), consumer, TechCraftingConditions.ENERGY_TRADER, LCCraftingConditions.TRADER_INTERFACE));

        //Fluid Capacity Upgrades
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(Items.BUCKET),
                Ingredient.of(Tags.Items.INGOTS_IRON),
                RecipeCategory.MISC,
                ModItems.FLUID_CAPACITY_UPGRADE_1.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .save(makeConditional(ItemID("upgrades/",ModItems.FLUID_CAPACITY_UPGRADE_1),consumer, TechCraftingConditions.FLUID_TRADER),"fcu1");
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.FLUID_CAPACITY_UPGRADE_1.get()),
                Ingredient.of(Tags.Items.INGOTS_GOLD),
                RecipeCategory.MISC,
                ModItems.FLUID_CAPACITY_UPGRADE_2.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModItems.FLUID_CAPACITY_UPGRADE_1))
                .save(makeConditional(ItemID("upgrades/", ModItems.FLUID_CAPACITY_UPGRADE_2), consumer, TechCraftingConditions.FLUID_TRADER),"fcu2");
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.FLUID_CAPACITY_UPGRADE_2.get()),
                Ingredient.of(Tags.Items.GEMS_DIAMOND),
                RecipeCategory.MISC,
                ModItems.FLUID_CAPACITY_UPGRADE_3.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModItems.FLUID_CAPACITY_UPGRADE_2))
                .save(makeConditional(ItemID("upgrades/", ModItems.FLUID_CAPACITY_UPGRADE_3), consumer, TechCraftingConditions.FLUID_TRADER),"fcu3");
        SmithingTransformRecipeBuilder.smithing(
                        SmithingTemplate(),
                        Ingredient.of(ModItems.FLUID_CAPACITY_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.FLUID_CAPACITY_UPGRADE_4.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("bucket", LazyTrigger(Items.BUCKET))
                .unlocks("previous", LazyTrigger(ModItems.FLUID_CAPACITY_UPGRADE_3))
                .save(makeConditional(ItemID("upgrades/", ModItems.FLUID_CAPACITY_UPGRADE_4), consumer, TechCraftingConditions.FLUID_TRADER),"fcu4");

        //Energy Capacity Upgrades
        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.BATTERY.get()),
                Ingredient.of(Tags.Items.INGOTS_IRON),
                RecipeCategory.MISC,
                ModItems.ENERGY_CAPACITY_UPGRADE_1.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .save(makeConditional(ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_1), consumer, TechCraftingConditions.ENERGY_TRADER),"ecu1");

        SmithingTransformRecipeBuilder.smithing(
                SmithingTemplate(),
                Ingredient.of(ModItems.ENERGY_CAPACITY_UPGRADE_1.get()),
                Ingredient.of(Tags.Items.INGOTS_GOLD),
                RecipeCategory.MISC,
                ModItems.ENERGY_CAPACITY_UPGRADE_2.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .unlocks("previous", LazyTrigger(ModItems.ENERGY_CAPACITY_UPGRADE_1))
                .save(makeConditional(ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_2), consumer, TechCraftingConditions.ENERGY_TRADER),"ecu2");

        SmithingTransformRecipeBuilder.smithing(
                        SmithingTemplate(),
                        Ingredient.of(ModItems.ENERGY_CAPACITY_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.GEMS_DIAMOND),
                        RecipeCategory.MISC,
                        ModItems.ENERGY_CAPACITY_UPGRADE_3.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .unlocks("previous", LazyTrigger(ModItems.ENERGY_CAPACITY_UPGRADE_2))
                .save(makeConditional(ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_3), consumer, TechCraftingConditions.ENERGY_TRADER),"ecu3");

        SmithingTransformRecipeBuilder.smithing(
                        SmithingTemplate(),
                        Ingredient.of(ModItems.ENERGY_CAPACITY_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.ENERGY_CAPACITY_UPGRADE_4.get())
                .unlocks("trader", TraderKnowledge())
                .unlocks("battery", LazyTrigger(TechTags.Items.BATTERIES))
                .unlocks("previous", LazyTrigger(ModItems.ENERGY_CAPACITY_UPGRADE_3))
                .save(makeConditional(ItemID("upgrades/",ModItems.ENERGY_CAPACITY_UPGRADE_4), consumer, TechCraftingConditions.ENERGY_TRADER),"ecu4");


        //0.2.1.3
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VOID_TANK.get())
                .pattern("mgm")
                .pattern("geg")
                .pattern("mgm")
                .unlockedBy("material", LazyTrigger(Tags.Items.OBSIDIAN))
                .unlockedBy("glass", LazyTrigger(Tags.Items.GLASS_COLORLESS))
                .unlockedBy("bucket", LazyTrigger(Items.BUCKET))
                .unlockedBy("pearl", LazyTrigger(Tags.Items.ENDER_PEARLS))
                .define('m', Tags.Items.OBSIDIAN)
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('e', Tags.Items.ENDER_PEARLS)
                .save(makeConditional(ID("fluid_tanks/void_tank"), consumer, TechCraftingConditions.VOID_TANK));

    }

    private static void GenerateTankRecipe(@Nonnull Consumer<FinishedRecipe> consumer, @Nonnull TagKey<Item> ingredient, @Nonnull Supplier<? extends ItemLike> result)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get())
                .pattern("mgm")
                .pattern("g g")
                .pattern("mgm")
                .unlockedBy("material", LazyTrigger(ingredient))
                .unlockedBy("glass", LazyTrigger(Tags.Items.GLASS_COLORLESS))
                .unlockedBy("bucket", LazyTrigger(Items.BUCKET))
                .define('m', ingredient)
                .define('g', Tags.Items.GLASS_COLORLESS)
                .save(makeConditional(ItemID("fluid_tanks/", result), consumer, TechCraftingConditions.FLUID_TANK));
    }

    private static String ItemPath(ItemLike item) { return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath(); }
    private static String ItemPath(Supplier<? extends ItemLike> item) { return ItemPath(item.get()); }
    private static ResourceLocation ItemID(String prefix, ItemLike item) { return ID(prefix + ItemPath(item)); }
    private static ResourceLocation ItemID(Supplier<? extends ItemLike> item) { return ID(ItemPath(item)); }
    private static ResourceLocation ItemID(String prefix, Supplier<? extends ItemLike> item) { return ID(prefix + ItemPath(item)); }
    private static ResourceLocation ID(String path) { return new ResourceLocation(LCTech.MODID, path); }


    private static ItemLike TradingCore() { return io.github.lightman314.lightmanscurrency.common.core.ModItems.TRADING_CORE.get(); }
    private static Ingredient SmithingTemplate() { return Ingredient.of(io.github.lightman314.lightmanscurrency.common.core.ModItems.UPGRADE_SMITHING_TEMPLATE.get()); }

    private static CriterionTriggerInstance TraderKnowledge() { return LazyTrigger(LCTags.Items.TRADER); }
    private static CriterionTriggerInstance TerminalKnowledge() { return LazyTrigger(LCTags.Items.NETWORK_TERMINAL); }

    private static CriterionTriggerInstance LazyTrigger(ItemLike item) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item).build()); }
    private static CriterionTriggerInstance LazyTrigger(List<? extends ItemLike> items) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items.toArray(new ItemLike[0])).build()); }
    private static CriterionTriggerInstance LazyTrigger(Supplier<? extends ItemLike> item) { return LazyTrigger(item.get()); }
    private static CriterionTriggerInstance LazyTrigger(TagKey<Item> tag) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build()); }

}
