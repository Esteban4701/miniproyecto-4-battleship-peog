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

    /** 1 per fleet, 4 cells. */
    AIRCRAFT_CARRIER(4, 1),

    /** 2 per fleet, 3 cells each. */
    SUBMARINE(3, 2),

    /** 3 per fleet, 2 cells each. */
    DESTROYER(2, 3),

    /** 4 per fleet, 1 cell each. */
    FRIGATE(1, 4);

    /** Total number of cells occupied by a complete fleet (one of each required ship). */
    public static final int TOTAL_FLEET_CELLS = 20;

    private final int sizeInCells;
    private final int countPerFleet;

    /**
     * @param sizeInCells   how many cells one ship of this type occupies
     * @param countPerFleet how many ships of this type a full fleet includes
     */
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
