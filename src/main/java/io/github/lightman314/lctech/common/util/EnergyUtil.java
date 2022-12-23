package io.github.lightman314.lctech.common.util;

import java.text.DecimalFormat;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class EnergyUtil {

	public static final String ENERGY_UNIT = "FE";
	
	public static LazyOptional<IEnergyStorage> getEnergyHandler(ItemStack itemStack)
	{
		return itemStack.getCapability(ForgeCapabilities.ENERGY);
	}
	
	public static String formatEnergyAmount(int amount) { return formatEnergyAmount(amount, true); }
	
	public static String formatEnergyAmount(int amount, boolean addUnit)
	{
		String amountText = new DecimalFormat().format(amount);
		if(addUnit)
			amountText += ENERGY_UNIT;
		return amountText;
	}
	
	/**
     * Fill a container from the given energySource.
     *
     * @param container   The container to be filled. Will not be modified.
     *                    Separate handling must be done to reduce the stack size, stow containers, etc, on success.
     * @param energySource The energy handler to be drained.
     * @param maxAmount   The largest amount of energy that should be transferred.
     * @param doFill      true if the container should actually be filled, false if it should be simulated.
     * @return a {@link EnergyActionResult} holding the filled container if successful.
     */
    @Nonnull
    public static EnergyActionResult tryFillContainer(@Nonnull ItemStack container, IEnergyStorage energySource, int maxAmount, boolean doFill)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        return getEnergyHandler(containerCopy)
                .map(containerEnergyHandler -> {
                    int simulatedTransfer = tryEnergyTransfer(containerEnergyHandler, energySource, maxAmount, false);
                    if (simulatedTransfer > 0)
                    {
                        if (doFill)
                        {
                        	tryEnergyTransfer(containerEnergyHandler, energySource, maxAmount, true);
                        }
                        else
                        {
                        	containerEnergyHandler.receiveEnergy(simulatedTransfer, !doFill);
                        }
                        return new EnergyActionResult(containerCopy);
                    }
                    return EnergyActionResult.FAILURE;
                })
                .orElse(EnergyActionResult.FAILURE);
    }
    
    /**
     * Takes a filled container and tries to empty it into the given energy storage.
     *
     * @param container        The filled container. Will not be modified.
     *                         Separate handling must be done to reduce the stack size, stow containers, etc, on success.
     * @param energyDestination The energy storage to be filled by the container.
     * @param maxAmount        The largest amount of energy that should be transferred.
     * @param doDrain          true if the container should actually be drained, false if it should be simulated.
     * @return a {@link EnergyActionResult} holding the empty container if the energy handler was filled.
     *         NOTE If the container is consumable, the empty container will be null on success.
     */
    @Nonnull
    public static EnergyActionResult tryEmptyContainer(@Nonnull ItemStack container, IEnergyStorage energyDestination, int maxAmount, boolean doDrain)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        return getEnergyHandler(containerCopy)
                .map(containerEnergyHandler -> {

                    // We are acting on a COPY of the stack, so performing changes is acceptable even if we are simulating.
                    int transfer = tryEnergyTransfer(energyDestination, containerEnergyHandler, maxAmount, true);
                    if (transfer <= 0)
                        return EnergyActionResult.FAILURE;
                    
                    return new EnergyActionResult(containerCopy);
                })
                .orElse(EnergyActionResult.FAILURE);
    }
    
    /**
     * Fill a destination energy handler from a source energy handler with a max amount.
     * To specify a fluid to transfer instead of max amount, use {@link #tryEnergyTransfer(IFluidHandler, IFluidHandler, FluidStack, boolean)}
     * To transfer as much as possible, use {@link Integer#MAX_VALUE} for maxAmount.
     *
     * @param energyDestination The energy handler to be filled.
     * @param energySource      The energy handler to be drained.
     * @param maxAmount        The largest amount of energy that should be transferred.
     * @param doTransfer       True if the transfer should actually be done, false if it should be simulated.
     * @return the amount of energy that was transferred from the source to the destination. 0 on failure.
     */
    private static int tryEnergyTransfer(IEnergyStorage fluidDestination, IEnergyStorage fluidSource, int maxAmount, boolean doTransfer)
    {
        int drainable = fluidSource.extractEnergy(maxAmount, true);
        if (drainable > 0)
        {
            return tryEnergyTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer);
        }
        return 0;
    }
    
    /**
     * Internal method for filling a destination energy handler from a source energy handler using a specific fluid.
     * Assumes that "amount" can be drained from "energySource".
     */
    @Nonnull
    private static int tryEnergyTransfer_Internal(IEnergyStorage energyDestination, IEnergyStorage energySource, int amount, boolean doTransfer)
    {
        int fillableAmount = energyDestination.receiveEnergy(amount, true);
        if (fillableAmount > 0)
        {
            if (doTransfer)
            {
                int drained = energySource.extractEnergy(fillableAmount, false);
                if (drained > 0)
                {
                    return energyDestination.receiveEnergy(drained, false);
                }
            }
            else
            {
                return fillableAmount;
            }
        }
        return 0;
    }
    
	
	public static class EnergyActionResult
	{
		
		private static final EnergyActionResult FAILURE = new EnergyActionResult(false, ItemStack.EMPTY);
		
		private final ItemStack result;
		public final ItemStack getResult() { return this.result; }
		private final boolean success;
		public final boolean success() { return this.success; }
		
		public EnergyActionResult(ItemStack result) { this(true, result); }
		private EnergyActionResult(boolean success, ItemStack result)
		{
			this.success = success;
			this.result = result;
		}
	}
	
}
