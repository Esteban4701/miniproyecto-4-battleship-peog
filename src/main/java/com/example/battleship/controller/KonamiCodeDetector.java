package com.example.battleship.controller;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Watches keyboard input for the Konami Code (Up, Up, Down, Down, Left,
 * Right, Left, Right, B, A) and runs a callback once the whole
 * sequence is entered correctly, in order -- this is what activates
 * HU-3's "cheat" reveal of the machine's fleet.
 * <p>
 * A wrong key resets progress back to zero, except that the wrong key
 * itself is immediately re-checked as a possible first key of a new
 * attempt (so typing UP, UP, UP, DOWN, DOWN, ... still recovers cleanly
 * instead of requiring a fully clean restart).
 * </p>
 */
public class KonamiCodeDetector implements EventHandler<KeyEvent> {

    private static final KeyCode[] SEQUENCE = {
            KeyCode.UP, KeyCode.UP, KeyCode.DOWN, KeyCode.DOWN,
            KeyCode.LEFT, KeyCode.RIGHT, KeyCode.LEFT, KeyCode.RIGHT,
            KeyCode.B, KeyCode.A
    };

    private final Runnable onCodeEntered;
    private int progress;

    /**
     * @param onCodeEntered called once the full sequence has been typed correctly
     */
    public KonamiCodeDetector(Runnable onCodeEntered) {
        this.onCodeEntered = onCodeEntered;
    }

    @Override
    public void handle(KeyEvent event) {
        KeyCode pressed = event.getCode();

        if (pressed == SEQUENCE[progress]) {
            progress++;
            if (progress == SEQUENCE.length) {
                progress = 0;
                onCodeEntered.run();
            }
        } else {
            progress = (pressed == SEQUENCE[0]) ? 1 : 0;
        }
    }
}
