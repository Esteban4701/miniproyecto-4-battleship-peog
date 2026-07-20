package com.example.battleship.model.exception;

/**
 * Thrown when a ship cannot be placed where requested -- either it
 * would overlap another ship already on the board, or part of it would
 * fall outside the board's bounds.
 * <p>
 * This is a <b>checked</b> exception on purpose: placement can fail for
 * ordinary, expected reasons while the player is still setting up their
 * fleet (HU-1), and the caller (the placement controller) is expected
 * to catch it and prompt the player to try a different spot, rather
 * than let it propagate as a programming error.
 * </p>
 */
public class InvalidShipPlacementException extends Exception {

    /**
     * @param message human-readable explanation of why the placement was rejected
     */
    public InvalidShipPlacementException(String message) {
        super(message);
    }

    /**
     * @param message human-readable explanation of why the placement was rejected
     * @param cause   the lower-level cause, if any (e.g. an {@link OutOfBoardException})
     */
    public InvalidShipPlacementException(String message, Throwable cause) {
        super(message, cause);
    }
}
