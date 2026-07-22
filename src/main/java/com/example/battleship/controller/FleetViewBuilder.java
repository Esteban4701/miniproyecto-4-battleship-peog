package com.example.battleship.controller;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.view.ships.Ship3D;
import com.example.battleship.view.ships.ShipFactory3D;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the {@link Ship3D} shapes matching every {@link Ship} already
 * placed on a model {@link Board} -- used for the machine's fleet
 * (placed all at once by {@code FleetPlacer}, then mirrored into the
 * scene in one pass) and reusable anywhere else a board's ships need a
 * matching set of 3D shapes.
 */
public final class FleetViewBuilder {

    private FleetViewBuilder() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * @param board a board whose ships are already placed
     * @return a map from each model ship to its matching 3D shape, in
     *         placement order
     */
    public static Map<Ship, Ship3D> buildViews(Board board) {
        Map<Ship, Ship3D> views = new LinkedHashMap<>();
        for (Ship ship : board.getShips()) {
            Ship3D ship3D = ShipFactory3D.create(
                    ship.getType(), ship.getOrigin().row(), ship.getOrigin().column(), ship.getOrientation());
            views.put(ship, ship3D);
        }
        return views;
    }
}
