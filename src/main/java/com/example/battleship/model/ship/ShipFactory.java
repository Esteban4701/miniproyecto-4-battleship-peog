package com.example.battleship.model.ship;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Ship} instances and the list of ship types a complete
 * fleet is made of.
 * <p>
 * Applies the <b>Factory Method</b> creational pattern: callers ask for
 * a ship by type/position/orientation, or for "the whole fleet's
 * types", without needing to know the counts and sizes from the rules
 * (1 aircraft carrier, 2 submarines, 3 destroyers, 4 frigates) --
 * that knowledge lives in {@link ShipType} and here, in one place.
 * </p>
 */
public final class ShipFactory {

    private ShipFactory() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Creates a single ship of the given type at the given position and
     * orientation. This does not check the ship against a board -- use
     * {@link Board#placeShip(Ship)} for that.
     *
     * @param type        the kind of ship to create
     * @param origin      position of the ship's first segment
     * @param orientation direction the ship extends in
     * @return the new ship
     */
    public static Ship createShip(ShipType type, Position origin, Orientation orientation) {
        return new Ship(type, origin, orientation);
    }

    /**
     * The type of every ship a complete fleet must contain, in a
     * sensible placement order (largest first) -- one entry per ship,
     * so the list has {@value ShipType#TOTAL_FLEET_CELLS} total cells'
     * worth of ships across {@code AIRCRAFT_CARRIER.getCountPerFleet()
     * + SUBMARINE.getCountPerFleet() + ...} entries.
     *
     * @return the ordered list of ship types for one complete fleet
     */
    public static List<ShipType> createFullFleetTypes() {
        List<ShipType> fleet = new ArrayList<>();
        for (ShipType type : ShipType.values()) {
            for (int i = 0; i < type.getCountPerFleet(); i++) {
                fleet.add(type);
            }
        }
        return fleet;
    }
}
