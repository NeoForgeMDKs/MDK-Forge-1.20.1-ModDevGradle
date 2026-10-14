package com.closky.rpgmod.village;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.player.PlayerClass;
import com.closky.rpgmod.registry.ModItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Торговля деревень: материалы, броня класса, ключи подземелий.
 *
 * <p>Важно: ОРУЖИЕ И ИНСТРУМЕНТЫ У ЖИТЕЛЕЙ НЕ ПРОДАЮТСЯ (требование 1б — только из лута).
 * Зато каждая деревня торгует своим набором — торговать всем в одной деревне не получится.</p>
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID)
public class VillageTradesHandler {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        VillagerProfession profession = event.getType();

        if (profession == VillagerProfession.ARMORER) {
            // Военная деревня: броня воина и стальные слитки
            addArmorTrades(event, PlayerClass.WARRIOR);
            event.getTrades().get(2).add(trade(Items.EMERALD, 6, ModItems.STEEL_INGOT.get(), 2, 12));
        }
        if (profession == VillagerProfession.FLETCHER) {
            // Охотничья деревня: броня стрелка
            addArmorTrades(event, PlayerClass.RANGER);
        }
        if (profession == VillagerProfession.CLERIC) {
            // Поселение магов: броня мага и арканная пыль
            addArmorTrades(event, PlayerClass.MAGE);
            event.getTrades().get(2).add(trade(Items.EMERALD, 4, ModItems.ARCANE_DUST.get(), 3, 15));
            event.getTrades().get(3).add(trade(Items.EMERALD, 12, ModItems.MANA_CRYSTAL.get(), 1, 25));
        }
        if (profession == VillagerProfession.FARMER) {
            // Поселение друидов: броня друида
            addArmorTrades(event, PlayerClass.DRUID);
        }
        if (profession == VillagerProfession.LIBRARIAN) {
            // Ключи подземелий — редкий товар библиотекаря (мастер-уровень)
            event.getTrades().get(4).add(trade(Items.EMERALD, 32, ModItems.DUNGEON_KEY.get(), 1, 40));
        }
        if (profession == VillagerProfession.WEAPONSMITH) {
            event.getTrades().get(2).add(trade(Items.EMERALD, 8, ModItems.DUSK_INGOT.get(), 1, 30));
        }
    }

    private static void addArmorTrades(VillagerTradesEvent event, PlayerClass playerClass) {
        event.getTrades().get(1).add(trade(Items.EMERALD, 5,
                ModItems.getArmor(playerClass, ArmorItem.Type.BOOTS).get(), 1, 10));
        event.getTrades().get(2).add(trade(Items.EMERALD, 8,
                ModItems.getArmor(playerClass, ArmorItem.Type.LEGGINGS).get(), 1, 15));
        event.getTrades().get(3).add(trade(Items.EMERALD, 12,
                ModItems.getArmor(playerClass, ArmorItem.Type.CHESTPLATE).get(), 1, 20));
        event.getTrades().get(4).add(trade(Items.EMERALD, 10,
                ModItems.getArmor(playerClass, ArmorItem.Type.HELMET).get(), 1, 25));
    }

    private static VillagerTrades.ItemListing trade(net.minecraft.world.item.Item currency, int price,
                                                    net.minecraft.world.item.Item result, int count, int xp) {
        return new VillagerTrades.BasicItemListing(
                new ItemStack(currency, price), new ItemStack(result, count), 8, xp);
    }

    private VillageTradesHandler() {}
}
