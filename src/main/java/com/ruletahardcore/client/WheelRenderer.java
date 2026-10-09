package com.ruletahardcore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ruletahardcore.RuletaColor;
import com.ruletahardcore.RuletaHardcore;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class WheelRenderer {
    public static final ResourceLocation MARCO = new ResourceLocation(RuletaHardcore.MODID, "textures/gui/ruleta_marco.png");
    public static final ResourceLocation PUNTERO = new ResourceLocation(RuletaHardcore.MODID, "textures/gui/ruleta_puntero.png");

    /** Orden de los gajes en sentido horario desde arriba, igual que la imagen de referencia. */
    public static final RuletaColor[] WHEEL = {
        RuletaColor.MORADO, RuletaColor.AZUL, RuletaColor.CELESTE, RuletaColor.VERDE,
        RuletaColor.AMARILLO, RuletaColor.NARANJA, RuletaColor.ROJO, RuletaColor.ROSA
    };
    /** Posicion fija del puntero (grados desde arriba, horario): centro del gaje rojo. */
    public static final float POINTER_ANGLE = 292.5f;

    public static int[] baseColors() {
        int[] c = new int[8];
        for (int i = 0; i < 8; i++) c[i] = WHEEL[i].rgb;
        return c;
    }

    /** Dibuja ruleta + puntero. glitch 0..1: bandas desplazadas y barras en escala de grises (sin colores extra). */
    public static void draw(GuiGraphics g, float cx, float cy, float r, float rot, int[] colors, float[] flash,
                            float glitch, long seed, int screenW) {
        if (glitch <= 0.02f) {
            drawWheel(g, cx, cy, r, rot, colors, flash);
            drawPointer(g, cx, cy, r);
            return;
        }
        Random rnd = new Random(seed);
        int bands = 14;
        int top = (int) (cy - r - 40);
        int bh = (int) ((2 * r + 80) / bands) + 1;
        for (int b = 0; b < bands; b++) {
            int y0 = top + b * bh;
            float dx = rnd.nextFloat() < 0.55f ? (rnd.nextFloat() - 0.5f) * 2f * 45f * glitch : 0f;
            g.enableScissor(0, y0, screenW, y0 + bh);
            g.pose().pushPose();
            g.pose().translate(dx, 0, 0);
            drawWheel(g, cx, cy, r, rot, colors, flash);
            drawPointer(g, cx, cy, r);
            g.pose().popPose();
            g.disableScissor();
        }
        // barras glitch solo en gris / blanco / negro
        int n = 4 + (int) (glitch * 14);
        for (int i = 0; i < n; i++) {
            int y = (int) (cy - r - 20 + rnd.nextFloat() * (2 * r + 40));
            int h = 1 + rnd.nextInt(10);
            int x = (int) (cx - r - 40 + rnd.nextFloat() * (2 * r + 80) * 0.8f);
            int w = 20 + rnd.nextInt((int) (r * 1.5f));
            int tone = switch (rnd.nextInt(3)) { case 0 -> 0xFFFFFF; case 1 -> 0x000000; default -> 0x808080; };
            int alpha = 140 + rnd.nextInt(100);
            g.fill(x, y, x + w, y + h, (alpha << 24) | tone);
        }
        g.flush();
    }

    private static void drawWheel(GuiGraphics g, float cx, float cy, float r, float rot, int[] colors, float[] flash) {
        Matrix4f m = g.pose().last().pose();
        float wr = r * 0.82f; // los gajes quedan debajo del marco
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        for (int i = 0; i < 8; i++) {
            int rgb = colors[i];
            float f = flash == null ? 0f : flash[i];
            int cr = mix((rgb >> 16) & 255, 255, f), cg = mix((rgb >> 8) & 255, 255, f), cb = mix(rgb & 255, 255, f);
            BufferBuilder b = Tesselator.getInstance().getBuilder();
            b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            b.vertex(m, cx, cy, 0f).color(mix(cr, 255, 0.25f), mix(cg, 255, 0.25f), mix(cb, 255, 0.25f), 255).endVertex();
            int steps = 12;
            for (int s = 0; s <= steps; s++) {
                double a = Math.toRadians(rot + i * 45f + 45f * s / steps);
                b.vertex(m, cx + (float) Math.sin(a) * wr, cy - (float) Math.cos(a) * wr, 0f).color(cr, cg, cb, 255).endVertex();
            }
            BufferUploader.drawWithShader(b.end());
        }
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(cx, cy, 0);
        ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rot));
        g.blit(MARCO, (int) -r, (int) -r, (int) (2 * r), (int) (2 * r), 0f, 0f, 256, 256, 256, 256);
        ps.popPose();
    }

    private static void drawPointer(GuiGraphics g, float cx, float cy, float r) {
        double a = Math.toRadians(POINTER_ANGLE);
        float px = cx + (float) Math.sin(a) * (r * 0.98f);
        float py = cy - (float) Math.cos(a) * (r * 0.98f);
        int size = (int) (r * 0.5f);
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(px, py, 0);
        ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-67.5f)); // la punta apunta al centro
        g.blit(PUNTERO, -size / 2, -size / 2, size, size, 0f, 0f, 64, 64, 64, 64);
        ps.popPose();
    }

    public static int mix(int a, int b, float t) {
        return Math.max(0, Math.min(255, Math.round(a + (b - a) * t)));
    }
}
