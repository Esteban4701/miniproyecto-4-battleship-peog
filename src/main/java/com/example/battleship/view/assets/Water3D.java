package com.example.battleship.view.assets;

import com.example.battleship.view.Config3D;

import javafx.scene.shape.Box;

/**
 * Represents a single water cell of the board (an empty cell with no
 * ship). It is a thin flat slab; the whole board is built by repeating
 * this shape across a 10x10 grid.
 * <p>
 * Keeps its own row/column so that mouse picking (used for the hover
 * highlight) can identify which cell was hit without having to reverse
 * the math from a 3D position.
 * </p>
 */
public class Water3D extends Box {

    private final int row;
    private final int column;

    public Water3D(int row, int column) {
        super(Config3D.CELL_SIZE - 1, Config3D.WATER_HEIGHT, Config3D.CELL_SIZE - 1);
        this.row = row;
        this.column = column;
        setMaterial(Config3D.waterMaterial());
        setTranslateX(column * Config3D.CELL_SIZE);
        setTranslateZ(row * Config3D.CELL_SIZE);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}