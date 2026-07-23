package com.example.battleship.model;

import com.example.battleship.model.board.Position;
import com.example.battleship.model.exception.InvalidShipPlacementException;
import com.example.battleship.model.player.RandomShootingStrategy;
import com.example.battleship.model.player.ShotResult;
import com.example.battleship.model.player.Turn;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.ShipFactory;
import com.example.battleship.model.ship.ShipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game("Tester", new RandomShootingStrategy(new Random(42)));
    }

    @Test
    @DisplayName("A new game always starts on the human's turn")
    void startsOnTheHumansTurn() {
        assertEquals(Turn.HUMAN, game.getCurrentTurn());
    }

    @Test
    @DisplayName("A miss passes the turn to the machine")
    void aMissPassesTheTurnToTheMachine() {
        // The machine's board is empty, so any shot at it is guaranteed to miss.
        ShotResult result = game.fireAtMachine(new Position(0, 0));
        assertEquals(ShotResult.WATER, result);
        assertEquals(Turn.MACHINE, game.getCurrentTurn());
    }

    @Test
    @DisplayName("A hit (or a sink) keeps the turn with the same shooter")
    void aHitKeepsTheTurnWithTheSameShooter() throws InvalidShipPlacementException {
        game.getMachine().getOwnBoard().placeShip(
                ShipFactory.createShip(ShipType.FRIGATE, new Position(0, 0), Orientation.HORIZONTAL));

        ShotResult result = game.fireAtMachine(new Position(0, 0));
        assertEquals(ShotResult.SUNK, result); // one-cell ship: a hit sinks it immediately
        assertEquals(Turn.HUMAN, game.getCurrentTurn());
    }

    @Test
    @DisplayName("Firing out of turn throws instead of silently resolving")
    void firingOutOfTurnThrows() {
        game.fireAtMachine(new Position(1, 1)); // guaranteed miss: passes the turn to the machine
        assertThrows(IllegalStateException.class, () -> game.fireAtMachine(new Position(2, 2)));
    }

    @Test
    @DisplayName("The game ends, with the human as winner, once the machine's whole fleet is sunk")
    void gameEndsOnceOneSidesFleetIsFullySunk() throws InvalidShipPlacementException {
        game.getMachine().getOwnBoard().placeShip(
                ShipFactory.createShip(ShipType.FRIGATE, new Position(0, 0), Orientation.HORIZONTAL));
        assertFalse(game.isOver());

        game.fireAtMachine(new Position(0, 0));

        assertTrue(game.isOver());
        assertEquals(game.getHuman(), game.getWinner());
    }

    @Test
    @DisplayName("playMachineShot resolves exactly one shot and reports what it targeted")
    void playMachineShotResolvesExactlyOneShot() {
        game.fireAtMachine(new Position(0, 0)); // guaranteed miss: hands the turn to the machine

        Game.ShotOutcome outcome = game.playMachineShot();

        assertTrue(game.getHuman().getOwnBoard().getCell(outcome.position()).isShot());
    }
}
