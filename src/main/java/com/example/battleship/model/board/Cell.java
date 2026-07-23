package com.example.battleship.model.board;

import com.example.battleship.model.ship.Ship;

import java.io.Serial;
import java.io.Serializable;

/**
 * A single cell of a {@link Board}: its current {@link CellState}, and
 * -- if a ship occupies it -- a reference to that ship, used to look up
 * hit/sunk information without scanning the whole fleet.
 */
public class Cell implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Position position;
    private CellState state;
    private Ship occupyingShip;

    /**
     * @param position this cell's board position
     */
    public Cell(Position position) {
        this.position = position;
        this.state = CellState.EMPTY;
    }

    /** @return this cell's fixed position on the board */
    public Position getPosition() {
        return position;
    }

    /** @return this cell's current state (empty, occupied by a ship, missed, hit, or sunk) */
    public CellState getState() {
        return state;
    }

    /**
     * Updates this cell's state. Package-private: only {@link Board}
     * mutates a cell, as part of placing a ship or resolving a shot --
     * every other caller only ever reads {@link #getState()}.
     *
     * @param state the new state for this cell
     */
    void setState(CellState state) {
        this.state = state;
    }

    /** @return the ship occupying this cell, or {@code null} if there isn't one */
    public Ship getOccupyingShip() {
        return occupyingShip;
    }

    /**
     * Records which ship occupies this cell. Package-private: only
     * {@link Board} sets this, while placing a ship -- every other
     * caller only ever reads {@link #getOccupyingShip()}.
     *
     * @param ship the ship now occupying this cell
     */
    void setOccupyingShip(Ship ship) {
        this.occupyingShip = ship;
    }

    /** @return {@code true} if this cell has already been the target of a shot */
    public boolean isShot() {
        return state == CellState.MISS || state == CellState.HIT || state == CellState.SUNK;
    }
}
