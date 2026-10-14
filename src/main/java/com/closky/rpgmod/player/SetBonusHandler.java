package com.closky.rpgmod.player;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.item.RpgArmorItem;
import com.closky.rpgmod.network.SyncPlayerDataPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Бонусы за полный сет брони (требование 4г) и смена класса «на ходу» (требование 4в).
 *
 * <p>Игрок надел 4 предмета одного класса — получил классовые бонусы. Снял/сменил сет —
 * бонусы мгновенно пересчитываются, прогресс персонажа при этом не теряется.</p>
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID)
public class SetBonusHandler {
    private static final UUID UUID_SET_ARMOR = uuid("set_armor");
    private static final UUID UUID_SET_DAMAGE = uuid("set_damage");
    private static final UUID UUID_SET_SPEED = uuid("set_speed");
    private static final UUID UUID_SET_ATTACK_SPEED = uuid("set_attack_speed");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        PlayerClass detected = detectFullSet(player);
        RpgPlayerData data = RpgPlayerData.get(player);

        if (detected != data.getActiveClass()) {
            data.setActiveClass(detected);
            applyClassBonuses(player, detected);
            SyncPlayerDataPacket.sendTo(player, data);
            if (detected != PlayerClass.NONE) {
                player.displayClientMessage(Component.translatable("msg.rpgmod.class_set",
                        Component.translatable(detected.getTranslationKey()).withStyle(detected.getColor())), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_DIAMOND, SoundSource.PLAYERS, 0.8F, 1.2F);
            }
        }

        // Друид под полным сетом slowly лечится
        if (detected == PlayerClass.DRUID) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
        }
    }

    /** Полный сет = все 4 слота брони нашего мода одного класса. */
    public static PlayerClass detectFullSet(Player player) {
        PlayerClass found = null;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof RpgArmorItem armor)) {
                return PlayerClass.NONE;
            }
            if (found == null) {
                found = armor.getPlayerClass();
            } else if (found != armor.getPlayerClass()) {
                return PlayerClass.NONE;
            }
        }
        return found == null ? PlayerClass.NONE : found;
    }

    public static void applyClassBonuses(ServerPlayer player, PlayerClass playerClass) {
        modify(player, Attributes.ARMOR, UUID_SET_ARMOR, "rpgmod:set_armor",
                playerClass.getBonusArmor(), AttributeModifier.Operation.ADDITION);
        modify(player, Attributes.ATTACK_DAMAGE, UUID_SET_DAMAGE, "rpgmod:set_damage",
                playerClass.getDamageMultiplier() - 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        modify(player, Attributes.MOVEMENT_SPEED, UUID_SET_SPEED, "rpgmod:set_speed",
                playerClass.getSpeedBonus(), AttributeModifier.Operation.MULTIPLY_TOTAL);
        modify(player, Attributes.ATTACK_SPEED, UUID_SET_ATTACK_SPEED, "rpgmod:set_attack_speed",
                playerClass.getAttackSpeedBonus(), AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void modify(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute,
                               UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(uuid);
        if (amount != 0.0D) {
            instance.addTransientModifier(new AttributeModifier(uuid, name, amount, operation));
        }
    }

    /** Текст бонусов класса (используется в HUD и подсказках). */
    public static Component describeClass(PlayerClass playerClass) {
        return Component.translatable(playerClass.getBonusKey()).withStyle(ChatFormatting.GRAY);
    }

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("rpgmod:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
