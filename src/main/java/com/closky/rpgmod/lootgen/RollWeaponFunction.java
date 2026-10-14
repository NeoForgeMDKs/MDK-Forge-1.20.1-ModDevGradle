package com.closky.rpgmod.lootgen;

import com.closky.rpgmod.item.WeaponRoller;
import com.closky.rpgmod.registry.ModLootFunctions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Лут-функция {@code rpgmod:roll_weapon} — «выкатить» случайное оружие нужного уровня.
 *
 * <p>Используется в наших сундуках:</p>
 * <pre>
 * "functions": [ { "function": "rpgmod:roll_weapon", "min_level": 3, "max_level": 6, "honing": true } ]
 * </pre>
 */
public class RollWeaponFunction extends LootItemConditionalFunction {
    private final int minLevel;
    private final int maxLevel;
    private final float honeChance;

    protected RollWeaponFunction(LootItemCondition[] conditions, int minLevel, int maxLevel, float honeChance) {
        super(conditions);
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.honeChance = honeChance;
    }

    public static LootItemConditionalFunction.Builder<?> roll(int minLevel, int maxLevel, boolean honing) {
        return simpleBuilder(conditions -> new RollWeaponFunction(conditions, minLevel, maxLevel, honing ? 0.3F : 0.0F));
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        return WeaponRoller.roll(context.getRandom(), minLevel, maxLevel, honeChance);
    }

    @Override
    public LootItemFunctionType getType() {
        return ModLootFunctions.ROLL_WEAPON.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RollWeaponFunction> {
        @Override
        public void serialize(JsonObject json, RollWeaponFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
            json.addProperty("min_level", function.minLevel);
            json.addProperty("max_level", function.maxLevel);
            json.addProperty("hone_chance", function.honeChance);
        }

        @Override
        public RollWeaponFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            int min = GsonHelper.getAsInt(json, "min_level", 1);
            int max = GsonHelper.getAsInt(json, "max_level", 5);
            float hone = GsonHelper.getAsFloat(json, "hone_chance", 0.3F);
            return new RollWeaponFunction(conditions, min, max, hone);
        }
    }
}
