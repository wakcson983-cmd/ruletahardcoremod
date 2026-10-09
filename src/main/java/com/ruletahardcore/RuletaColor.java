package com.ruletahardcore;

/** Orden del menu: ROJO AZUL AMARILLO MORADO ROSA NARANJA VERDE CELESTE */
public enum RuletaColor {
    ROJO("Rojo", 0xFF3B30),
    AZUL("Azul", 0x3B6BFF),
    AMARILLO("Amarillo", 0xFFD60A),
    MORADO("Morado", 0xA64DFF),
    ROSA("Rosa", 0xFF4DDB),
    NARANJA("Naranja", 0xFF9500),
    VERDE("Verde", 0x34D13A),
    CELESTE("Azul celeste", 0x2EE6FF);

    public final String nombre;
    public final int rgb;

    RuletaColor(String nombre, int rgb) {
        this.nombre = nombre;
        this.rgb = rgb;
    }

    public static RuletaColor of(int i) {
        RuletaColor[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
