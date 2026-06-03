package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "modid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // الحدث بيشتغل أول ما اللاعب يدخل العالم
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // إرسال رسالة ترحيبية متوافقة مع Mojang Mappings
            handler.getPlayer().sendSystemMessage(Component.literal("Welcome!"));
        });
    }
}
