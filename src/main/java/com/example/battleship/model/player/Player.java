package com.example.battleship.model.player;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.ship.Ship;

import java.io.Serial;
import java.io.Serializable;

/**
 * A participant in a game: a nickname and the {@link Board} holding
 * their own fleet (the board the <em>opponent</em> shoots at).
 * <p>
 * Kept abstract so {@link HumanPlayer} and {@link MachinePlayer} can
 * each add exactly what makes them different -- a machine additionally
 * needs a {@link ShootingStrategy} to decide where to fire, while a
 * human doesn't need any extra state at all, since their shots come
 * straight from mouse clicks handled by the controller.
 * </p>
 */
public abstract class Player implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String nickname;
    private final Board ownBoard;

    /**
     * @param nickname display name for this player
     * @param ownBoard the board holding this player's own fleet
     */
    protected Player(String nickname, Board ownBoard) {
        this.nickname = nickname;
        this.ownBoard = ownBoard;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /** @return the board holding this player's own fleet (shot at by the opponent) */
    public Board getOwnBoard() {
        return ownBoard;
    }

    /** @return how many of this player's own ships have been sunk by the opponent */
    public int countSunkShips() {
        int sunkCount = 0;
        for (Ship ship : ownBoard.getShips()) {
            if (ship.isSunk()) {
                sunkCount++;
            }
        }
        return sunkCount;
    }

    /** @return {@code true} once this player's entire fleet has been sunk */
    public boolean hasLost() {
        return ownBoard.areAllShipsSunk();
    }
}
