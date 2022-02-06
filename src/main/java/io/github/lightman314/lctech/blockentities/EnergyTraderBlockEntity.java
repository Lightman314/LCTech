package io.github.lightman314.lctech.blockentities;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lctech.core.ModBlockEntities;
import io.github.lightman314.lctech.trader.IEnergyTrader;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyTraderBlockEntity extends TraderBlockEntity implements IEnergyTrader{

	public EnergyTraderBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.ENERGY_TRADER, pos, state);
	}
	
	protected EnergyTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public List<Settings> getAdditionalSettings() {
		return Lists.newArrayList();
	}

	@Override
	public int getTradeStock(int tradeIndex) {
		return 0;
	}

	@Override
	public MenuProvider getCashRegisterTradeMenuProvider(CashRegisterBlockEntity cashRegister) {
		return null;
	}

	@Override
	public MenuProvider getStorageMenuProvider() {
		return null;
	}

	@Override
	public MenuProvider getTradeMenuProvider() {
		return null;
	}

	@Override
	protected void onVersionUpdate(int oldVersion) { }

}
