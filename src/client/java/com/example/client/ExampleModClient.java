package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ExampleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // التأكد من أن اللاعب داخل عالم بالفعل
            if (client.player == null || client.level == null) return;

            // فحص البلوكة التي ينظر إليها مؤشر اللاعب (Crosshair)
            HitResult hitResult = client.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState state = client.level.getBlockState(blockPos);

                // إذا كانت البلوكة سلك ريدستون، اعرض قوتها فوراً على الشاشة
                if (state.is(Blocks.REDSTONE_WIRE)) {
                    int power = state.getValue(RedStoneWireBlock.POWER);
                    
                    // إرسال رسالة في شاشة الـ Action Bar (فوق الأدوات مباشرة)
                    client.player.displayClientMessage(
                        Component.literal("§cRedstone Power: §e" + power), 
                        true // true تعني عرضها في الـ Action Bar وليس في الشات
                    );
                }
            }
        });
    }
}
