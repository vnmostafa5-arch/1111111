package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExampleModClient implements ClientModInitializer {
    // جدول لتخزين الكيانات النصية النشطة فوق كل بلوكة ريدستون لمنع التكرار والـ Lag
    private final Map<BlockPos, Display.TextDisplay> activeDisplays = new HashMap<>();
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // الحدث المتوافق تماماً مع مشروعك الحالي والذي نجح في البناء سابقاً
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                activeDisplays.clear();
                return;
            }

            tickCounter++;
            // الفحص والتحديث كل 5 حركات (Ticks) لحماية الأداء والـ FPS
            if (tickCounter % 5 == 0) {
                BlockPos playerPos = client.player.blockPosition();
                int radius = 8; // مسافة الفحص حول اللاعب (8 بلوكات في كل اتجاه)
                Map<BlockPos, Integer> currentRedstone = new HashMap<>();

                // 1. البحث عن بلوكات الريدستون القريبة وقراءة قوتها الحالية
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

                // 2. تنظيف وإزالة الكيانات النصية للبلوكات التي اختفت، كُسرت أو ابتعد عنها اللاعب
                Iterator<Map.Entry<BlockPos, Display.TextDisplay>> iterator = activeDisplays.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<BlockPos, Display.TextDisplay> entry = iterator.next();
                    BlockPos pos = entry.getKey();
                    Display.TextDisplay display = entry.getValue();

                    if (!currentRedstone.containsKey(pos) || display.isRemoved()) {
                        display.discard(); // حذف الكيان النصي من عالم اللعبة تماماً
                        iterator.remove();
                    }
                }

                // 3. إنشاء نصوص جديدة أو تحديث أرقام الطاقة للكيانات الحالية
                for (Map.Entry<BlockPos, Integer> entry : currentRedstone.entrySet()) {
                    BlockPos pos = entry.getKey();
                    int power = entry.getValue();
                    String textStr = String.valueOf(power);

                    if (activeDisplays.containsKey(pos)) {
                        // إذا كان النص موجوداً بالفعل، نقوم بتحديث الرقم فقط لو تغيرت الطاقة
                        Display.TextDisplay display = activeDisplays.get(pos);
                        display.setText(Component.literal(textStr));
                    } else {
                        // إنشاء كيان نصي 3D (TextDisplay) مدمج من كود ماينكرافت الأساسي (Mojang Mappings)
                        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, client.level);
                        
                        // تحديد موقع النص (في منتصف البلوكة ومرفوع قليلاً للأعلى فوق السلك)
                        display.setPos(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
                        display.setText(Component.literal(textStr));
                        
                        // الكود السحري: جعل النص يلتف ويواجه عين اللاعب تلقائياً من أي زاوية (Billboard)
                        display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
                        
                        // إدخال الكيان في عالم اللاعب ليقوم المحرك الافتراضي برسمه فوراً وبسلاسة
                        client.level.addFreshEntity(display);
                        activeDisplays.put(pos, display);
                    }
                }
            }
        });
    }
}
