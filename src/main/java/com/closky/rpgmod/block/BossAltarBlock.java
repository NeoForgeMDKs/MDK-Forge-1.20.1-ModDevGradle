package com.closky.rpgmod.block;

import com.closky.rpgmod.entity.DungeonWarden;
import com.closky.rpgmod.registry.ModEntities;
import com.closky.rpgmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Алтарь босса. Правый клик ключом подземелья — призывает Хранителя подземелья.
 * Так «боссы в больших данжах» появляются не сами по себе, а по воле игрока.
 */
public class BossAltarBlock extends Block {
    public BossAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.DUNGEON_KEY.get())) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("msg.rpgmod.altar_needs_key")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level instanceof ServerLevel serverLevel) {
            DungeonWarden boss = ModEntities.DUNGEON_WARDEN.get().create(serverLevel);
            if (boss != null) {
                boss.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                boss.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
                serverLevel.addFreshEntity(boss);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 1.5D,
                        pos.getZ() + 0.5D, 60, 0.7D, 0.5D, 0.7D, 0.05D);
                serverLevel.playSound(null, pos, SoundEvents.WARDEN_ROAR, SoundSource.BLOCKS, 1.2F, 0.7F);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
