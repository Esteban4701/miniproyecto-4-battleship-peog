package com.example.battleship.view.ships;

/**
 * Fleet ship types, with their size (number of cells) and the quantity
 * each player gets, per the mini-project specification.
 */
public enum ShipType {
    AIRCRAFT_CARRIER(4, 1),
    SUBMARINE(3, 2),
    DESTROYER(2, 3),
    FRIGATE(1, 4);

    private final int sizeInCells;
    private final int countPerPlayer;

    ShipType(int sizeInCells, int countPerPlayer) {
        this.sizeInCells = sizeInCells;
        this.countPerPlayer = countPerPlayer;
    }

    public int getSizeInCells() {
        return sizeInCells;
    }

    public int getCountPerPlayer() {
        return countPerPlayer;
    }
}
