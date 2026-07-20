package com.example.battleship.model.board;

import com.example.battleship.model.ShotResult;
import com.example.battleship.model.ship.Ship;

/**
 * Receives notifications about changes to a {@link Board}.
 * <p>
 * Applies the <b>Observer</b> behavioral pattern: {@code Board} (the
 * subject) knows nothing about who is listening or how they react --
 * it just reports what happened. This is what keeps the model
 * decoupled from the view/controller in the MVC architecture: the
 * controller registers itself as a listener and updates the 3D scene
 * in response, but {@code Board} never imports or depends on any
 * JavaFX type.
 * </p>
 */
public interface BoardListener {

    /**
     * Called right after a ship has been successfully placed on the board.
     *
     * @param ship the ship that was placed
     */
    void onShipPlaced(Ship ship);

    /**
     * Called right after a shot has been resolved.
     *
     * @param position the cell that was shot at
     * @param result   the outcome of the shot
     * @param ship     the ship that was hit, or {@code null} if the result was {@link ShotResult#WATER}
     */
    void onShotResolved(Position position, ShotResult result, Ship ship);
}
