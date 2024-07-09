package io.github.lightman314.lctech.common.blockentities.fluid_tank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class FluidTankFluidHandler implements IFluidHandler {

    private final FluidTankBlockEntity fluidTank;
    public FluidTankFluidHandler(FluidTankBlockEntity tank) { this.fluidTank = tank; }
    public final FluidStack getTankContents() {
        List<FluidTankBlockEntity> tanks = this.fluidTank.getTankStack();
        if(tanks.isEmpty())
            return FluidStack.EMPTY;
        FluidStack contents = tanks.get(0).getTankContents().copy();
        if(contents.isEmpty())
            return FluidStack.EMPTY;
        for(int i = 1; i < tanks.size(); ++i)
        {
            FluidStack tc = tanks.get(i).getTankContents().copy();
            if(tc.isFluidEqual(contents))
                contents.grow(tc.getAmount());
            if(!tc.isFluidEqual(contents) && !tc.isEmpty())
                this.fluidTank.refactorTankStack();
        }
        return contents;
    }

    private void growTankContents(int amount) {
        FluidStack contents = this.getTankContents();
        contents.grow(amount);
        this.setTankContents(contents);
    }

    private void shrinkTankContents(int amount) {
        FluidStack contents = this.getTankContents();
        contents.shrink(amount);
        this.setTankContents(contents);
    }

    public final void setTankContents(FluidStack newContents) {
        List<FluidTankBlockEntity> tanks = this.fluidTank.getTankStack(newContents);
        if(tanks.isEmpty())
        {
            LightmansCurrency.LogError("Somehow a Fluid Tank stack has no tanks in it!");
            return;
        }
        FluidStack fill = newContents.copy();
        for(FluidTankBlockEntity tank : tanks)
        {
            if(fill.isEmpty())
                tank.setTankContents(FluidStack.EMPTY);
            else
            {
                FluidStack thisTank = fill.copy();
                thisTank.setAmount(MathUtil.clamp(fill.getAmount(), 0, tank.getTankCapacity()));
                if(thisTank.getAmount() == fill.getAmount())
                    fill = FluidStack.EMPTY;
                else
                    fill.shrink(thisTank.getAmount());
                tank.setTankContents(thisTank);
            }
        }
        if(!fill.isEmpty())
        {
            //Forcibly overflow the last tank to keep us from losing fluid
            FluidTankBlockEntity lastTank = tanks.get(tanks.size() - 1);
            FluidStack contents = lastTank.getTankContents();
            contents.grow(fill.getAmount());
            lastTank.setTankContents(contents);
        }
    }

    private int getTankCapacity() {
        int capacity = 0;
        List<FluidTankBlockEntity> tanks = this.fluidTank.getTankStack();
        for(FluidTankBlockEntity tank : tanks)
            capacity += tank.getTankCapacity();
        return capacity;
    }

    private int getTankSpace() { return Math.max(0, this.getTankCapacity() - this.getTankContents().getAmount()); }


    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return tank == 0 ? this.getTankContents() : FluidStack.EMPTY; }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? this.getTankCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        if(tank != 0)
            return false;
        FluidStack contents = this.getTankContents();
        return contents.isEmpty() || contents.isFluidEqual(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if(isFluidValid(0, resource))
        {
            int fillAmount = MathUtil.clamp(resource.getAmount(), 0, this.getTankSpace());
            if(fillAmount > 0 && action.execute())
            {
                FluidStack contents = this.getTankContents();
                if(contents.isEmpty())
                {
                    FluidStack fluidToSet = resource.copy();
                    if(!fluidToSet.isEmpty())
                        fluidToSet.setAmount(fillAmount);
                    this.setTankContents(fluidToSet);
                }
                else
                    this.growTankContents(fillAmount);
            }
            return fillAmount;
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack contents = this.getTankContents();
        if(contents.isEmpty() || !contents.isFluidEqual(resource))
            return FluidStack.EMPTY;
        //Tank is not empty, and the resource is equal to the tank contents
        int drainAmount = MathUtil.clamp(resource.getAmount(), 0, contents.getAmount());
        FluidStack resultStack = contents.copy();
        resultStack.setAmount(drainAmount);
        if(action.execute())
        {
            //Drain the tank
            this.shrinkTankContents(drainAmount);
            if(contents.isEmpty())
                this.setTankContents(FluidStack.EMPTY);
        }
        return resultStack;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack contents = this.getTankContents();
        if(contents.isEmpty())
            return FluidStack.EMPTY;
        FluidStack drainStack = contents.copy();
        drainStack.setAmount(maxDrain);
        return drain(drainStack, action);
    }

}