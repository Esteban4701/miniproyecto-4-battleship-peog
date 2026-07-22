package com.example.battleship.model;

import com.example.battleship.model.board.Board;

/**
 * The outcome of resolving a single shot against a {@link Board}.
 */
public enum ShotResult {

    /** The shot landed on an empty cell; the shooter's turn ends. */
    WATER,

    /** The shot damaged part of a ship that isn't fully destroyed yet; the shooter fires again. */
    HIT,

    /** The shot destroyed the last remaining cell of a ship; the shooter fires again (unless the fleet is now fully sunk). */
    SUNK;

    /**
     * Whether this result grants the shooter another turn, per the
     * project rules: a miss passes the turn, a hit or a sunk ship does
     * not.
     *
     * @return {@code true} if the same player keeps shooting
     */
    public boolean grantsAnotherTurn() {
        return this != WATER;
    }
}
