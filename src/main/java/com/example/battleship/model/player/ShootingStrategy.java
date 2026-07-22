package com.example.battleship.model.player;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Position;

import java.io.Serializable;

/**
 * Decides where the machine player shoots next.
 * <p>
 * Applies the <b>Strategy</b> behavioral pattern: {@link MachinePlayer}
 * doesn't know or care <em>how</em> a target is chosen, only that it
 * can ask for one. HU-4 only requires random targeting
 * ({@link RandomShootingStrategy}), but this separation means a
 * smarter strategy (e.g. one that hunts around a previous hit) could
 * be swapped in later without changing {@code MachinePlayer} at all.
 * </p>
 * <p>
 * Extends {@link Serializable} because {@link MachinePlayer} (which
 * holds one of these) is itself serializable -- any implementation
 * must be safe to write out along with the rest of the game state.
 * </p>
 */
public interface ShootingStrategy extends Serializable {

    /**
     * Chooses the next cell to shoot at on the opponent's board.
     *
     * @param opponentBoard the board being fired upon
     * @return a position that has not been shot at yet
     */
    Position chooseTarget(Board opponentBoard);
}
