package com.example.battleship.model.board;

import com.example.battleship.model.ship.Orientation;

import java.io.Serial;
import java.io.Serializable;

/**
 * An immutable board coordinate, expressed as a zero-based row and
 * column pair (0-9 for a 10x10 board).
 * <p>
 * Implemented as a {@code record} for value-based equality: two
 * positions with the same row and column are considered equal and
 * interchangeable, which is exactly the semantics a hash-based
 * "already shot at" lookup on {@link Board} needs.
 * </p>
 */
public record Position(int row, int column) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param row    zero-based row, must be within the board bounds
     * @param column zero-based column, must be within the board bounds
     */
    public Position {
        if (row < 0 || row >= Board.SIZE || column < 0 || column >= Board.SIZE) {
            throw new IllegalArgumentException(
                    "Position out of bounds: (" + row + ", " + column + ")");
        }
    }

    /**
     * Returns the position one cell away from this one in the given
     * orientation, used while laying out a ship's occupied cells.
     *
     * @param orientation direction to step in
     * @param steps       how many cells to move (maybe 0)
     * @return the resulting position
     */
    public Position shift(Orientation orientation, int steps) {
        return orientation == Orientation.HORIZONTAL
                ? new Position(row, column + steps)
                : new Position(row + steps, column);
    }

    /** Spreadsheet-style label for this position, e.g. {@code "C5"} (column letter, 1-based row). */
    public String toLabel() {
        char columnLetter = (char) ('A' + column);
        return "" + columnLetter + (row + 1);
    }
}
