package com.example.battleship.model.player;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Position;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Picks a uniformly random cell, among the ones not shot at yet, on
 * every call -- the machine-player behavior required by HU-4.
 */
public class RandomShootingStrategy implements ShootingStrategy {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Random random;

    /** Uses a new, non-seeded {@link Random}. */
    public RandomShootingStrategy() {
        this(new Random());
    }

    /**
     * @param random the random source to draw from (accepting it as a
     *               parameter, instead of always creating one, makes this
     *               class easy to test with a seeded {@code Random})
     */
    public RandomShootingStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Position chooseTarget(Board opponentBoard) {
        List<Position> candidates = new ArrayList<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Position candidate = new Position(row, column);
                if (opponentBoard.canBeShotAt(candidate)) {
                    candidates.add(candidate);
                }
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No cells left to shoot at: every cell has already been targeted");
        }
        return candidates.get(random.nextInt(candidates.size()));
    }
}
