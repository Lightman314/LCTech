package io.github.lightman314.lctech.datagen.common.crafting;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class EasyRecipeProvider extends RecipeProvider {

    public EasyRecipeProvider(DataGenerator generator) { super(generator); }

    protected static Consumer<FinishedRecipe> makeConditional(@Nonnull ResourceLocation id, @Nonnull Consumer<FinishedRecipe> consumer, @Nonnull ICondition... conditions)
    {
        ConditionalRecipe.Builder builder = ConditionalRecipe.builder();
        for(ICondition condition : conditions)
            builder.addCondition(condition);
        return finishedRecipe ->
                builder.addRecipe(finishedRecipe)
                        .generateAdvancement(withPrefix(id,"recipes/misc/"))
                        .build(consumer, id);
    }

    protected static ResourceLocation withPrefix(@Nonnull ResourceLocation id, @Nonnull String prefix)
    {
        return new ResourceLocation(id.getNamespace(), prefix + id.getPath());
    }
}