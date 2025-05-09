package io.github.lightman314.lctech.common.blockentities.handler;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lctech.LCTech;
import io.github.lightman314.lctech.common.blockentities.FluidTraderInterfaceBlockEntity;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage;
import io.github.lightman314.lctech.common.traders.fluid.TraderFluidStorage.FluidEntry;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public class FluidInterfaceHandler extends ConfigurableSidedHandler<IFluidHandler> {
	
	public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(LCTech.MODID, "fluid_interface");
	
	protected final FluidTraderInterfaceBlockEntity blockEntity;
	
	protected final TraderFluidStorage getFluidBuffer() { return this.blockEntity.getFluidBuffer(); }
	
	private final Map<Direction,Handler> handlers = new HashMap<>();

	public FluidInterfaceHandler(FluidTraderInterfaceBlockEntity blockEntity) { this.blockEntity = blockEntity; }
	
	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public String getTag() { return "FluidData"; }
	
	@Override
	public IFluidHandler getHandler(Direction side) {
		if(!this.handlers.containsKey(side))
			this.handlers.put(side, new Handler(this, side));
		return this.handlers.get(side);
	}
	
	private static class Handler implements IFluidHandler {
		
		final FluidInterfaceHandler handler;
		final Direction side;
		
		Handler(FluidInterfaceHandler handler, Direction side) { this.handler = handler; this.side = side; }
		
		protected final boolean allowInputs() { return this.handler.allowInputSide(this.side); }
		protected final boolean allowOutputs() { return this.handler.allowOutputSide(this.side); }

		@Override
		public int getTanks() { return this.handler.getFluidBuffer().getTanks(); }

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank) { return this.handler.getFluidBuffer().getFluidInTank(tank); }

		@Override
		public int getTankCapacity(int tank) { return this.handler.getFluidBuffer().getTankCapacity(tank); }

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return this.handler.getFluidBuffer().isFluidValid(tank, stack); }

		@Override
		public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
			if(this.allowInputs() && this.handler.blockEntity.allowInput(resource))
			{
				int result = this.handler.getFluidBuffer().fill(resource, action);
				if(action.execute())
					this.handler.blockEntity.setFluidBufferDirty();
				return result;
			}
			return 0;
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, @Nonnull FluidAction action) {
			if(resource.isEmpty())
				return FluidStack.EMPTY;
			if(this.allowOutputs() && this.handler.blockEntity.allowOutput(resource))
			{
				TraderFluidStorage storage = this.handler.getFluidBuffer();
				int drainableAmount = Math.min(storage.getActualFluidCount(resource), resource.getAmount());
				FluidStack result = resource.copy();
				result.setAmount(drainableAmount);
				if(action.execute())
				{
					//Drain manually
					storage.drain(result);
					this.handler.blockEntity.setFluidBufferDirty();
				}
				return result;
			}
			return FluidStack.EMPTY;
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, @Nonnull FluidAction action) {
			if(!this.allowOutputs())
				return FluidStack.EMPTY;
			for(FluidEntry entry : this.handler.getFluidBuffer().getContents())
			{
				if(this.handler.blockEntity.allowOutput(entry.filter) && entry.getStoredAmount() > 0)
				{
					FluidStack drainFluid = entry.filter.copy();
					drainFluid.setAmount(maxDrain);
					return this.drain(drainFluid, action);
				}
			}
			return FluidStack.EMPTY;
		}
		
	}

}
