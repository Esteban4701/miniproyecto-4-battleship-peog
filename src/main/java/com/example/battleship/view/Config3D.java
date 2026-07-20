package com.example.battleship.view;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 * Shared scale constants and reusable materials for all 3D shapes on the
 * board (water, ships and shot marks).
 * <p>
 * Centralizing these values avoids repeated "magic numbers" across
 * classes and lets the whole board's visual scale be adjusted in one
 * place.
 * </p>
 */
public final class Config3D {

    private Config3D() {
        // Utility class: not meant to be instantiated.
    }

    /** Width/depth of a single board cell, in 3D units. */
    public static final double CELL_SIZE = 40.0;

    /** Thickness of the water sheet. */
    public static final double WATER_HEIGHT = 4.0;

    // ---- Reusable state materials, shared by any ship ----

    public static PhongMaterial waterMaterial() {
        PhongMaterial m = new PhongMaterial(Color.rgb(30, 95, 165));
        m.setSpecularColor(Color.rgb(180, 210, 255));
        return m;
    }

    public static PhongMaterial intactHullMaterial(Color baseColor) {
        PhongMaterial m = new PhongMaterial(baseColor);
        m.setSpecularColor(Color.rgb(200, 200, 200));
        return m;
    }

    public static PhongMaterial hitMaterial() {
        PhongMaterial m = new PhongMaterial(Color.rgb(230, 110, 20));
        m.setSpecularColor(Color.YELLOW);
        return m;
    }

    public static PhongMaterial sunkMaterial() {
        PhongMaterial m = new PhongMaterial(Color.rgb(25, 25, 25));
        m.setSpecularColor(Color.rgb(60, 0, 0));
        return m;
    }
}
