package com.example.battleship.view.ships;

import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.ShipType;

/**
 * Factory for 3D ships. Applies the <b>Factory Method</b> pattern to
 * decouple the rest of the program (controller / placement logic) from
 * the concrete classes of each shape.
 */
public final class ShipFactory3D {

    private ShipFactory3D() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Creates the 3D shape matching the requested ship type.
     *
     * @param type        ship type (defines its size in cells)
     * @param row         anchor row on the board (0-9)
     * @param column      anchor column on the board (0-9)
     * @param orientation horizontal or vertical orientation
     * @return concrete {@link Ship3D} instance ready to add to the scene
     */
    public static Ship3D create(ShipType type, int row, int column, Orientation orientation) {
        return switch (type) {
            case AIRCRAFT_CARRIER -> new AircraftCarrier3D(row, column, orientation);
            case SUBMARINE -> new Submarine3D(row, column, orientation);
            case DESTROYER -> new Destroyer3D(row, column, orientation);
            case FRIGATE -> new Frigate3D(row, column, orientation);
        };
    }
}
