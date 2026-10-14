package com.closky.rpgmod.client;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.entity.DungeonWarden;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Рендер босса подземелий — увеличенный гуманоид. */
@OnlyIn(Dist.CLIENT)
public class DungeonWardenRenderer extends HumanoidMobRenderer<DungeonWarden, HumanoidModel<DungeonWarden>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RpgMod.MODID, "textures/entity/dungeon_warden.png");

    public DungeonWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.9F);
    }

    @Override
    protected void scale(DungeonWarden entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.7F, 1.7F, 1.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(DungeonWarden entity) {
        return TEXTURE;
    }
}
