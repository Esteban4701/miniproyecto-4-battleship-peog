package com.example.battleship.model;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Orientation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {

    @Test
    @DisplayName("Accepts coordinates anywhere within the 10x10 board")
    void acceptsCoordinatesWithinTheBoard() {
        Position position = new Position(0, 9);
        assertEquals(0, position.row());
        assertEquals(9, position.column());
    }

    @Test
    @DisplayName("Rejects negative row or column")
    void rejectsNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, -1));
    }

    @Test
    @DisplayName("Rejects row or column at or beyond the board's size")
    void rejectsCoordinatesAtOrBeyondBoardSize() {
        assertThrows(IllegalArgumentException.class, () -> new Position(Board.SIZE, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, Board.SIZE));
    }

    @Test
    @DisplayName("shift(HORIZONTAL, n) moves n columns to the right, same row")
    void shiftHorizontalMovesAlongColumns() {
        Position shifted = new Position(3, 3).shift(Orientation.HORIZONTAL, 2);
        assertEquals(new Position(3, 5), shifted);
    }

    @Test
    @DisplayName("shift(VERTICAL, n) moves n rows down, same column")
    void shiftVerticalMovesAlongRows() {
        Position shifted = new Position(3, 3).shift(Orientation.VERTICAL, 2);
        assertEquals(new Position(5, 3), shifted);
    }

    @Test
    @DisplayName("toLabel() combines the column letter with a one-based row number")
    void toLabelCombinesColumnLetterAndOneBasedRow() {
        assertEquals("A1", new Position(0, 0).toLabel());
        assertEquals("J10", new Position(9, 9).toLabel());
    }
}
