package com.closky.rpgmod.client;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.config.RpgClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Табличка «Lv.N  HP/MaxHP» над враждебными мобами (требование 5г).
 *
 * <p>Данные об уровне приходят с сервера пакетом {@code EnemySyncPacket} и лежат в
 * {@link ClientEnemyData}. Отключается в настройках мода (Config -> hud ->
 * showEnemyLevel / showEnemyHealth) — то есть кнопкой «Config» в списке модов.</p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = RpgMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EnemyNameplateRenderer {
    /** Дальше 48 блоков таблички не рисуем — иначе экран превращается в мусор. */
    private static final double MAX_DISTANCE_SQR = 48.0D * 48.0D;

    private EnemyNameplateRenderer() {}

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.options.hideGui) return;
        if (entity == minecraft.player || entity.isInvisible()) return;
        if (!(entity instanceof Enemy)) return;
        if (minecraft.player.distanceToSqr(entity) > MAX_DISTANCE_SQR) return;

        boolean showLevel = RpgClientConfig.SHOW_ENEMY_LEVEL.get();
        boolean showHealth = RpgClientConfig.SHOW_ENEMY_HEALTH.get();
        if (!showLevel && !showHealth) return;

        int level = ClientEnemyData.getLevel(entity.getId());
        if (level <= 0) return;

        Component label = buildLabel(entity, level, ClientPlayerData.getLevel(), showLevel, showHealth);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        // над головой моба
        poseStack.translate(0.0D, entity.getBbHeight() + 0.7D, 0.0D);
        // всегда лицом к камере
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        Font font = minecraft.font;
        float x = -font.width(label) / 2.0F;
        int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

        font.drawInBatch(label, x, 0.0F, 0xFFFFFFFF, true, matrix, event.getMultiBufferSource(),
                Font.DisplayMode.NORMAL, background, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
    }

    private static Component buildLabel(LivingEntity entity, int level, int playerLevel,
                                       boolean showLevel, boolean showHealth) {
        MutableComponent label = Component.empty();
        if (showLevel) {
            label.append(Component.literal("Lv." + level).withStyle(levelColor(level, playerLevel)));
        }
        if (showHealth) {
            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            if (maxHealth <= 0.0F) {
                maxHealth = ClientEnemyData.getMaxHealth(entity.getId());
            }
            if (showLevel) {
                label.append(Component.literal("  "));
            }
            label.append(Component.literal(Math.round(health) + "/" + Math.round(maxHealth))
                    .withStyle(healthColor(maxHealth <= 0.0F ? 0.0F : health / maxHealth)));
        }
        return label;
    }

    /** Цвет уровня: зелёный — по силам игроку, красный — смертельно опасен. */
    private static ChatFormatting levelColor(int level, int playerLevel) {
        int diff = level - playerLevel;
        if (diff >= 5) return ChatFormatting.DARK_RED;
        if (diff >= 1) return ChatFormatting.RED;
        if (diff >= -2) return ChatFormatting.YELLOW;
        return ChatFormatting.GREEN;
    }

    private static ChatFormatting healthColor(float ratio) {
        if (ratio > 0.6F) return ChatFormatting.GREEN;
        if (ratio > 0.3F) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }
}
