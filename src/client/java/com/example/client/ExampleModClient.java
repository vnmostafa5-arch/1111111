package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock; // هنا الـ S كابيتال
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // حدث يشتغل مع كل حركة (Tick) في اللعبة عند اللاعب
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) {
                // في موجانج مابينج اسمها hitResult
                HitResult hit = client.hitResult;
                
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    BlockPos pos = blockHit.getBlockPos();
                    BlockState state = client.level.getBlockState(pos);

                    // التأكد لو البلوكة دي هي سلك ريدستون
                    if (state.is(Blocks.REDSTONE_WIRE)) {
                        // الحصول على قيمة القوة
                        int power = state.getValue(RedStoneWireBlock.POWER);
                        
                        // عرض القوة في الـ Action Bar بطريقة متوافقة ومضمونة للـ Client
                        client.gui.setOverlayMessage(Component.literal("Redstone Power: " + power), false);
                    }
                }
            }
        });
    }
}
