package io.github.lightman314.lctech.datagen.common.loot;

import io.github.lightman314.lctech.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.packs.BlockDropLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class TechBlockLootProvider extends BlockDropLoot {

    public TechBlockLootProvider() { super(); }
    @Override
    protected void generateLootTables() {
        this.lazyBlock(ModBlocks.VOID_TANK);
    }

    protected void lazyBlock(Supplier<? extends Block> block) { this.lazyBlock(block.get()); }
    protected void lazyBlock(Block block)
    {
        this.define(this.getBlockTable(block),LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(block))));
    }

}