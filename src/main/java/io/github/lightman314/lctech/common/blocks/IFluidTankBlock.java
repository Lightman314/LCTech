package io.github.lightman314.lctech.common.blocks;

import io.github.lightman314.lctech.client.resourcepacks.data.fluid_rendering.FluidRenderData;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.FluidTankBlockEntity;
import io.github.lightman314.lctech.common.blockentities.fluid_tank.TankStackState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface IFluidTankBlock {

	EnumProperty<TankStackState> TANK_STATE = EnumProperty.create("tank_state", TankStackState.class);

	FluidRenderData getItemRenderData();

	FluidRenderData getRenderData(BlockState state, boolean isLighterThanAir, FluidTankBlockEntity tank, FluidTankBlockEntity nextTank);

	default TankStackState getTankState(BlockState state) {
		if(state.hasProperty(TANK_STATE))
			return state.getValue(TANK_STATE);
		return TankStackState.SOLO;
	}

}
