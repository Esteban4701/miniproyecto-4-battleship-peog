package com.example.battleship.controller;

/**
 * Which phase the single main screen is currently showing. Drives
 * which overlay pane {@link GameScreenController} keeps visible.
 */
public enum GamePhase {

    /** Title, nickname field, "New Game"/"Continue" buttons. */
    START_MENU,

    /** HU-1: the human player is placing their fleet. */
    PLACEMENT,

    /** HU-2/HU-4: turns alternate between the human and the machine. */
    COMBAT,

    /** Someone has won; the result overlay is showing. */
    GAME_OVER
}
