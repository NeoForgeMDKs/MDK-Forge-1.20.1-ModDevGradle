package com.closky.rpgmod.player;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.enemy.EnemyLevelHelper;
import com.closky.rpgmod.item.WeaponData;
import com.closky.rpgmod.network.SyncPlayerDataPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Команды мода: /rpg stats, /rpg level, /rpg enemy.
 * Полезно для отладки и для админов сервера.
 */
@Mod.EventBusSubscriber(modid = RpgMod.MODID)
public class RpgCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("rpg")
                .then(Commands.literal("stats").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    RpgPlayerData data = RpgPlayerData.get(player);
                    ctx.getSource().sendSuccess(() -> Component.literal("=== RPG ===").withStyle(ChatFormatting.GOLD), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("msg.rpgmod.cmd_level",
                            Component.literal(String.valueOf(data.getLevel()))), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("msg.rpgmod.cmd_points",
                            Component.literal(String.valueOf(data.getStatPoints()))), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("msg.rpgmod.cmd_stats",
                            Component.literal(String.valueOf(data.getSpentStrength())),
                            Component.literal(String.valueOf(data.getSpentVitality())),
                            Component.literal(String.valueOf(data.getSpentIntellect()))), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("msg.rpgmod.cmd_mana",
                            Component.literal((int) data.getMana() + "/" + (int) data.getMaxMana())), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("msg.rpgmod.cmd_class",
                            Component.translatable(data.getActiveClass().getTranslationKey())), false);
                    return 1;
                }))
                .then(Commands.literal("level")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1, 1000))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                            RpgPlayerData data = RpgPlayerData.get(player);
                                            data.setLevel(value);
                                            PlayerProgressionHandler.applyStatBonuses(player);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Уровень: " + value), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("add")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1, 1000))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                            RpgPlayerData data = RpgPlayerData.get(player);
                                            data.setLevel(data.getLevel() + value);
                                            data.addStatPoints(value);
                                            PlayerProgressionHandler.applyStatBonuses(player);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Уровень: " + data.getLevel()), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            RpgPlayerData data = RpgPlayerData.get(player);
                            data.resetStats();
                            PlayerProgressionHandler.applyStatBonuses(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Очки характеристик сброшены"), true);
                            return 1;
                        }))
                .then(Commands.literal("spend")
                        .then(Commands.argument("stat", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (StatType type : StatType.values()) builder.suggest(type.getId());
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    StatType type = StatType.byId(StringArgumentType.getString(ctx, "stat"));
                                    RpgPlayerData data = RpgPlayerData.get(player);
                                    if (!data.spendStatPoint(type)) {
                                        ctx.getSource().sendFailure(Component.translatable("msg.rpgmod.no_stat_points"));
                                        return 0;
                                    }
                                    PlayerProgressionHandler.applyStatBonuses(player);
                                    SyncPlayerDataPacket.sendTo(player, data);
                                    ctx.getSource().sendSuccess(() -> Component.translatable("msg.rpgmod.stat_spent",
                                            Component.translatable(type.getTranslationKey()),
                                            Component.literal(String.valueOf(data.getStatPoints()))), false);
                                    return 1;
                                })))
                .then(Commands.literal("enemy")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ItemStack stack = player.getMainHandItem();
                            int weaponLevel = WeaponData.getLevel(stack);
                            LivingEntity nearest = player.level().getNearestEntity(
                                    player.level().getEntitiesOfClass(LivingEntity.class,
                                            player.getBoundingBox().inflate(16.0D)),
                                    net.minecraft.world.entity.ai.targeting.TargetingConditions.DEFAULT,
                                    player, player.getX(), player.getY(), player.getZ());
                            int enemyLevel = nearest == null ? 0 : EnemyLevelHelper.getLevel(nearest);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Уровень оружия: " + weaponLevel + ", уровень ближайшего врага: " + enemyLevel), false);
                            return 1;
                        })));
    }

    private RpgCommands() {}
}
