package com.example.battleship.model;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.exception.InvalidShipPlacementException;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.ship.ShipFactory;
import com.example.battleship.model.ship.ShipType;

import java.util.List;
import java.util.Random;

/**
 * Places a complete fleet of ships onto a board at random valid
 * positions, following the rules (no overlaps, nothing hanging off the
 * edge). This is what gives the machine player its board per HU-4.
 */
public final class FleetPlacer {

    /** Safety cap so a pathological run of bad luck fails loudly instead of looping forever. */
    private static final int MAX_ATTEMPTS_PER_SHIP = 1000;

    private FleetPlacer() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Places one complete fleet (per {@link ShipFactory#createFullFleetTypes()})
     * onto {@code board} at random, non-overlapping, in-bounds positions.
     *
     * @param board  the board to place ships on (should be empty)
     * @param random the random source to draw positions and orientations from
     * @throws IllegalStateException if a ship could not be placed after
     *         {@value #MAX_ATTEMPTS_PER_SHIP} random attempts -- this
     *         would indicate a bug (e.g. an already-crowded board), not
     *         an expected outcome
     */
    public static void placeFleetRandomly(Board board, Random random) {
        List<ShipType> fleetTypes = ShipFactory.createFullFleetTypes();

        for (ShipType type : fleetTypes) {
            boolean placed = false;

            for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_SHIP && !placed; attempt++) {
                Orientation orientation = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                Position origin = randomOrigin(random);
                Ship ship = ShipFactory.createShip(type, origin, orientation);

                try {
                    board.placeShip(ship);
                    placed = true;
                } catch (InvalidShipPlacementException e) {
                    // Expected during random search: try another spot.
                }
            }

            if (!placed) {
                throw new IllegalStateException(
                        "Could not place a " + type + " after " + MAX_ATTEMPTS_PER_SHIP + " random attempts");
            }
        }
    }

    /** @return a uniformly random position anywhere on the board, valid or not as an origin for a given ship */
    private static Position randomOrigin(Random random) {
        return new Position(random.nextInt(Board.SIZE), random.nextInt(Board.SIZE));
    }
}
