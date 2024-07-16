package io.github.lightman314.lctech;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TechTags {

    public static class Blocks {

    }

    public static class Items {
        //Fluid Trader Tag
        public static final TagKey<Item> TRADER_FLUID = lcTag("trader_fluid");
        public static final TagKey<Item> TRADER_NETWORK_FLUID = lcTag("traders/network/fluid");
        //Energy Trader Tag
        public static final TagKey<Item> TRADER_ENERGY = lcTag("trader_energy");
        public static final TagKey<Item> TRADER_NETWORK_ENERGY = lcTag("traders/network/energy");

        //Misc LC Tags
        public static final TagKey<Item> FLUID_TANK = techTag("fluid_tanks");
        public static final TagKey<Item> BATTERIES = techTag("batteries");


        private static TagKey<Item> lcTag(String id) { return ItemTags.create(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, id)); }
        private static TagKey<Item> techTag(String id) { return ItemTags.create(ResourceLocation.fromNamespaceAndPath(LCTech.MODID, id)); }
    }

}
