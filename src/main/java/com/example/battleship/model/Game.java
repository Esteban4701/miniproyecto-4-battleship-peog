package com.example.battleship.model;

import com.example.battleship.model.board.Position;
import com.example.battleship.model.player.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Orchestrates one full match: the two players, whose turn it is, and
 * the rule that a hit or a sunk ship grants another shot while a miss
 * passes the turn (HU-2).
 * <p>
 * Deliberately does not loop through consecutive machine shots by
 * itself -- {@link #playMachineShot()} resolves exactly one shot and
 * returns. This keeps the model free of any notion of timing or
 * animation; the controller decides when (and with what delay between
 * shots) to call it again while {@link #getCurrentTurn()} is
 * {@link Turn#MACHINE}.
 * </p>
 */
public class Game implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HumanPlayer human;
    private final MachinePlayer machine;
    private Turn currentTurn;

    /**
     * @param nickname         the human player's chosen nickname
     * @param shootingStrategy how the machine chooses its targets
     */
    public Game(String nickname, ShootingStrategy shootingStrategy) {
        this.human = new HumanPlayer(nickname);
        this.machine = new MachinePlayer(shootingStrategy);
        this.currentTurn = Turn.HUMAN;
    }

    /**
     * Resolves a shot fired by the human player at the machine's board.
     *
     * @param target the cell being shot at
     * @return the outcome of the shot
     * @throws IllegalStateException if it isn't currently the human's turn
     */
    public ShotResult fireAtMachine(Position target) {
        requireTurn(Turn.HUMAN);
        ShotResult result = machine.getOwnBoard().receiveShot(target);
        advanceTurnIfNeeded(result);
        return result;
    }

    /**
     * Resolves exactly one shot fired by the machine at the human's
     * board, choosing the target via its {@link ShootingStrategy}.
     *
     * @return the position that was targeted and the outcome of that shot
     * @throws IllegalStateException if it isn't currently the machine's turn
     */
    public ShotOutcome playMachineShot() {
        requireTurn(Turn.MACHINE);
        Position target = machine.chooseShot(human.getOwnBoard());
        ShotResult result = human.getOwnBoard().receiveShot(target);
        advanceTurnIfNeeded(result);
        return new ShotOutcome(target, result);
    }

    private void requireTurn(Turn expected) {
        if (currentTurn != expected) {
            throw new IllegalStateException("It is not " + expected + "'s turn (current turn: " + currentTurn + ")");
        }
    }

    private void advanceTurnIfNeeded(ShotResult result) {
        if (!result.grantsAnotherTurn()) {
            currentTurn = currentTurn == Turn.HUMAN ? Turn.MACHINE : Turn.HUMAN;
        }
    }

    /** @return {@code true} once either player's whole fleet has been sunk */
    public boolean isOver() {
        return human.hasLost() || machine.hasLost();
    }

    /** @return the winning player, or {@code null} if the game isn't over yet */
    public Player getWinner() {
        if (machine.hasLost()) {
            return human;
        }
        if (human.hasLost()) {
            return machine;
        }
        return null;
    }

    public HumanPlayer getHuman() {
        return human;
    }

    public MachinePlayer getMachine() {
        return machine;
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }

    /**
     * The position targeted and the result of one resolved shot,
     * returned by {@link #playMachineShot()} since -- unlike
     * {@link #fireAtMachine(Position)} -- the caller doesn't already
     * know which position the machine chose.
     *
     * @param position the cell that was shot at
     * @param result   the outcome of that shot
     */
    public record ShotOutcome(Position position, ShotResult result) {
    }
}
