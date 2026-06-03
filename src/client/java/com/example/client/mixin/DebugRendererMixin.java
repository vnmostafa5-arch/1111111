package com.example.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.example.client.ExampleModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || ExampleModClient.REDSTONE_WIRES.isEmpty()) return;

        Font font = client.font;

        for (BlockPos pos : ExampleModClient.REDSTONE_WIRES) {
            BlockState state = client.level.getBlockState(pos);
            if (!state.is(Blocks.REDSTONE_WIRE)) continue;

            // جلب قوة الريدستون
            int power = state.getValue(RedStoneWireBlock.POWER);
            String text = String.valueOf(power);

            poseStack.pushPose();

            // حساب موقع النص الثلاثي الأبعاد بدقة نسبةً لكاميرا اللاعب لمنع الأخطاء
            double renderX = pos.getX() - cameraX + 0.5;
            double renderY = pos.getY() - cameraY + 0.4; // الارتفاع فوق السلك مباشرة
            double renderZ = pos.getZ() - cameraZ + 0.5;

            poseStack.translate(renderX, renderY, renderZ);

            // جعل النص يلتف ويواجه عين اللاعب تلقائياً من أي زاوية
            poseStack.mulPose(client.gameRenderer.getMainCamera().rotation());

            // حجم النص المظبوط للبلوكة
            poseStack.scale(-0.02F, -0.02F, 0.02F);

            Matrix4f matrix = poseStack.last().pose();
            float textWidth = font.width(text);
            float xOffset = -textWidth / 2; // التوسيط في المنتصف تماماً

            // رسم الرقم داخل العالم
            font.drawInBatch(text, xOffset, 0, 0xFFFFFF, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

            poseStack.popPose();
        }
    }
}
