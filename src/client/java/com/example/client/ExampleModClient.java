package com.example.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ExampleModClient implements ClientModInitializer {
    // قائمة لتخزين أماكن الريدستون القريبة من اللاعب لمنع الـ Lag
    private final List<BlockPos> redstoneWires = new ArrayList<>();
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        
        // 1. حدث يشتغل في الخلفية لتحديث قائمة بلوكات الريدستون القريبة
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            
            tickCounter++;
            // بنعمل الفحص ده كل 5 تيكس بس مش كل فريم عشان نحافظ على الأداء
            if (tickCounter % 5 == 0) {
                redstoneWires.clear();
                BlockPos playerPos = client.player.blockPosition();
                int radius = 8; // المسافة المحيطة باللاعب اللي المود هيقرأ فيها (8 بلوكات في كل اتجاه)

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos pos = playerPos.offset(x, y, z);
                            if (client.level.getBlockState(pos).is(Blocks.REDSTONE_WIRE)) {
                                redstoneWires.add(pos);
                            }
                        }
                    }
                }
            }
        });

        // 2. حدث الرندرة: المسؤول عن رسم الأرقام 3D داخل عالم اللعبة
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null || redstoneWires.isEmpty()) return;

            PoseStack poseStack = context.matrixStack();
            Camera camera = context.camera();
            Vec3 cameraPos = camera.getPosition();
            MultiBufferSource bufferSource = context.consumers();
            Font font = client.font;

            for (BlockPos pos : redstoneWires) {
                BlockState state = client.level.getBlockState(pos);
                if (!state.is(Blocks.REDSTONE_WIRE)) continue;

                // قراءة طاقة الريدستون
                int power = state.getValue(RedStoneWireBlock.POWER);
                String text = String.valueOf(power);

                poseStack.pushPose();
                
                // حساب موقع النص بالظبط فوق البلوكة بالنسبة لكاميرا اللاعب
                double renderX = pos.getX() - cameraPos.x + 0.5;
                double renderY = pos.getY() - cameraPos.y + 0.25; // الارتفاع المظبوط فوق السلك مباشرة
                double renderZ = pos.getZ() - cameraPos.z + 0.5;

                poseStack.translate(renderX, renderY, renderZ);
                
                // الكود السحري اللي بيخلي الكتابة تلف وتواجه شاشة اللاعب دايماً من أي زاوية يبص منها
                poseStack.mulPose(camera.rotation());
                
                // تصغير حجم النص عشان يطلع مناسب وميغطيش البلوكة كلها
                poseStack.scale(-0.02F, -0.02F, 0.02F);

                Matrix4f matrix = poseStack.last().pose();
                float textWidth = font.width(text);
                float xOffset = -textWidth / 2; // لتوسيط الرقم في منتصف البلوكة تماماً

                // رسم النص في فضاء اللعبة
                font.drawInBatch(text, xOffset, 0, 0xFFFFFF, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

                poseStack.popPose();
            }
        });
    }
}
