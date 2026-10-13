package com.closky.rpgmod.player;

/** Три характеристики персонажа, которые игрок прокачивает за очки. */
public enum StatType {
    STRENGTH("strength", "Сила", "Увеличивает урон в ближнем бою"),
    VITALITY("vitality", "Живучесть", "Увеличивает максимальное здоровье"),
    INTELLECT("intellect", "Интеллект", "Увеличивает ману и силу заклинаний");

    private final String id;
    private final String displayName;
    private final String description;

    StatType(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getTranslationKey() {
        return "stat.rpgmod." + id;
    }

    public String getDescriptionKey() {
        return "stat.rpgmod." + id + ".desc";
    }

    /** Поиск характеристики по её id (используется командой /rpg spend). */
    public static StatType byId(String id) {
        for (StatType type : values()) {
            if (type.id.equalsIgnoreCase(id)) return type;
        }
        return STRENGTH;
    }
}
