package com.example.battleship.view.ships;

import com.example.battleship.SegmentState;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.view.Config3D;
import com.example.battleship.view.assets.WreckageDebris3D;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D representation of a fleet ship, built from JavaFX {@code Shape3D}
 * subclasses ({@code Box}, {@code Cylinder}, {@code Sphere}).
 * <p>
 * Applies the <b>Template Method</b> pattern: this class handles
 * everything common to any ship (positioning on the board, orientation,
 * per-cell hit tracking), while each concrete subclass
 * ({@link Frigate3D}, {@link Destroyer3D}, {@link Submarine3D},
 * {@link AircraftCarrier3D}) only needs to implement
 * {@link #buildHull()} to define its distinctive look.
 * </p>
 * <p>
 * Important: the hull is built as a single continuous piece of geometry
 * spanning the whole length of the ship, not as one disconnected chunk
 * per occupied cell -- otherwise the ship reads visually as a row of
 * separate blocks instead of one vessel. Grid cells only matter for
 * hit-tracking logic and for where the per-cell hit marker is placed;
 * they never split the hull itself. When the whole ship is sunk, the
 * entire hull is recolored at once, so the finished "wreck" always
 * shows the full, uninterrupted ship design.
 * </p>
 */
public abstract class Ship3D extends Group {

    private final int sizeInCells;
    private final int anchorRow;
    private final int anchorColumn;
    private final Orientation orientation;

    private final Group hull;
    private final List<SegmentState> states = new ArrayList<>();
    private final List<Group> hitMarks;

    /**
     * @param sizeInCells  number of cells occupied by the ship (1 to 4)
     * @param anchorRow    board row where the ship starts (0-9)
     * @param anchorColumn board column where the ship starts (0-9)
     * @param orientation  HORIZONTAL (along columns) or VERTICAL (along rows)
     */
    protected Ship3D(int sizeInCells, int anchorRow, int anchorColumn, Orientation orientation) {
        this.sizeInCells = sizeInCells;
        this.anchorRow = anchorRow;
        this.anchorColumn = anchorColumn;
        this.orientation = orientation;

        for (int i = 0; i < sizeInCells; i++) {
            states.add(SegmentState.INTACT);
        }
        this.hitMarks = new ArrayList<>(java.util.Collections.nCopies(sizeInCells, null));

        this.hull = buildHull();
        getChildren().add(hull);
        placeOnBoard();
    }

    /**
     * Builds the ship's entire hull as one continuous group of
     * {@code Shape3D} nodes (Box, Cylinder, Sphere), spanning its full
     * length. Use {@link #cellCenterX(int)} to place distinctive
     * features (a bridge, a periscope, an island...) at a specific cell,
     * and {@link #shipCenterX()} to center pieces that span the whole
     * ship.
     *
     * @return group representing the complete hull
     */
    protected abstract Group buildHull();

    /** X offset of the center of cell {@code index} within this ship's local coordinates. */
    protected final double cellCenterX(int index) {
        return index * Config3D.CELL_SIZE;
    }

    /** X offset of the midpoint of the whole ship, useful to center a full-length hull piece. */
    protected final double shipCenterX() {
        return (sizeInCells - 1) * Config3D.CELL_SIZE / 2.0;
    }

    private void placeOnBoard() {
        setTranslateX(anchorColumn * Config3D.CELL_SIZE);
        setTranslateZ(anchorRow * Config3D.CELL_SIZE);
        if (orientation == Orientation.VERTICAL) {
            setRotationAxis(Rotate.Y_AXIS);
            setRotate(90);
        }
    }

    /**
     * Marks one cell of the ship as hit. This does not alter the hull
     * geometry or color -- it only scatters a wreckage cluster (broken
     * fragments, fire, smoke -- see {@link WreckageDebris3D}) over the
     * affected cell, the same for every ship type. If every cell ends
     * up hit, the whole ship transitions to the sunk state.
     *
     * @param segmentIndex index of the hit cell (0-based)
     */
    public void markHit(int segmentIndex) {
        validateIndex(segmentIndex);
        if (states.get(segmentIndex) == SegmentState.SUNK) {
            return;
        }
        states.set(segmentIndex, SegmentState.HIT);

        Group mark = WreckageDebris3D.create();
        mark.setTranslateX(cellCenterX(segmentIndex));
        getChildren().add(mark);
        hitMarks.set(segmentIndex, mark);

        if (isFullyHit()) {
            markSunk();
        }
    }

    /**
     * Sinks the ship: removes the individual per-cell hit markers and
     * recolors the whole hull in a single pass, so the result is always
     * the complete ship silhouette in the "sunk" material -- never a
     * grid-chopped patchwork.
     */
    private void markSunk() {
        for (int i = 0; i < sizeInCells; i++) {
            states.set(i, SegmentState.SUNK);
            Group mark = hitMarks.get(i);
            if (mark != null) {
                getChildren().remove(mark);
                hitMarks.set(i, null);
            }
        }
        applyMaterialRecursively(hull, Config3D.sunkMaterial());
    }

    /** Recursively applies a material to every {@code Shape3D} found under {@code node}. */
    private void applyMaterialRecursively(Node node, PhongMaterial material) {
        if (node instanceof Shape3D shape) {
            shape.setMaterial(material);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyMaterialRecursively(child, material);
            }
        }
    }

    private boolean isFullyHit() {
        return states.stream().allMatch(s -> s == SegmentState.HIT || s == SegmentState.SUNK);
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= sizeInCells) {
            throw new IndexOutOfBoundsException(
                    "Invalid segment index: " + index + " (ship size: " + sizeInCells + ")");
        }
    }

    public boolean isSunk() {
        return states.stream().allMatch(s -> s == SegmentState.SUNK);
    }

    public int getSizeInCells() {
        return sizeInCells;
    }

    public int getAnchorRow() {
        return anchorRow;
    }

    public int getAnchorColumn() {
        return anchorColumn;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public SegmentState getState(int segmentIndex) {
        validateIndex(segmentIndex);
        return states.get(segmentIndex);
    }
}