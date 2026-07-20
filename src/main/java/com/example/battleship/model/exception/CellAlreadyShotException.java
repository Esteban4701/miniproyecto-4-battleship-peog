package com.example.battleship.model.exception;

import com.example.battleship.model.board.Position;

/**
 * Thrown when a shot targets a cell that has already been shot at
 * (miss, hit, or sunk).
 * <p>
 * This is an <b>unchecked</b> exception on purpose: per HU-2, the
 * interface itself must prevent the player from ever selecting an
 * already-resolved cell (e.g. by disabling it), so reaching this state
 * signals a bug in the calling code rather than a normal, expected
 * outcome the caller should be forced to handle everywhere.
 * </p>
 */
public class CellAlreadyShotException extends RuntimeException {

    private final Position position;

    /**
     * @param position the cell that was already shot at
     */
    public CellAlreadyShotException(Position position) {
        super("Cell " + position.toLabel() + " has already been shot at");
        this.position = position;
    }

    /** @return the cell that was already shot at */
    public Position getPosition() {
        return position;
    }
}
