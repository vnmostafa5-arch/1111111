package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExampleModClient implements ClientModInitializer {
    private final Map<BlockPos, Display.TextDisplay> activeDisplays = new HashMap<>();
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                activeDisplays.clear();
                return;
            }

            tickCounter++;
            if (tickCounter % 5 == 0) {
                BlockPos playerPos = client.player.blockPosition();
                int radius = 8; // مسافة الفحص حول اللاعب
                Map<BlockPos, Integer> currentRedstone = new HashMap<>();

                // 1. البحث عن البلوكات
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos pos = playerPos.offset(x, y, z);
                            BlockState state = client.level.getBlockState(pos);
                            if (state.is(Blocks.REDSTONE_WIRE)) {
                                int power = state.getValue(RedStoneWireBlock.POWER);
                                currentRedstone.put(pos, power);
                            }
                        }
                    }
                }

                // 2. تنظيف النصوص القديمة أو البعيدة
                Iterator<Map.Entry<BlockPos, Display.TextDisplay>> iterator = activeDisplays.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<BlockPos, Display.TextDisplay> entry = iterator.next();
                    BlockPos pos = entry.getKey();
                    Display.TextDisplay display = entry.getValue();

                    if (!currentRedstone.containsKey(pos) || display.isRemoved()) {
                        // إزالة الكيان من عالم العميل باستخدام الـ ID الخاص به
                        client.level.removeEntity(display.getId(), Entity.RemovalReason.DISCARDED);
                        iterator.remove();
                    }
                }

                // 3. إنشاء أو تحديث النصوص
                for (Map.Entry<BlockPos, Integer> entry : currentRedstone.entrySet()) {
                    BlockPos pos = entry.getKey();
                    int power = entry.getValue();
                    String textStr = String.valueOf(power);

                    if (activeDisplays.containsKey(pos)) {
                        Display.TextDisplay display = activeDisplays.get(pos);
                        display.setText(Component.literal(textStr));
                    } else {
                        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, client.level);
                        
                        // تحديد الموقع بدقة فوق سلك الريدستون
                        display.setPos(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5);
                        display.setText(Component.literal(textStr));
                        display.setBillboardConstraints(Display.BillboardConstraints.CENTER); // يلتف مع عين اللاعب
                        
                        // التعديل السحري: توليد ID فريد سالب لمنع التعارض مع السيرفر ولإجبار العميل على رندرتها
                        int uniqueId = -(pos.getX() * 31 + pos.getY() * 17 + pos.getZ()) - 1000;
                        display.setId(uniqueId);
                        
                        // إدخال الكيان عبر الدالة المخصصة للـ ClientLevel
                        client.level.addEntity(uniqueId, display);
                        activeDisplays.put(pos, display);
                    }
                }
            }
        });
    }
}
