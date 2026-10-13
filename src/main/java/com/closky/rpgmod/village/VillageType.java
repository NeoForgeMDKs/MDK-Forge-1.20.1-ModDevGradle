package com.closky.rpgmod.village;

import com.closky.rpgmod.player.PlayerClass;

/**
 * Специализация деревни. Одна деревня не может торговать всем — только своим набором
 * профессий, поэтому игроку выгодно ходить между деревнями (требование 2к).
 */
public enum VillageType {
    WARRIOR_VILLAGE("warrior", "Военное поселение", PlayerClass.WARRIOR,
            "Кузнецы, оружейники, кожевники — оружие и броня воина"),
    RANGER_VILLAGE("ranger", "Охотничье поселение", PlayerClass.RANGER,
            "Лучники, картографы, пастухи — стрелковое снаряжение"),
    MAGE_VILLAGE("mage", "Поселение магов", PlayerClass.MAGE,
            "Клирики, библиотекари — посохи, свитки, мана"),
    DRUID_VILLAGE("druid", "Поселение друидов", PlayerClass.DRUID,
            "Фермеры, рыбаки — травы, зелья, природа");

    private final String id;
    private final String displayName;
    private final PlayerClass playerClass;
    private final String description;

    VillageType(String id, String displayName, PlayerClass playerClass, String description) {
        this.id = id;
        this.displayName = displayName;
        this.playerClass = playerClass;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public String getDescription() {
        return description;
    }

    public String getTranslationKey() {
        return "village_type.rpgmod." + id;
    }

    public static VillageType byId(String id) {
        for (VillageType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return WARRIOR_VILLAGE;
    }
}
