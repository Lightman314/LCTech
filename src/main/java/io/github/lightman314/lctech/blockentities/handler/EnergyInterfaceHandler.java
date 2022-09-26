package io.github.lightman314.lctech.blockentities.handler;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.blockentities.EnergyTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyInterfaceHandler extends ConfigurableSidedHandler<IEnergyStorage> {

	public static final ResourceLocation TYPE = new ResourceLocation(LCTech.MODID, "energy_interface");
	
	protected final EnergyTraderInterfaceBlockEntity blockEntity;
	
	public final IEnergyStorage tradeHandler;
	
	private final Map<Direction,Handler> handlers = new HashMap<Direction,Handler>();
	
	public EnergyInterfaceHandler(EnergyTraderInterfaceBlockEntity blockEntity) { this.blockEntity = blockEntity; this.tradeHandler = new Handler(this, null); }
	
	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public String getTag() { return "EnergyData"; }
	
	@Override
	public IEnergyStorage getHandler(Direction side) {
		if(side == null)
			return null;
		if(this.inputSides.get(side) || this.outputSides.get(side))
		{
			if(!this.handlers.containsKey(side))
				this.handlers.put(side, new Handler(this, side));
			return this.handlers.get(side);
		}
		return null;
	}
	
	private static class Handler implements IEnergyStorage
	{
		
		final EnergyInterfaceHandler handler;
		final Direction side;
		
		Handler(EnergyInterfaceHandler handler, Direction side) { this.handler = handler; this.side = side; }

		@Override
		public boolean canExtract() { return this.side == null ? true : this.handler.outputSides.get(this.side); }

		@Override
		public boolean canReceive() { return this.side == null ? true : this.handler.inputSides.get(this.side); }
		
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if(!this.canReceive())
				return 0;
			int receivableAmount = Math.min(this.getMaxEnergyStored() - this.getEnergyStored(), maxReceive);
			if(receivableAmount <= 0)
				return 0;
			if(!simulate)
				this.handler.blockEntity.addStoredEnergy(receivableAmount);
			return receivableAmount;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if(!this.canExtract())
				return 0;
			int extractableAmount = Math.min(this.getEnergyStored(), maxExtract);
			if(extractableAmount <= 0)
				return 0;
			if(!simulate)
				this.handler.blockEntity.drainStoredEnergy(extractableAmount);
			return extractableAmount;
		}

		@Override
		public int getEnergyStored() { return this.handler.blockEntity.getStoredEnergy(); }

		@Override
		public int getMaxEnergyStored() { return this.handler.blockEntity.getMaxEnergy(); }
		
	}

}
