package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExampleModClient implements ClientModInitializer {
    private final Map<BlockPos, Display.TextDisplay> activeDisplays = new HashMap<>();
    private int tickCounter = 0;
    
    private boolean showDisplays = true;
    
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.examplemod.toggle_redstone", 
                GLFW.GLFW_KEY_R,                  
                "category.examplemod.tools"       
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                activeDisplays.clear();
                return;
            }

            while (toggleKey.consumeClick()) {
                showDisplays = !showDisplays;
                
                if (showDisplays) {
                    client.player.displayClientMessage(Component.literal("§aتم إظهار أرقام الريدستون"), true);
                } else {
                    client.player.displayClientMessage(Component.literal("§cتم إخفاء أرقام الريدستون"), true);
                    
                    for (Display.TextDisplay display : activeDisplays.values()) {
                        display.discard();
                    }
                    activeDisplays.clear();
                }
            }

            if (!showDisplays) {
                return;
            }

            tickCounter++;
            if (tickCounter % 5 == 0) {
                BlockPos playerPos = client.player.blockPosition();
                int radius = 8;
                Map<BlockPos, Integer> currentRedstone = new HashMap<>();

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

                for (Map.Entry<BlockPos, Integer> entry : currentRedstone.entrySet()) {
                    BlockPos pos = entry.getKey();
                    int power = entry.getValue();
                    String textStr = String.valueOf(power);

                    if (activeDisplays.containsKey(pos)) {
                        Display.TextDisplay display = activeDisplays.get(pos);
                        display.setText(Component.literal(textStr));
                    } else {
                        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, client.level);
                        
                        display.setPos(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                        display.setText(Component.literal(textStr));
                        display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
                        
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
