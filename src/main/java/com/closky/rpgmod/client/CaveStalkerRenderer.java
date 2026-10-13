package com.closky.rpgmod.client;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.entity.CaveStalker;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Рендер пещерного хищника (CaveStalker) — приземистый гуманоид. */
@OnlyIn(Dist.CLIENT)
public class CaveStalkerRenderer extends HumanoidMobRenderer<CaveStalker, HumanoidModel<CaveStalker>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RpgMod.MODID, "textures/entity/cave_stalker.png");

    public CaveStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    protected void scale(CaveStalker entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9F, 0.9F, 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(CaveStalker entity) {
        return TEXTURE;
    }
}
