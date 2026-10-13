package com.closky.rpgmod.registry;

import com.closky.rpgmod.RpgMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Своя вкладка в творческом режиме — чтобы всё добро мода было в одном месте. */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RpgMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.rpgmod.main"))
            .icon(() -> new ItemStack(ModItems.DUSK_INGOT.get()))
            .displayItems((parameters, output) -> ModItems.creativeTabItems().forEach(item -> output.accept(item.get())))
            .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
