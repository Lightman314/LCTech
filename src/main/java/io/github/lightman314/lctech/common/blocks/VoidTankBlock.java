package io.github.lightman314.lctech.common.blocks;

import io.github.lightman314.lctech.common.blockentities.VoidTankBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class VoidTankBlock extends Block implements EntityBlock {

    public VoidTankBlock(Properties properties) { super(properties); }

    @Nonnull
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, BlockHitResult hitResult) {
        if(hitResult.getDirection().getAxis().isVertical())
            return InteractionResult.PASS;
        //Try to fill the tank
        ItemStack heldItem = player.getItemInHand(hand);
        FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, VoidTankBlockEntity.VOID_HANDLER, Integer.MAX_VALUE, player, true);
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
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(EasyText.translatable("block.lctech.void_tank.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new VoidTankBlockEntity(pos,state); }
}