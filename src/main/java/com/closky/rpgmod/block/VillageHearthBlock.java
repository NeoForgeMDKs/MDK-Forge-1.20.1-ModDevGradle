package com.closky.rpgmod.block;

import com.closky.rpgmod.block.entity.VillageHearthBlockEntity;
import com.closky.rpgmod.village.VillageData;
import com.closky.rpgmod.village.VillageSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Очаг деревни — «сердце» поселения. Ставится в центре деревни (шаблон village/center).
 *
 * <p>Пока чанк прогружен, очаг сообщает серверу, где находится деревня, и копит «очки роста».
 * Благодаря этому деревня развивается сама (см. {@code VillageDevelopmentHandler}).</p>
 */
public class VillageHearthBlock extends BaseEntityBlock {
    public VillageHearthBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VillageHearthBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof VillageHearthBlockEntity hearth) {
                hearth.serverTick((ServerLevel) lvl, pos);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel) {
            VillageData data = VillageSavedData.get(serverLevel).getOrCreate(pos);
            player.displayClientMessage(Component.translatable("msg.rpgmod.village_info",
                    Component.literal(String.valueOf(data.getType().getDisplayName())),
                    Component.literal(String.valueOf(data.getPopulation())),
                    Component.literal(String.valueOf(data.getGrowthPoints()))).withStyle(ChatFormatting.GREEN), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
