package com.closky.rpgmod.client;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Клиентский кэш уровней врагов.
 *
 * <p>Уровень моба хранится на сервере (в его NBT) и приходит пакетом
 * {@code EnemySyncPacket}. Клиент раскладывает присланные данные по id сущностей —
 * потом их использует отрисовка таблички над головой.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientEnemyData {
    private static final Int2IntMap LEVELS = new Int2IntOpenHashMap();
    private static final Int2FloatMap MAX_HEALTHS = new Int2FloatOpenHashMap();

    private ClientEnemyData() {}

    public static void setLevel(int entityId, int level) {
        LEVELS.put(entityId, level);
    }

    /** Уровень моба или 0, если данных нет (значит табличку рисовать не надо). */
    public static int getLevel(int entityId) {
        return LEVELS.getOrDefault(entityId, 0);
    }

    /** Максимальное HP моба (нужно для полоски «12/40» над головой). */
    public static void setMaxHealth(int entityId, float maxHealth) {
        MAX_HEALTHS.put(entityId, maxHealth);
    }

    public static float getMaxHealth(int entityId) {
        return MAX_HEALTHS.getOrDefault(entityId, 0.0F);
    }

    public static void remove(int entityId) {
        LEVELS.remove(entityId);
        MAX_HEALTHS.remove(entityId);
    }

    public static void clear() {
        LEVELS.clear();
        MAX_HEALTHS.clear();
    }
}
