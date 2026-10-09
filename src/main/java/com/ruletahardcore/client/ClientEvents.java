package com.ruletahardcore.client;

import com.ruletahardcore.RuletaCommand;
import com.ruletahardcore.RuletaHardcore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Cuando llega el mensaje de la ruleta al chat, se lanza la animacion en pantalla
 * (todos los jugadores con el mod la ven, no hace falta paquete de red propio).
 */
public class ClientEvents {
    // Linea de tiempo (ms)
    static final int ENTER = 900, SPIN = 1800, STEP = 350, HOLD = 1200, EXIT = 900;
    static final int CASCADE = 7 * STEP;
    static final int TOTAL = ENTER + SPIN + CASCADE + HOLD + EXIT;

    static boolean active;
    static long start;
    static int selected; // indice en RuletaColor
    static int wedge;    // gaje (en WHEEL) donde cae
    static boolean playedSpin, playedExit;

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onChat);
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onGui);
    }

    private static void onChat(ClientChatReceivedEvent e) {
        if (e.getMessage().getContents() instanceof TranslatableContents tc
            && RuletaCommand.KEY.equals(tc.getKey())
            && tc.getArgs().length > 1 && tc.getArgs()[1] instanceof Number n) {
            selected = n.intValue();
            wedge = 0;
            for (int i = 0; i < 8; i++) {
                if (WheelRenderer.WHEEL[i].ordinal() == Math.floorMod(selected, 8)) wedge = i;
            }
            start = System.currentTimeMillis();
            active = true;
            playedSpin = false;
            playedExit = false;
            play(RuletaHardcore.SND_CHAT.get());
            play(RuletaHardcore.SND_GLITCH.get());
        }
    }

    static void play(SoundEvent ev) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ev, 1.0f));
    }

    private static void onGui(RenderGuiEvent.Post e) {
        if (!active) return;
        long t = System.currentTimeMillis() - start;
        if (t >= TOTAL) {
            active = false;
            return;
        }
        GuiGraphics g = e.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        float r = Math.min(h * 0.32f, 110f);
        float cx = w / 2f, cyMid = h / 2f, cyOff = h + r + 40f;
        float cy = cyMid, glitch = 0f, rot = 0f;

        float finalRot = ((WheelRenderer.POINTER_ANGLE - (22.5f + 45f * wedge)) % 360f + 360f) % 360f + 720f;

        if (t < ENTER) {                       // sube desde abajo con glitch
            float p = t / (float) ENTER;
            float ease = 1f - (1f - p) * (1f - p) * (1f - p);
            cy = cyOff + (cyMid - cyOff) * ease;
            glitch = 1f - p;
        } else if (t < ENTER + SPIN) {         // gira y cae en el color elegido
            if (!playedSpin) { playedSpin = true; play(RuletaHardcore.SND_SPIN.get()); }
            float p = (t - ENTER) / (float) SPIN;
            float ease = 1f - (1f - p) * (1f - p) * (1f - p);
            rot = finalRot * ease;
        } else {
            rot = finalRot;
        }
        if (t >= TOTAL - EXIT) {               // baja con glitch
            if (!playedExit) { playedExit = true; play(RuletaHardcore.SND_GLITCH.get()); }
            float p = (t - (TOTAL - EXIT)) / (float) EXIT;
            cy = cyMid + (cyOff - cyMid) * p * p;
            glitch = p;
        }

        // Cascada en orden: el color elegido y luego los siguientes gajes se vuelven ese color
        int[] colors = WheelRenderer.baseColors();
        float[] flash = new float[8];
        long tc = t - (ENTER + SPIN);
        if (tc >= 0) {
            for (int k = 1; k < 8; k++) {
                int j = (wedge + k) % 8;
                long when = (long) k * STEP;
                if (tc >= when) {
                    colors[j] = com.ruletahardcore.RuletaColor.of(selected).rgb;
                    long since = tc - when;
                    if (since < 220) flash[j] = 0.7f * (1f - since / 220f);
                }
            }
        }
        WheelRenderer.draw(g, cx, cy, r, rot, colors, flash, glitch, System.currentTimeMillis() / 60, w);
    }
}
