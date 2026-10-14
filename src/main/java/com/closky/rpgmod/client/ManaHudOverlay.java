package com.closky.rpgmod.client;

import com.closky.rpgmod.config.RpgClientConfig;
import com.closky.rpgmod.player.PlayerClass;
import com.closky.rpgmod.player.SetBonusHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * HUD мода: полоска маны прямо над полосой опыта + уровень персонажа и текущий класс.
 *
 * <p>Отключается в настройках мода (Mods -> RPG Overhaul -> Config -> hud).</p>
 */
@OnlyIn(Dist.CLIENT)
public class ManaHudOverlay implements IGuiOverlay {
    public static final ManaHudOverlay INSTANCE = new ManaHudOverlay();

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private ManaHudOverlay() {}

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        if (minecraft.player.isSpectator()) return;

        Font font = minecraft.font;

        if (RpgClientConfig.SHOW_MANA_BAR.get()) {
            renderManaBar(graphics, font, screenWidth, screenHeight);
        }
        renderProgression(graphics, font, minecraft);
    }

    private void renderManaBar(GuiGraphics graphics, Font font, int screenWidth, int screenHeight) {
        float maxMana = Math.max(1.0F, ClientPlayerData.getMaxMana());
        float mana = Mth.clamp(ClientPlayerData.getMana(), 0.0F, maxMana);
        float ratio = mana / maxMana;

        int barX = screenWidth / 2 - BAR_WIDTH / 2;
        int barY = screenHeight - 40; // чуть выше полосы опыта

        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xB0000000);
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF252540);
        int filled = Math.round(BAR_WIDTH * ratio);
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + BAR_HEIGHT, 0xFF4C7BFF);
            graphics.fill(barX, barY, barX + filled, barY + 1, 0xFF9FC0FF); // блик сверху
        }

        Component text = Component.translatable("hud.rpgmod.mana",
                Component.literal(String.valueOf((int) mana)),
                Component.literal(String.valueOf((int) maxMana)));
        graphics.drawString(font, text, screenWidth / 2 - font.width(text) / 2, barY - 11, 0xFFD0E0FF, true);
    }

    private void renderProgression(GuiGraphics graphics, Font font, Minecraft minecraft) {
        Component levelText = Component.translatable("hud.rpgmod.level",
                Component.literal(String.valueOf(ClientPlayerData.getLevel())).withStyle(ChatFormatting.GOLD));
        graphics.drawString(font, levelText, 6, 6, 0xFFFFFFFF, true);

        int y = 16;
        if (RpgClientConfig.SHOW_PLAYER_CLASS.get()) {
            PlayerClass playerClass = SetBonusHandler.detectFullSet(minecraft.player);
            if (playerClass != PlayerClass.NONE) {
                Component classText = Component.translatable("hud.rpgmod.class",
                        Component.translatable(playerClass.getTranslationKey()).withStyle(playerClass.getColor()));
                graphics.drawString(font, classText, 6, y, 0xFFFFFFFF, true);
                y += 10;
            }
        }

        if (ClientPlayerData.getStatPoints() > 0) {
            Component points = Component.translatable("hud.rpgmod.stat_points",
                    Component.literal(String.valueOf(ClientPlayerData.getStatPoints()))).withStyle(ChatFormatting.YELLOW);
            graphics.drawString(font, points, 6, y, 0xFFFFD700, true);
        }
    }
}
