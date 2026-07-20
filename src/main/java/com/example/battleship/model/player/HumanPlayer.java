package com.example.battleship.model.player;

import com.example.battleship.model.board.Board;

import java.io.Serial;

/**
 * The human player. Their shots come from mouse input handled by the
 * controller, so this class adds no behavior beyond {@link Player}.
 */
public class HumanPlayer extends Player {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param nickname the player's chosen nickname
     */
    public HumanPlayer(String nickname) {
        super(nickname, new Board());
    }
}
