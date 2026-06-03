package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // حدث يشتغل مع كل حركة (Tick) في اللعبة عند اللاعب
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) {
                // معرفة البلوكة اللي اللاعب بيبص عليها حالياً (الكروس هير)
                HitResult hit = client.crosshairTarget;
                
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    BlockPos pos = blockHit.getBlockPos();
                    BlockState state = client.level.getBlockState(pos);

                    // التأكد لو البلوكة دي هي سلك ريدستون (Redstone Wire)
                    if (state.is(Blocks.REDSTONE_WIRE)) {
                        // الحصول على قيمة القوة (من 0 إلى 15)
                        int power = state.getValue(RedstoneWireBlock.POWER);
                        
                        // عرض القوة في شريط الـ Action Bar فوق الأدوات مباشرة
                        client.player.displayClientMessage(Component.literal("Redstone Power: " + power), true);
                    }
                }
            }
        });
    }
}
