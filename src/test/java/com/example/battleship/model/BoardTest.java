package com.example.battleship.model;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.BoardListener;
import com.example.battleship.model.board.CellState;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.exception.CellAlreadyShotException;
import com.example.battleship.model.exception.InvalidShipPlacementException;
import com.example.battleship.model.player.ShotResult;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.ship.ShipFactory;
import com.example.battleship.model.ship.ShipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("A ship fully inside the board can be placed")
    void placesShipWithinBounds() throws InvalidShipPlacementException {
        Ship frigate = ShipFactory.createShip(ShipType.FRIGATE, new Position(0, 0), Orientation.HORIZONTAL);
        board.placeShip(frigate);

        assertEquals(1, board.getShips().size());
        assertEquals(CellState.SHIP, board.getCell(new Position(0, 0)).getState());
    }

    @Test
    @DisplayName("A ship that would run off the edge of the board is rejected")
    void rejectsShipOutOfBounds() {
        Ship carrier = ShipFactory.createShip(ShipType.AIRCRAFT_CARRIER, new Position(0, 7), Orientation.HORIZONTAL);
        assertThrows(InvalidShipPlacementException.class, () -> board.placeShip(carrier));
    }

    @Test
    @DisplayName("A ship overlapping an already-placed ship is rejected")
    void rejectsOverlappingShip() throws InvalidShipPlacementException {
        board.placeShip(ShipFactory.createShip(ShipType.DESTROYER, new Position(0, 0), Orientation.HORIZONTAL));

        Ship overlapping = ShipFactory.createShip(ShipType.FRIGATE, new Position(0, 1), Orientation.HORIZONTAL);
        assertThrows(InvalidShipPlacementException.class, () -> board.placeShip(overlapping));
    }

    @Test
    @DisplayName("canPlaceShip reports validity without throwing or placing anything")
    void canPlaceShipDoesNotMutateTheBoard() {
        Ship outOfBounds = ShipFactory.createShip(ShipType.AIRCRAFT_CARRIER, new Position(0, 7), Orientation.HORIZONTAL);
        assertFalse(board.canPlaceShip(outOfBounds));

        Ship valid = ShipFactory.createShip(ShipType.FRIGATE, new Position(5, 5), Orientation.HORIZONTAL);
        assertTrue(board.canPlaceShip(valid));

        assertTrue(board.getShips().isEmpty(), "canPlaceShip must not actually place anything");
    }

    @Test
    @DisplayName("A shot on an empty cell is a miss")
    void shotOnEmptyCellIsWater() {
        assertEquals(ShotResult.WATER, board.receiveShot(new Position(3, 3)));
    }

    @Test
    @DisplayName("A single-cell ship is sunk by its one and only hit")
    void oneCellShipSinksOnFirstHit() throws InvalidShipPlacementException {
        board.placeShip(ShipFactory.createShip(ShipType.FRIGATE, new Position(2, 2), Orientation.HORIZONTAL));
        assertEquals(ShotResult.SUNK, board.receiveShot(new Position(2, 2)));
    }

    @Test
    @DisplayName("A multi-cell ship stays HIT until every cell has been shot, then SUNK")
    void multiCellShipSinksOnlyOnLastHit() throws InvalidShipPlacementException {
        board.placeShip(ShipFactory.createShip(ShipType.DESTROYER, new Position(4, 4), Orientation.HORIZONTAL));

        assertEquals(ShotResult.HIT, board.receiveShot(new Position(4, 4)));
        assertEquals(ShotResult.SUNK, board.receiveShot(new Position(4, 5)));
    }

    @Test
    @DisplayName("Shooting an already-shot cell throws instead of resolving again")
    void rejectsShootingTheSameCellTwice() {
        board.receiveShot(new Position(1, 1));
        assertThrows(CellAlreadyShotException.class, () -> board.receiveShot(new Position(1, 1)));
    }

    @Test
    @DisplayName("areAllShipsSunk is false with nothing placed, and true only once every placed ship is sunk")
    void areAllShipsSunkTracksEveryShip() throws InvalidShipPlacementException {
        assertFalse(board.areAllShipsSunk());

        board.placeShip(ShipFactory.createShip(ShipType.FRIGATE, new Position(0, 0), Orientation.HORIZONTAL));
        board.placeShip(ShipFactory.createShip(ShipType.FRIGATE, new Position(5, 5), Orientation.HORIZONTAL));
        assertFalse(board.areAllShipsSunk());

        board.receiveShot(new Position(0, 0));
        assertFalse(board.areAllShipsSunk());

        board.receiveShot(new Position(5, 5));
        assertTrue(board.areAllShipsSunk());
    }

    @Test
    @DisplayName("Placing a ship and resolving a shot both notify registered listeners")
    void notifiesListenersOfPlacementAndShots() throws InvalidShipPlacementException {
        boolean[] shipPlacedFired = {false};
        boolean[] shotResolvedFired = {false};

        board.addListener(new BoardListener() {
            @Override
            public void onShipPlaced(Ship ship) {
                shipPlacedFired[0] = true;
            }

            @Override
            public void onShotResolved(Position position, ShotResult result, Ship ship) {
                shotResolvedFired[0] = true;
            }
        });

        board.placeShip(ShipFactory.createShip(ShipType.FRIGATE, new Position(0, 0), Orientation.HORIZONTAL));
        assertTrue(shipPlacedFired[0]);

        board.receiveShot(new Position(9, 9));
        assertTrue(shotResolvedFired[0]);
    }
}
