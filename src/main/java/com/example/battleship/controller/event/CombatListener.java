package com.example.battleship.controller.event;

import com.example.battleship.model.player.Player;
import com.example.battleship.model.player.Turn;

/**
 * Receives notifications about the combat phase (HU-2, HU-4): whose
 * turn it is, and when the game ends.
 */
public interface CombatListener {

    /**
     * Called whenever the turn passes from one player to the other.
     *
     * @param newTurn who the turn now belongs to
     */
    void onTurnChanged(Turn newTurn);

    /**
     * Called once a player's whole fleet has been sunk.
     *
     * @param winner the player who won
     */
    void onGameOver(Player winner);
}
