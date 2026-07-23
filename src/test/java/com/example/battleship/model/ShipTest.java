package com.example.battleship.model;

import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.ship.ShipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipTest {

    @Test
    @DisplayName("A horizontal ship occupies consecutive columns in the same row")
    void horizontalShipOccupiesConsecutiveColumns() {
        Ship destroyer = new Ship(ShipType.DESTROYER, new Position(2, 2), Orientation.HORIZONTAL);
        assertEquals(List.of(new Position(2, 2), new Position(2, 3)), destroyer.getOccupiedPositions());
    }

    @Test
    @DisplayName("A vertical ship occupies consecutive rows in the same column")
    void verticalShipOccupiesConsecutiveRows() {
        Ship destroyer = new Ship(ShipType.DESTROYER, new Position(2, 2), Orientation.VERTICAL);
        assertEquals(List.of(new Position(2, 2), new Position(3, 2)), destroyer.getOccupiedPositions());
    }

    @Test
    @DisplayName("Width and depth swap with orientation, matching the ship's type size")
    void widthAndDepthSwapWithOrientation() {
        Ship horizontal = new Ship(ShipType.AIRCRAFT_CARRIER, new Position(0, 0), Orientation.HORIZONTAL);
        assertEquals(4, horizontal.getWidthInColumns());
        assertEquals(1, horizontal.getDepthInRows());

        Ship vertical = new Ship(ShipType.AIRCRAFT_CARRIER, new Position(0, 0), Orientation.VERTICAL);
        assertEquals(1, vertical.getWidthInColumns());
        assertEquals(4, vertical.getDepthInRows());
    }

    @Test
    @DisplayName("segmentIndexAt finds the right segment, or -1 outside the ship")
    void segmentIndexAtFindsTheRightSegment() {
        Ship destroyer = new Ship(ShipType.DESTROYER, new Position(5, 5), Orientation.HORIZONTAL);
        assertEquals(0, destroyer.segmentIndexAt(new Position(5, 5)));
        assertEquals(1, destroyer.segmentIndexAt(new Position(5, 6)));
        assertEquals(-1, destroyer.segmentIndexAt(new Position(5, 7)));
        assertEquals(-1, destroyer.segmentIndexAt(new Position(6, 5)));
    }

    @Test
    @DisplayName("A ship is only sunk once every one of its segments has been hit")
    void isSunkOnlyOnceEverySegmentIsHit() {
        Ship destroyer = new Ship(ShipType.DESTROYER, new Position(5, 5), Orientation.HORIZONTAL);
        assertFalse(destroyer.isSunk());

        destroyer.registerHit(new Position(5, 5));
        assertFalse(destroyer.isSunk());

        destroyer.registerHit(new Position(5, 6));
        assertTrue(destroyer.isSunk());
    }

    @Test
    @DisplayName("registerHit rejects a position the ship doesn't occupy")
    void registerHitRejectsAPositionTheShipDoesNotOccupy() {
        Ship frigate = new Ship(ShipType.FRIGATE, new Position(0, 0), Orientation.HORIZONTAL);
        assertThrows(IllegalArgumentException.class, () -> frigate.registerHit(new Position(1, 1)));
    }
}
