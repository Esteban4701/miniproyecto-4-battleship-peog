package com.example.battleship.model.ship;

import com.example.battleship.model.board.Position;
import com.example.battleship.model.board.Board;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A ship placed on a {@link Board}: a type, a starting {@link Position},
 * an {@link Orientation}, and per-segment hit tracking.
 * <p>
 * A ship is immutable in terms of where it sits (per HU-1, "Una vez
 * colocados, los barcos no pueden ser movidos ni modificados") -- the
 * only thing that ever changes after construction is which of its
 * segments have been hit.
 * </p>
 */
public class Ship implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ShipType type;
    private final Position origin;
    private final Orientation orientation;
    private final boolean[] hitSegments;

    /**
     * @param type        the kind of ship (fixes its size)
     * @param origin      position of the ship's first segment (index 0)
     * @param orientation direction the remaining segments extend in
     */
    public Ship(ShipType type, Position origin, Orientation orientation) {
        this.type = type;
        this.origin = origin;
        this.orientation = orientation;
        this.hitSegments = new boolean[type.getSizeInCells()];
    }

    /** @return every cell this ship occupies, from its first segment to its last */
    public List<Position> getOccupiedPositions() {
        List<Position> positions = new ArrayList<>(type.getSizeInCells());
        for (int segment = 0; segment < type.getSizeInCells(); segment++) {
            positions.add(origin.shift(orientation, segment));
        }
        return positions;
    }

    /**
     * @param position a board position
     * @return the segment index (0 = the segment at {@link #getOrigin()}) that
     *         occupies {@code position}, or -1 if this ship doesn't occupy it
     */
    public int segmentIndexAt(Position position) {
        if (orientation == Orientation.HORIZONTAL) {
            if (position.row() != origin.row()) {
                return -1;
            }
            int offset = position.column() - origin.column();
            return isValidSegment(offset) ? offset : -1;
        } else {
            if (position.column() != origin.column()) {
                return -1;
            }
            int offset = position.row() - origin.row();
            return isValidSegment(offset) ? offset : -1;
        }
    }

    private boolean isValidSegment(int offset) {
        return offset >= 0 && offset < type.getSizeInCells();
    }

    /** @param position a board position
     *  @return {@code true} if this ship occupies {@code position} */
    public boolean occupies(Position position) {
        return segmentIndexAt(position) != -1;
    }

    /**
     * Marks the segment at {@code position} as hit.
     *
     * @param position a position this ship occupies
     * @throws IllegalArgumentException if this ship does not occupy {@code position}
     */
    public void registerHit(Position position) {
        int segment = segmentIndexAt(position);
        if (segment == -1) {
            throw new IllegalArgumentException(
                    "Ship at " + origin.toLabel() + " does not occupy " + position.toLabel());
        }
        hitSegments[segment] = true;
    }

    /** @return {@code true} once every segment of this ship has been hit */
    public boolean isSunk() {
        for (boolean hit : hitSegments) {
            if (!hit) {
                return false;
            }
        }
        return true;
    }

    public ShipType getType() {
        return type;
    }

    public Position getOrigin() {
        return origin;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * How many columns this ship's footprint spans: its full size when
     * {@link Orientation#HORIZONTAL}, just 1 when {@link Orientation#VERTICAL}.
     * Together with {@link #getDepthInRows()}, this is the single
     * source of truth for how big a ship's footprint is on the grid --
     * every bound/overlap check should be built from these two numbers
     * plus {@link #getOrigin()}, rather than re-deriving them separately.
     */
    public int getWidthInColumns() {
        return orientation == Orientation.HORIZONTAL ? type.getSizeInCells() : 1;
    }

    /**
     * How many rows this ship's footprint spans: its full size when
     * {@link Orientation#VERTICAL}, just 1 when {@link Orientation#HORIZONTAL}.
     * See {@link #getWidthInColumns()}.
     */
    public int getDepthInRows() {
        return orientation == Orientation.VERTICAL ? type.getSizeInCells() : 1;
    }
}