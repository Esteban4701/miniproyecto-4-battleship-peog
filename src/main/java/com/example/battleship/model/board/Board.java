package com.example.battleship.model.board;

import com.example.battleship.model.ShotResult;
import com.example.battleship.model.exception.CellAlreadyShotException;
import com.example.battleship.model.exception.InvalidShipPlacementException;
import com.example.battleship.model.exception.OutOfBoardException;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.Ship;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A 10x10 Battleship board: the grid of {@link Cell}s, the fleet placed
 * on it, and the rules for placing ships and resolving shots.
 * <p>
 * A {@code Board} is serializable on its own, satisfying HU-5's
 * requirement to save the board's state to a file and restore it later.
 * {@link BoardListener}s are intentionally <em>not</em> part of that
 * saved state (see {@link #listeners}); after loading a saved board,
 * the controller is expected to re-attach its listener before using it.
 * </p>
 */
public class Board implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Both dimensions of the board: 10x10. */
    public static final int SIZE = 10;

    private final Cell[][] cells;
    private final List<Ship> ships = new ArrayList<>();

    /**
     * Listeners are UI/controller wiring, not game state, so they are
     * excluded from serialization and re-created empty on every load
     * (see {@link #readObject}).
     */
    private transient List<BoardListener> listeners = new ArrayList<>();

    /** Creates an empty 10x10 board: every cell starts as {@link CellState#EMPTY}. */
    public Board() {
        cells = new Cell[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                cells[row][column] = new Cell(new Position(row, column));
            }
        }
    }

    /**
     * Places a ship on the board, after validating that it fits within
     * the board and doesn't overlap any ship already placed.
     *
     * @param ship the ship to place
     * @throws InvalidShipPlacementException if the ship would run off the
     *         edge of the board or overlap an existing ship
     */
    public void placeShip(Ship ship) throws InvalidShipPlacementException {
        try {
            validateWithinBounds(ship);
        } catch (OutOfBoardException e) {
            throw new InvalidShipPlacementException(
                    "Ship at " + ship.getOrigin().toLabel() + " would extend past the edge of the board", e);
        }

        List<Position> occupiedPositions = ship.getOccupiedPositions();
        for (Position position : occupiedPositions) {
            if (getCell(position).getState() == CellState.SHIP) {
                throw new InvalidShipPlacementException(
                        "Ship placement overlaps an existing ship at " + position.toLabel());
            }
        }

        for (Position position : occupiedPositions) {
            Cell cell = getCell(position);
            cell.setState(CellState.SHIP);
            cell.setOccupyingShip(ship);
        }
        ships.add(ship);

        for (BoardListener listener : listeners) {
            listener.onShipPlaced(ship);
        }
    }

    private void validateWithinBounds(Ship ship) throws OutOfBoardException {
        int size = ship.getType().getSizeInCells();
        Position origin = ship.getOrigin();
        boolean horizontal = ship.getOrientation() == Orientation.HORIZONTAL;
        int lastRow = horizontal ? origin.row() : origin.row() + size - 1;
        int lastColumn = horizontal ? origin.column() + size - 1 : origin.column();
        if (lastRow >= SIZE || lastColumn >= SIZE) {
            throw new OutOfBoardException(lastRow, lastColumn);
        }
    }

    /**
     * Resolves a shot at the given position: marks the cell, updates the
     * hit ship (if any), and notifies listeners.
     *
     * @param position the cell being shot at
     * @return the outcome of the shot
     * @throws CellAlreadyShotException if this cell has already been shot at
     */
    public ShotResult receiveShot(Position position) {
        Cell cell = getCell(position);
        if (cell.isShot()) {
            throw new CellAlreadyShotException(position);
        }

        Ship hitShip = cell.getOccupyingShip();
        ShotResult result;

        if (hitShip == null) {
            cell.setState(CellState.MISS);
            result = ShotResult.WATER;
        } else {
            hitShip.registerHit(position);
            if (hitShip.isSunk()) {
                for (Position sunkPosition : hitShip.getOccupiedPositions()) {
                    getCell(sunkPosition).setState(CellState.SUNK);
                }
                result = ShotResult.SUNK;
            } else {
                cell.setState(CellState.HIT);
                result = ShotResult.HIT;
            }
        }

        for (BoardListener listener : listeners) {
            listener.onShotResolved(position, result, hitShip);
        }
        return result;
    }

    /**
     * @param position a board position
     * @return {@code true} if that cell has not been shot at yet
     */
    public boolean canBeShotAt(Position position) {
        return !getCell(position).isShot();
    }

    /** @return {@code true} once every ship placed on this board has been sunk */
    public boolean areAllShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return !ships.isEmpty();
    }

    /**
     * @param position a board position
     * @return the cell at that position
     */
    public Cell getCell(Position position) {
        return cells[position.row()][position.column()];
    }

    /** @return an unmodifiable view of every ship placed on this board */
    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    public void addListener(BoardListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BoardListener listener) {
        listeners.remove(listener);
    }

    /**
     * Restores the transient {@link #listeners} list after
     * deserialization -- it is never part of the saved state, so it is
     * always recreated empty; the controller re-attaches itself after
     * loading.
     */
    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        listeners = new ArrayList<>();
    }
}
