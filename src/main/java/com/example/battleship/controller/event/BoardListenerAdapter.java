package com.example.battleship.controller.event;

import com.example.battleship.model.board.BoardListener;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.player.ShotResult;

/**
 * No-op default implementation of the model's
 * {@link com.example.battleship.model.board.BoardListener}, following the
 * same <b>Adapter</b> pattern as {@link ShipPlacementAdapter} and
 * {@link CombatAdapter}. {@code CombatController} uses this to listen
 * for shots on each board without having to implement the
 * {@code onShipPlaced} callback it doesn't need during combat.
 */
public class BoardListenerAdapter implements BoardListener {

    @Override
    public void onShipPlaced(Ship ship) {
        // No-op by default: override if this callback matters to the caller.
    }

    @Override
    public void onShotResolved(Position position, ShotResult result, Ship ship) {
        // No-op by default: override if this callback matters to the caller.
    }
}
