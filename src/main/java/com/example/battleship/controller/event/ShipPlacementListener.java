package com.example.battleship.controller.event;

import com.example.battleship.model.ship.Ship;
import com.example.battleship.view.ships.Ship3D;

/**
 * Receives notifications about the fleet-placement phase (HU-1).
 * <p>
 * Applies the <b>Observer</b> pattern at the controller layer, the same
 * way {@code com.example.battleship.model.BoardListener} does at the
 * model layer: {@code ShipPlacementController} doesn't know or care who
 * is listening, it just reports what happened. This is what lets
 * {@code MainGameController} react to "the fleet is fully placed" by
 * starting the combat phase, without {@code ShipPlacementController}
 * needing to know anything about combat.
 * </p>
 */
public interface ShipPlacementListener {

    /**
     * Called right after one ship has been successfully placed.
     *
     * @param ship   the model ship that was placed
     * @param ship3D the matching 3D shape added to the scene
     */
    void onShipPlaced(Ship ship, Ship3D ship3D);

    /**
     * Called when an attempted placement was rejected (overlap, out of
     * bounds, etc.) -- useful for showing feedback to the player.
     *
     * @param reason human-readable explanation of the rejection
     */
    void onPlacementRejected(String reason);

    /** Called once every ship in the fleet has been placed. */
    void onFleetPlacementComplete();
}
