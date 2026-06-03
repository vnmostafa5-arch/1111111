package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class ExampleModClient implements ClientModInitializer {
    // قائمة عامة ثابتة هنخزن فيها أماكن الريدستون عشان الـ Mixins يقراها
    public static final List<BlockPos> REDSTONE_WIRES = new ArrayList<>();
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // حدث فحص البلوكات القريبة كل 5 تيكس لحماية الأداء ومنع اللاج
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            
            tickCounter++;
            if (tickCounter % 5 == 0) {
                REDSTONE_WIRES.clear();
                BlockPos playerPos = client.player.blockPosition();
                int radius = 8; // مسافة الفحص حول اللاعب

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos pos = playerPos.offset(x, y, z);
                            if (client.level.getBlockState(pos).is(Blocks.REDSTONE_WIRE)) {
                                REDSTONE_WIRES.add(pos);
                            }
                        }
                    }
                }
            }
        });
    }
}
