package com.example.battleship.view;

/**
 * Visual state of an individual segment (cell) of a ship.
 */
public enum SegmentState {
    /** The segment has not been hit by any shot. */
    INTACT,
    /** The segment was hit by an enemy shot. */
    HIT,
    /** The segment (and the whole ship) has been sunk. */
    SUNK
}
