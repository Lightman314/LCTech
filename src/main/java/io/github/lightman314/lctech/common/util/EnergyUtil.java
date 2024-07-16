package io.github.lightman314.lctech.common.util;

import java.text.DecimalFormat;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyUtil {

	public static final String ENERGY_UNIT = "FE";
	
	public static Optional<IEnergyStorage> getEnergyHandler(ItemStack itemStack)
	{
		return Optional.ofNullable(itemStack.getCapability(Capabilities.EnergyStorage.ITEM));
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
     *                    Separate handling must be done to reduce the stack size, stow containers, etc., on success.
     * @param energySource The energy handler to be drained.
     * @param maxAmount   The largest amount of energy that should be transferred.
     * @param doFill      true if the container should actually be filled, false if it should be simulated.
     * @return a {@link EnergyActionResult} holding the filled container if successful.
     */
    @Nonnull
    public static EnergyActionResult tryFillContainer(@Nonnull ItemStack container, IEnergyStorage energySource, int maxAmount, boolean doFill)
    {
        ItemStack containerCopy = container.copyWithCount(1); // do not modify the input
        return getEnergyHandler(containerCopy).map(containerEnergyHandler -> {
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
        }).orElse(EnergyActionResult.FAILURE);
    }
    
    /**
     * Takes a filled container and tries to empty it into the given energy storage.
     *
     * @param container        The filled container. Will not be modified.
     *                         Separate handling must be done to reduce the stack size, stow containers, etc., on success.
     * @param energyDestination The energy storage to be filled by the container.
     * @param maxAmount        The largest amount of energy that should be transferred.
     * @param doDrain          true if the container should actually be drained, false if it should be simulated.
     * @return a {@link EnergyActionResult} holding the empty container if the energy handler was filled.
     *         NOTE If the container is consumable, the empty container will be null on success.
     */
    @Nonnull
    public static EnergyActionResult tryEmptyContainer(@Nonnull ItemStack container, IEnergyStorage energyDestination, int maxAmount, boolean doDrain)
    {
        ItemStack containerCopy = container.copyWithCount(1); // do not modify the input
        return getEnergyHandler(containerCopy).map(containerEnergyHandler -> {
            // We are acting on a COPY of the stack, so performing changes is acceptable even if we are simulating.
            int transfer = tryEnergyTransfer(energyDestination, containerEnergyHandler, maxAmount, true);
            if (transfer <= 0)
                return EnergyActionResult.FAILURE;

            return new EnergyActionResult(containerCopy);
        }).orElse(EnergyActionResult.FAILURE);
    }
    
    /**
     * Fill a destination energy handler from a source energy handler with a max amount.
     * To transfer as much as possible, use {@link Integer#MAX_VALUE} for maxAmount.
     *
     * @param energyDestination The energy handler to be filled.
     * @param energySource      The energy handler to be drained.
     * @param maxAmount        The largest amount of energy that should be transferred.
     * @param doTransfer       True if the transfer should actually be done, false if it should be simulated.
     * @return the amount of energy that was transferred from the source to the destination. 0 on failure.
     */
    private static int tryEnergyTransfer(IEnergyStorage energyDestination, IEnergyStorage energySource, int maxAmount, boolean doTransfer)
    {
        int drainable = energyDestination.extractEnergy(maxAmount, true);
        if (drainable > 0)
            return tryEnergyTransfer_Internal(energyDestination, energySource, drainable, doTransfer);
        return 0;
    }
    
    /**
     * Internal method for filling a destination energy handler from a source energy handler using a specific fluid.
     * Assumes that "amount" can be drained from "energySource".
     */
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
