package com.example.battleship.model.board;

/**
 * The state a single board {@link Cell} can be in.
 */
public enum CellState {

    /** Untouched water; nothing is known about this cell yet. */
    EMPTY,

    /** Occupied by part of a ship that hasn't been shot at. Only ever shown on a player's own board. */
    SHIP,

    /** Shot at, and there was no ship here. */
    MISS,

    /** Shot at, hit part of a ship, but that ship hasn't been fully sunk yet. */
    HIT,

    /** Part of a ship whose every cell has now been hit -- the whole ship is destroyed. */
    SUNK
}
