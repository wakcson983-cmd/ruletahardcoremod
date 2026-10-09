package com.ruletahardcore.client;

import com.ruletahardcore.RuletaColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RuletaScreen extends Screen {
    private static int selected = 0;
    private static String lastTitle = "ATENCION";
    private static String lastText = "";

    private EditBox titleBox, textBox;
    private int sx0, sy0; // origen de los cuadros de color
    private static final int SQ = 26, GAP = 6;

    public RuletaScreen() {
        super(Component.literal("Ruleta Hardcore"));
    }

    @Override
    protected void init() {
        int px = (int) (width * 0.52f);
        sx0 = px;
        sy0 = height / 2 - 78;
        titleBox = new EditBox(font, px, height / 2 - 20, 190, 20, Component.literal("Titulo"));
        titleBox.setMaxLength(30);
        titleBox.setValue(lastTitle);
        addRenderableWidget(titleBox);
        textBox = new EditBox(font, px, height / 2 + 20, 190, 20, Component.literal("Texto"));
        textBox.setMaxLength(140);
        textBox.setValue(lastText);
        addRenderableWidget(textBox);
        addRenderableWidget(Button.builder(Component.literal("GIRAR"), b -> spin())
            .bounds(px, height / 2 + 52, 190, 22).build());
    }

    private void spin() {
        lastTitle = titleBox.getValue();
        lastText = textBox.getValue();
        String t = lastTitle.replace('|', '/').replace('§', ' ');
        String x = lastText.replace('|', '/').replace('§', ' ');
        if (x.isBlank()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendCommand("ruletahardcore " + selected + " " + t + "|" + x);
        }
        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        float r = Math.min(height * 0.3f, 90f);
        WheelRenderer.draw(g, width * 0.27f, height / 2f, r, 0f, WheelRenderer.baseColors(), null, 0f, 0L, width);
        g.drawString(font, "Color que va a salir:", sx0, sy0 - 12, 0xFFFFFF);
        for (int i = 0; i < 8; i++) {
            int x = sx0 + (i % 4) * (SQ + GAP + 18);
            int y = sy0 + (i / 4) * (SQ + GAP);
            RuletaColor c = RuletaColor.of(i);
            if (i == selected) g.fill(x - 2, y - 2, x + SQ + 2, y + SQ + 2, 0xFFFFFFFF);
            g.fill(x, y, x + SQ, y + SQ, 0xFF000000 | c.rgb);
        }
        g.drawString(font, RuletaColor.of(selected).nombre, sx0, sy0 + 2 * (SQ + GAP) - 2, 0xFF000000 | RuletaColor.of(selected).rgb);
        g.drawString(font, "Titulo", titleBox.getX(), titleBox.getY() - 10, 0xAAAAAA);
        g.drawString(font, "Texto del chat", textBox.getX(), textBox.getY() - 10, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = 0; i < 8; i++) {
            int x = sx0 + (i % 4) * (SQ + GAP + 18);
            int y = sy0 + (i / 4) * (SQ + GAP);
            if (mx >= x && mx <= x + SQ && my >= y && my <= y + SQ) {
                selected = i;
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
