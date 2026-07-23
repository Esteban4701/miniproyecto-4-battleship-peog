package com.example.battleship.model;

import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.ship.ShipFactory;
import com.example.battleship.model.ship.ShipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipFactoryTest {

    @Test
    @DisplayName("createShip uses exactly the given type, origin, and orientation")
    void createShipUsesTheGivenTypeOriginAndOrientation() {
        Ship ship = ShipFactory.createShip(ShipType.SUBMARINE, new Position(1, 1), Orientation.VERTICAL);
        assertEquals(ShipType.SUBMARINE, ship.getType());
        assertEquals(new Position(1, 1), ship.getOrigin());
        assertEquals(Orientation.VERTICAL, ship.getOrientation());
    }

    @Test
    @DisplayName("The full fleet has exactly 10 ships, in the counts the spec requires")
    void fullFleetHasExactlyTenShipsInTheRightCounts() {
        List<ShipType> fleet = ShipFactory.createFullFleetTypes();

        assertEquals(10, fleet.size());
        assertEquals(1, countOf(fleet, ShipType.AIRCRAFT_CARRIER));
        assertEquals(2, countOf(fleet, ShipType.SUBMARINE));
        assertEquals(3, countOf(fleet, ShipType.DESTROYER));
        assertEquals(4, countOf(fleet, ShipType.FRIGATE));
    }

    private long countOf(List<ShipType> fleet, ShipType type) {
        return fleet.stream().filter(candidate -> candidate == type).count();
    }
}
