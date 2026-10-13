package com.closky.rpgmod.client;

import org.jetbrains.annotations.ApiStatus;

/**
 * Копия прогресса игрока на клиенте (для HUD и экрана прокачки).
 * Обновляется пакетом {@code SyncPlayerDataPacket}.
 */
public final class ClientPlayerData {
    private static int level = 1;
    private static int statPoints;
    private static int spentStrength;
    private static int spentVitality;
    private static int spentIntellect;
    private static float mana;
    private static float maxMana = 50.0F;

    private ClientPlayerData() {}

    @ApiStatus.Internal
    public static void set(int level, int statPoints, int spentStrength, int spentVitality,
                           int spentIntellect, float mana, float maxMana) {
        ClientPlayerData.level = level;
        ClientPlayerData.statPoints = statPoints;
        ClientPlayerData.spentStrength = spentStrength;
        ClientPlayerData.spentVitality = spentVitality;
        ClientPlayerData.spentIntellect = spentIntellect;
        ClientPlayerData.mana = mana;
        ClientPlayerData.maxMana = maxMana;
    }

    public static int getLevel() {
        return level;
    }

    public static int getStatPoints() {
        return statPoints;
    }

    public static int getSpentStrength() {
        return spentStrength;
    }

    public static int getSpentVitality() {
        return spentVitality;
    }

    public static int getSpentIntellect() {
        return spentIntellect;
    }

    public static float getMana() {
        return mana;
    }

    public static float getMaxMana() {
        return maxMana;
    }

    public static void setMana(float value) {
        mana = value;
    }
}
