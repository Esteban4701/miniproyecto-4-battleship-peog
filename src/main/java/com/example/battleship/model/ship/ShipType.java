package com.example.battleship.model.ship;

/**
 * The four kinds of ship in a fleet, each with a fixed size (number of
 * cells it occupies) and a fixed count per player, as specified by the
 * project statement:
 * <ul>
 *   <li>1 aircraft carrier, 4 cells</li>
 *   <li>2 submarines, 3 cells each</li>
 *   <li>3 destroyers, 2 cells each</li>
 *   <li>4 frigates, 1 cell each</li>
 * </ul>
 * A full fleet therefore occupies {@value #TOTAL_FLEET_CELLS} cells in total.
 */
public enum ShipType {

    AIRCRAFT_CARRIER(4, 1),
    SUBMARINE(3, 2),
    DESTROYER(2, 3),
    FRIGATE(1, 4);

    /** Total number of cells occupied by a complete fleet (one of each required ship). */
    public static final int TOTAL_FLEET_CELLS = 20;

    private final int sizeInCells;
    private final int countPerFleet;

    ShipType(int sizeInCells, int countPerFleet) {
        this.sizeInCells = sizeInCells;
        this.countPerFleet = countPerFleet;
    }

    /** @return how many cells a ship of this type occupies */
    public int getSizeInCells() {
        return sizeInCells;
    }

    /** @return how many ships of this type make up one player's fleet */
    public int getCountPerFleet() {
        return countPerFleet;
    }
}
