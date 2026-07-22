package com.example.battleship.controller.event;

import com.example.battleship.model.player.Player;
import com.example.battleship.model.player.Turn;

/**
 * No-op default implementation of {@link CombatListener}, following
 * the same <b>Adapter</b> pattern as {@link ShipPlacementAdapter}.
 */
public class CombatAdapter implements CombatListener {

    @Override
    public void onTurnChanged(Turn newTurn) {
        // No-op by default: override if this callback matters to the caller.
    }

    @Override
    public void onGameOver(Player winner) {
        // No-op by default: override if this callback matters to the caller.
    }
}
