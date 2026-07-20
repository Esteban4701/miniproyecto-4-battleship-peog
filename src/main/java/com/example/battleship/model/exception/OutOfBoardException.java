package com.example.battleship.model.exception;

/**
 * Thrown when a requested row/column falls outside the board (0-9 for
 * a 10x10 board), before a valid {@code Position} can even be built.
 * <p>
 * This is a <b>checked</b> exception: it represents an ordinary,
 * recoverable input problem (e.g. a ship placement that would run off
 * the edge of the board) rather than a programming bug, so callers are
 * required to handle it explicitly.
 * </p>
 */
public class OutOfBoardException extends Exception {

    private final int row;
    private final int column;

    /**
     * @param row    the offending row
     * @param column the offending column
     */
    public OutOfBoardException(int row, int column) {
        super("Position (" + row + ", " + column + ") is outside the board");
        this.row = row;
        this.column = column;
    }

    /** @return the row that was out of bounds */
    public int getRow() {
        return row;
    }

    /** @return the column that was out of bounds */
    public int getColumn() {
        return column;
    }
}
