package io.github.lightman314.lctech.common.blocks;

import io.github.lightman314.lctech.TechText;
import io.github.lightman314.lightmanscurrency.common.blocks.EasyBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class VoidTankBlock extends EasyBlock {

    public static final IFluidHandler VOID_HANDLER = new VoidFluidHandler();

    public VoidTankBlock(Properties properties) { super(properties); }

    @Nonnull
    @Override
    protected ItemInteractionResult useItemOn(@Nonnull ItemStack heldItem, @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if(hit.getDirection().getAxis().isVertical())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, VOID_HANDLER, Integer.MAX_VALUE, player, true);
        if(result.isSuccess())
        {
            if(heldItem.getCount() > 1)
            {
                heldItem.shrink(1);
                player.setItemInHand(hand, heldItem);
                ItemHandlerHelper.giveItemToPlayer(player, result.getResult());
            }
            else
            {
                player.setItemInHand(hand, result.getResult());
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(TechText.TOOLTIP_VOID_TANK.getWithStyle(ChatFormatting.GRAY));
    }

    private static class VoidFluidHandler implements IFluidHandler
    {
        @Override
        public int getTanks() { return 1; }
        @Override
        @Nonnull
        public FluidStack getFluidInTank(int i) { return FluidStack.EMPTY; }
        @Override
        public int getTankCapacity(int i) { return Integer.MAX_VALUE / 2; }
        @Override
        public boolean isFluidValid(int i, @Nonnull FluidStack fluidStack) { return true; }
        @Override
        public int fill(FluidStack fluidStack, @Nonnull FluidAction fluidAction) { return fluidStack.getAmount(); }
        @Override
        @Nonnull
        public FluidStack drain(@Nonnull FluidStack fluidStack, @Nonnull FluidAction fluidAction) { return FluidStack.EMPTY; }
        @Override
        @Nonnull
        public FluidStack drain(int i, @Nonnull FluidAction fluidAction) { return FluidStack.EMPTY; }
    }

}
