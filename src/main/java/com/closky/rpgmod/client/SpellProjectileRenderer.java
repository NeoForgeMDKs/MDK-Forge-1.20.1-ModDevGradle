package com.closky.rpgmod.client;

import com.closky.rpgmod.RpgMod;
import com.closky.rpgmod.entity.SpellProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/**
 * Снаряд заклинания: светящийся «шарик», который красится в цвет стихии.
 * Используется одна белая текстура-блик, цвет задаётся вершинами.
 */
@OnlyIn(Dist.CLIENT)
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RpgMod.MODID, "textures/entity/spell_projectile.png");
    private static final float SIZE = 0.55F;

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SpellProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        int color = entity.getElement().getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(SIZE, SIZE, SIZE);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));

        vertex(consumer, matrix, -0.5F, -0.5F, 0.0F, red, green, blue, 0.0F, 1.0F);
        vertex(consumer, matrix, 0.5F, -0.5F, 0.0F, red, green, blue, 1.0F, 1.0F);
        vertex(consumer, matrix, 0.5F, 0.5F, 0.0F, red, green, blue, 1.0F, 0.0F);
        vertex(consumer, matrix, -0.5F, 0.5F, 0.0F, red, green, blue, 0.0F, 0.0F);

        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                               float red, float green, float blue, float u, float v) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, 0.95F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SpellProjectile entity) {
        return TEXTURE;
    }
}
