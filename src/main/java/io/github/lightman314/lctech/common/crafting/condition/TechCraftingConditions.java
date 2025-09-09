package io.github.lightman314.lctech.common.crafting.condition;

import io.github.lightman314.lctech.TechConfig;
import io.github.lightman314.lightmanscurrency.api.config.conditions.ConfigCraftingCondition;
import net.neoforged.neoforge.common.conditions.ICondition;

public class TechCraftingConditions {

    public static final ICondition FLUID_TRADER = ConfigCraftingCondition.of(TechConfig.COMMON.canCraftFluidTraders);
    public static final ICondition FLUID_TANK = ConfigCraftingCondition.of(TechConfig.COMMON.canCraftFluidTanks);
    public static final ICondition VOID_TANK = ConfigCraftingCondition.of(TechConfig.COMMON.canCraftVoidTanks);
    public static final ICondition ENERGY_TRADER = ConfigCraftingCondition.of(TechConfig.COMMON.canCraftEnergyTraders);
    public static final ICondition BATTERIES = ConfigCraftingCondition.of(TechConfig.COMMON.canCraftBatteries);
	
}
