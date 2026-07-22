package com.example.battleship.controller.event;

import com.example.battleship.model.ship.Ship;
import com.example.battleship.view.ships.Ship3D;

/**
 * No-op default implementation of {@link ShipPlacementListener}.
 * <p>
 * Applies the classic <b>Adapter</b> pattern for multi-method listener
 * interfaces (the same idea as AWT's {@code MouseAdapter}): a caller
 * that only cares about one callback can extend this class and
 * override just that one method, instead of being forced to implement
 * every method of the interface -- most of which would just be empty
 * bodies anyway.
 * </p>
 */
public class ShipPlacementAdapter implements ShipPlacementListener {

    @Override
    public void onShipPlaced(Ship ship, Ship3D ship3D) {
        // No-op by default: override if this callback matters to the caller.
    }

    @Override
    public void onPlacementRejected(String reason) {
        // No-op by default: override if this callback matters to the caller.
    }

    @Override
    public void onFleetPlacementComplete() {
        // No-op by default: override if this callback matters to the caller.
    }
}
