package com.closky.rpgmod.player;

import com.closky.rpgmod.config.RpgConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Прогресс RPG-персонажа: уровень, очки характеристик, мана, класс.
 *
 * <p>Данные хранятся в «персистентном» NBT игрока (Forge-тег ForgeData), а не в отдельной
 * capability. Это самый простой способ: NBT сохраняется вместе с игроком, ничего не нужно
 * сериализовать самому. Если позже захочется сделать «правильно» — см. docs/05_Игрок.md,
 * там описан вариант с capability.</p>
 */
public class RpgPlayerData {
    public static final String ROOT_KEY = "RpgModData";

    private static final String TAG_LEVEL = "Level";
    private static final String TAG_STAT_POINTS = "StatPoints";
    private static final String TAG_SPENT_STRENGTH = "SpentStrength";
    private static final String TAG_SPENT_VITALITY = "SpentVitality";
    private static final String TAG_SPENT_INTELLECT = "SpentIntellect";
    private static final String TAG_MANA = "Mana";
    private static final String TAG_SET_CLASS = "SetClass";
    private static final String TAG_LAST_SYNCED_MANA = "LastSyncedMana";
    private static final String TAG_BONUS_APPLIED = "BonusApplied";

    private final Player player;
    private final CompoundTag tag;

    private RpgPlayerData(Player player) {
        this.player = player;
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_KEY)) {
            persistent.put(ROOT_KEY, new CompoundTag());
        }
        this.tag = persistent.getCompound(ROOT_KEY);
    }

    /** Получить данные игрока (для клиента работает только чтение — данные приходят пакетом). */
    public static RpgPlayerData get(Player player) {
        return new RpgPlayerData(player);
    }

    /** Скопировать прогресс при смерти/возрождении (вызывается из PlayerEvent.Clone). */
    public static void copyOnRespawn(Player original, Player replacement) {
        CompoundTag oldTag = original.getPersistentData().getCompound(ROOT_KEY);
        replacement.getPersistentData().put(ROOT_KEY, oldTag.copy());
    }

    // ------------------------------------------------------------------ уровень

    public int getLevel() {
        return Math.max(1, tag.getInt(TAG_LEVEL));
    }

    public void setLevel(int level) {
        tag.putInt(TAG_LEVEL, Math.max(1, Math.min(RpgConfig.MAX_RPG_LEVEL.get(), level)));
    }

    public int getStatPoints() {
        return tag.getInt(TAG_STAT_POINTS);
    }

    public void addStatPoints(int amount) {
        tag.putInt(TAG_STAT_POINTS, Math.max(0, getStatPoints() + amount));
    }

    // --------------------------------------------------------------------- статы

    public int getSpentStrength() {
        return tag.getInt(TAG_SPENT_STRENGTH);
    }

    public int getSpentVitality() {
        return tag.getInt(TAG_SPENT_VITALITY);
    }

    public int getSpentIntellect() {
        return tag.getInt(TAG_SPENT_INTELLECT);
    }

    public int getSpent(StatType type) {
        return switch (type) {
            case STRENGTH -> getSpentStrength();
            case VITALITY -> getSpentVitality();
            case INTELLECT -> getSpentIntellect();
        };
    }

    /** Вложить очко в характеристику. Сервер обязан вызвать это сам (защита от читов). */
    public boolean spendStatPoint(StatType type) {
        if (getStatPoints() <= 0) return false;
        tag.putInt(TAG_STAT_POINTS, getStatPoints() - 1);
        String key = switch (type) {
            case STRENGTH -> TAG_SPENT_STRENGTH;
            case VITALITY -> TAG_SPENT_VITALITY;
            case INTELLECT -> TAG_SPENT_INTELLECT;
        };
        tag.putInt(key, tag.getInt(key) + 1);
        return true;
    }

    /** Полный сброс вложенных очков (например, командой админа). */
    public void resetStats() {
        int spent = getSpentStrength() + getSpentVitality() + getSpentIntellect();
        tag.putInt(TAG_SPENT_STRENGTH, 0);
        tag.putInt(TAG_SPENT_VITALITY, 0);
        tag.putInt(TAG_SPENT_INTELLECT, 0);
        addStatPoints(spent);
    }

    // ---------------------------------------------------------------------- мана

    public PlayerClass getActiveClass() {
        return PlayerClass.byId(tag.getString(TAG_SET_CLASS));
    }

    public void setActiveClass(PlayerClass playerClass) {
        tag.putString(TAG_SET_CLASS, playerClass.getId());
    }

    public float getMaxMana() {
        double base = RpgConfig.BASE_MAX_MANA.get();
        double fromIntellect = getSpentIntellect() * RpgConfig.MANA_PER_INTELLECT_POINT.get();
        return (float) (base + fromIntellect + getActiveClass().getBonusMana());
    }

    public float getMana() {
        return Math.min(tag.getFloat(TAG_MANA), getMaxMana());
    }

    public void setMana(float mana) {
        tag.putFloat(TAG_MANA, Math.max(0.0F, Math.min(mana, getMaxMana())));
    }

    public void addMana(float amount) {
        setMana(getMana() + amount);
    }

    public boolean consumeMana(float amount) {
        if (getMana() < amount) return false;
        setMana(getMana() - amount);
        return true;
    }

    public float getSpellPowerMultiplier() {
        return (float) (1.0D + getSpentIntellect() * RpgConfig.SPELL_POWER_PER_INTELLECT.get());
    }

    // ---------------------------------------------------------------- утилиты

    /** Общий бонус к урону от силы (множитель). */
    public float getMeleeDamageMultiplier() {
        return 1.0F + (float) (getSpentStrength() * RpgConfig.DAMAGE_PER_STRENGTH_POINT.get());
    }

    public float getLastSyncedMana() {
        return tag.getFloat(TAG_LAST_SYNCED_MANA);
    }

    public void setLastSyncedMana(float mana) {
        tag.putFloat(TAG_LAST_SYNCED_MANA, mana);
    }

    public boolean isBonusApplied() {
        return tag.getBoolean(TAG_BONUS_APPLIED);
    }

    public void setBonusApplied(boolean applied) {
        tag.putBoolean(TAG_BONUS_APPLIED, applied);
    }

    /**
     * Начисление уровня персонажа по ванильному опыту.
     * По ТЗ 5 ванильных уровней опыта = 1 уровень персонажа.
     *
     * @return true, если уровень повысился
     */
    public boolean checkExperienceLevelUp() {
        int levelsPerRpgLevel = Math.max(1, RpgConfig.XP_LEVELS_PER_RPG_LEVEL.get());
        int derived = player.experienceLevel / levelsPerRpgLevel + 1;
        if (derived <= getLevel()) return false;
        int gained = Math.min(derived, RpgConfig.MAX_RPG_LEVEL.get()) - getLevel();
        if (gained <= 0) return false;
        setLevel(getLevel() + gained);
        int points = gained * RpgConfig.STAT_POINTS_PER_LEVEL.get();
        int bonusEvery = RpgConfig.BONUS_STAT_POINTS_EVERY_N_LEVELS.get();
        if (bonusEvery > 0) {
            for (int lvl = getLevel() - gained + 1; lvl <= getLevel(); lvl++) {
                if (lvl % bonusEvery == 0) points++;
            }
        }
        addStatPoints(points);
        return true;
    }

    @Override
    public String toString() {
        return "RpgPlayerData[level=" + getLevel() + ", points=" + getStatPoints()
                + ", str=" + getSpentStrength() + ", vit=" + getSpentVitality()
                + ", int=" + getSpentIntellect() + ", mana=" + getMana() + "/" + getMaxMana() + "]";
    }
}
