package com.example.battleship.model.player;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Position;

import java.io.Serial;

/**
 * The computer-controlled opponent (HU-4). Delegates the actual
 * targeting decision to a {@link ShootingStrategy}, so this class stays
 * unaware of whether that targeting is random or something smarter.
 */
public class MachinePlayer extends Player {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ShootingStrategy shootingStrategy;

    /**
     * @param shootingStrategy how this machine chooses where to shoot
     */
    public MachinePlayer(ShootingStrategy shootingStrategy) {
        super("Machine", new Board());
        this.shootingStrategy = shootingStrategy;
    }

    /**
     * Chooses this machine's next shot on the opponent's board, using
     * its {@link ShootingStrategy}.
     *
     * @param opponentBoard the human player's board
     * @return the chosen position
     */
    public Position chooseShot(Board opponentBoard) {
        return shootingStrategy.chooseTarget(opponentBoard);
    }
}
