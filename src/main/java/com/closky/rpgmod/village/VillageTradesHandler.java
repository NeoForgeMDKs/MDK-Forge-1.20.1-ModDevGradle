package com.closky.rpgmod.village;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.player.PlayerClass;
import com.closky.rpgmod.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Торговля деревень: материалы, броня класса, ключи подземелий.
 *
 * <p>Важно: ОРУЖИЕ И ИНСТРУМЕНТЫ У ЖИТЕЛЕЙ НЕ ПРОДАЮТСЯ (требование 1б — только из лута).
 * Зато каждая деревня торгует своим набором — торговать всем в одной деревне не получится.</p>
 *
 * <p>Сделки описаны своим классом {@link EmeraldTrade}, потому что ванильный
 * {@code VillagerTrades.ItemsForEmeralds} в 1.20.1 недоступен снаружи пакета.</p>
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID)
public class VillageTradesHandler {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        VillagerProfession profession = event.getType();

        if (profession == VillagerProfession.ARMORER) {
            // Военная деревня: броня воина и стальные слитки
            addArmorTrades(event, PlayerClass.WARRIOR);
            event.getTrades().get(2).add(sell(6, ModItems.STEEL_INGOT.get(), 2, 12));
        }
        if (profession == VillagerProfession.FLETCHER) {
            // Охотничья деревня: броня стрелка
            addArmorTrades(event, PlayerClass.RANGER);
        }
        if (profession == VillagerProfession.CLERIC) {
            // Поселение магов: броня мага, арканная пыль, кристаллы маны
            addArmorTrades(event, PlayerClass.MAGE);
            event.getTrades().get(2).add(sell(4, ModItems.ARCANE_DUST.get(), 3, 15));
            event.getTrades().get(3).add(sell(12, ModItems.MANA_CRYSTAL.get(), 1, 25));
        }
        if (profession == VillagerProfession.FARMER) {
            // Поселение друидов: броня друида
            addArmorTrades(event, PlayerClass.DRUID);
        }
        if (profession == VillagerProfession.LIBRARIAN) {
            // Ключи подземелий — редкий товар библиотекаря (мастер-уровень)
            event.getTrades().get(4).add(sell(32, ModItems.DUNGEON_KEY.get(), 1, 40));
        }
        if (profession == VillagerProfession.WEAPONSMITH) {
            // Сумрачный слиток — только у оружейника
            event.getTrades().get(2).add(sell(8, ModItems.DUSK_INGOT.get(), 1, 30));
        }
    }

    /** Житель продаёт броню своего класса по уровням: ботинки -> поножи -> нагрудник -> шлем. */
    private static void addArmorTrades(VillagerTradesEvent event, PlayerClass playerClass) {
        event.getTrades().get(1).add(sell(5, ModItems.getArmor(playerClass, ArmorItem.Type.BOOTS), 1, 10));
        event.getTrades().get(2).add(sell(8, ModItems.getArmor(playerClass, ArmorItem.Type.LEGGINGS), 1, 15));
        event.getTrades().get(3).add(sell(12, ModItems.getArmor(playerClass, ArmorItem.Type.CHESTPLATE), 1, 20));
        event.getTrades().get(4).add(sell(10, ModItems.getArmor(playerClass, ArmorItem.Type.HELMET), 1, 25));
    }

    /**
     * «Продать предмет за изумруды».
     *
     * @param emeraldCost сколько изумрудов хочет житель
     * @param result      что он даёт
     * @param count       сколько даёт
     * @param xp          опыт жителя за сделку
     */
    private static VillagerTrades.ItemListing sell(int emeraldCost, Item result, int count, int xp) {
        return new EmeraldTrade(result, emeraldCost, count, 12, xp);
    }

    /** Своя реализация ванильной сделки «изумруды -> предмет» (в 1.20.1 ванильная недоступна снаружи). */
    private static final class EmeraldTrade implements VillagerTrades.ItemListing {
        private final Item item;
        private final int emeraldCost;
        private final int count;
        private final int maxUses;
        private final int villagerXp;

        private EmeraldTrade(Item item, int emeraldCost, int count, int maxUses, int villagerXp) {
            this.item = item;
            this.emeraldCost = emeraldCost;
            this.count = count;
            this.maxUses = maxUses;
            this.villagerXp = villagerXp;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemStack(Items.EMERALD, emeraldCost),
                    new ItemStack(item, count), maxUses, villagerXp, 0.05F);
        }
    }

    private VillageTradesHandler() {}
}
