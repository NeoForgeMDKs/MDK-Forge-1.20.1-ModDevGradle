package com.closky.rpgmod.item;

/**
 * Класс оружия (тип). Каждый класс бьёт по-своему: урон, скорость атаки, дальность, шанс крита.
 *
 * <p>Значения — «модификаторы» атрибутов, а не абсолютные числа. Скорость атаки: у игрока
 * базово 4.0, поэтому модификатор -2.4 даёт итоговые 1.6 удара в секунду (как у меча).</p>
 */
public enum WeaponClass {
    DAGGER("dagger", "Кинжал", 0.70F, -1.6F, 0.0F, 0.08F,
            "Быстрые удары с высоким шансом критического урона"),
    SWORD("sword", "Меч", 1.00F, -2.4F, 0.0F, 0.03F,
            "Сбалансированное оружие без слабых мест"),
    GREATSWORD("greatsword", "Двуручник", 1.45F, -3.0F, 0.5F, 0.0F,
            "Огромный урон, но очень медленные удары"),
    AXE("axe", "Секира", 1.30F, -2.9F, 0.0F, 0.01F,
            "Тяжёлые рубящие удары"),
    SPEAR("spear", "Копьё", 1.10F, -2.2F, 1.5F, 0.02F,
            "Увеличенная дальность атаки"),
    MACE("mace", "Дробитель", 1.35F, -3.0F, 0.0F, 0.01F,
            "Проламывает броню: часть урона игнорирует защиту");

    private final String id;
    private final String displayName;
    private final float damageMultiplier;
    private final float attackSpeedModifier;
    private final float reachBonus;
    private final float critChance;
    private final String description;

    WeaponClass(String id, String displayName, float damageMultiplier, float attackSpeedModifier,
                float reachBonus, float critChance, String description) {
        this.id = id;
        this.displayName = displayName;
        this.damageMultiplier = damageMultiplier;
        this.attackSpeedModifier = attackSpeedModifier;
        this.reachBonus = reachBonus;
        this.critChance = critChance;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getAttackSpeedModifier() {
        return attackSpeedModifier;
    }

    public float getReachBonus() {
        return reachBonus;
    }

    public float getCritChance() {
        return critChance;
    }

    public String getDescription() {
        return description;
    }

    public String getTranslationKey() {
        return "weapon_class.rpgmod." + id;
    }

    public String getDescriptionKey() {
        return "weapon_class.rpgmod." + id + ".desc";
    }
}
