package com.example.battleship.model;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.exception.InvalidShipPlacementException;
import com.example.battleship.model.player.HuntShootingStrategy;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.ShipFactory;
import com.example.battleship.model.ship.ShipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntShootingStrategyTest {

    private Board board;
    private HuntShootingStrategy strategy;

    @BeforeEach
    void setUp() {
        board = new Board();
        strategy = new HuntShootingStrategy(new Random(1));
    }

    @Test
    @DisplayName("With no active hits, it behaves like plain random targeting")
    void behavesRandomlyWithNoHitsYet() {
        Position target = strategy.chooseTarget(board);
        assertTrue(board.canBeShotAt(target));
    }

    @Test
    @DisplayName("A lone hit makes only its four orthogonal neighbors valid targets")
    void probesAllFourNeighborsOfALoneHit() throws InvalidShipPlacementException {
        board.placeShip(ShipFactory.createShip(ShipType.SUBMARINE, new Position(5, 5), Orientation.HORIZONTAL));
        board.receiveShot(new Position(5, 5)); // one hit; the 3-cell submarine isn't sunk yet

        for (int i = 0; i < 50; i++) {
            Position target = strategy.chooseTarget(board);
            assertTrue(isOneOf(target, new Position(4, 5), new Position(6, 5), new Position(5, 4), new Position(5, 6)),
                    "Expected an orthogonal neighbor of (5,5), got " + target);
        }
    }

    @Test
    @DisplayName("Two aligned hits narrow targeting down to the two ends of that line")
    void onlyTargetsLineExtensionsOnceTwoHitsAreAligned() throws InvalidShipPlacementException {
        board.placeShip(ShipFactory.createShip(ShipType.SUBMARINE, new Position(5, 5), Orientation.HORIZONTAL));
        board.receiveShot(new Position(5, 5));
        board.receiveShot(new Position(5, 6)); // two hits in a row; the ship still isn't sunk (size 3)

        for (int i = 0; i < 50; i++) {
            Position target = strategy.chooseTarget(board);
            assertTrue(isOneOf(target, new Position(5, 4), new Position(5, 7)),
                    "Expected a line extension of the hit row, got " + target);
        }
    }

    @Test
    @DisplayName("Once every ship is sunk, it falls back to plain random targeting")
    void fallsBackToRandomOnceEverythingIsSunk() throws InvalidShipPlacementException {
        board.placeShip(ShipFactory.createShip(ShipType.FRIGATE, new Position(0, 0), Orientation.HORIZONTAL));
        board.receiveShot(new Position(0, 0)); // sinks the one-cell ship: nothing left to hunt

        Position target = strategy.chooseTarget(board);
        assertTrue(board.canBeShotAt(target));
    }

    private boolean isOneOf(Position target, Position... candidates) {
        for (Position candidate : candidates) {
            if (target.equals(candidate)) {
                return true;
            }
        }
        return false;
    }
}
