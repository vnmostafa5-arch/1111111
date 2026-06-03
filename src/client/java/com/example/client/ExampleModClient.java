package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Display;
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
                int radius = 8;
                Map<BlockPos, Integer> currentRedstone = new HashMap<>();

                // 1. البحث عن بلوكات الريدستون القريبة
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

                // 2. تنظيف الأرقام القديمة أو البعيدة
                Iterator<Map.Entry<BlockPos, Display.TextDisplay>> iterator = activeDisplays.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<BlockPos, Display.TextDisplay> entry = iterator.next();
                    BlockPos pos = entry.getKey();
                    Display.TextDisplay display = entry.getValue();

                    if (!currentRedstone.containsKey(pos) || display.isRemoved()) {
                        display.discard();
                        iterator.remove();
                    }
                }

                // 3. إنشاء أو تحديث الأرقام
                for (Map.Entry<BlockPos, Integer> entry : currentRedstone.entrySet()) {
                    BlockPos pos = entry.getKey();
                    int power = entry.getValue();

                    // تحديد اللون حسب قوة الإشارة
                    // 0-5: أحمر | 6-10: أصفر | 11-15: أخضر
                    int color;
                    if (power <= 5) {
                        // أحمر يتدرج مع القوة
                        int intensity = (int) (155 + (power / 5.0f) * 100);
                        color = (intensity << 16); // RGB أحمر
                    } else if (power <= 10) {
                        // أصفر
                        int t = (int) ((power - 6) / 4.0f * 255);
                        color = (255 << 16) | (t << 8); // RGB أصفر
                    } else {
                        // أخضر يزداد كثافة
                        int green = (int) (155 + ((power - 11) / 4.0f) * 100);
                        color = (green << 8); // RGB أخضر
                    }

                    // تحديد حجم النص حسب القوة (0.4 للضعيف ← 0.8 للقوي)
                    float scale = 0.4f + (power / 15.0f) * 0.4f;

                    Component text = Component.literal(String.valueOf(power))
                            .setStyle(Style.EMPTY.withColor(color).withBold(power >= 10));

                    if (activeDisplays.containsKey(pos)) {
                        Display.TextDisplay display = activeDisplays.get(pos);
                        display.setText(text);
                        display.setTransformation(
                            new com.mojang.math.Transformation(
                                null,
                                null,
                                new org.joml.Vector3f(scale, scale, scale),
                                null
                            )
                        );
                    } else {
                        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, client.level);

                        // النص على الريدستون مباشرة
                        display.setPos(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5);
                        display.setText(text);
                        display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
                        display.setTransformation(
                            new com.mojang.math.Transformation(
                                null,
                                null,
                                new org.joml.Vector3f(scale, scale, scale),
                                null
                            )
                        );

                        int uniqueId = -(pos.getX() * 31 + pos.getY() * 17 + pos.getZ()) - 1000;
                        display.setId(uniqueId);

                        client.level.addEntity(display);
                        activeDisplays.put(pos, display);
                    }
                }
            }
        });
    }
}
