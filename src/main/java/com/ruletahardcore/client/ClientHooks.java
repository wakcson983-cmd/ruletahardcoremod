package com.ruletahardcore.client;

import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openMenu() {
        Minecraft.getInstance().setScreen(new RuletaScreen());
    }
}
