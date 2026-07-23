package com.example.battleship.controller;

import com.example.battleship.controller.event.CombatListener;
import com.example.battleship.controller.event.ShipPlacementAdapter;
import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.Cell;
import com.example.battleship.model.FleetPlacer;
import com.example.battleship.model.Game;
import com.example.battleship.model.player.HuntShootingStrategy;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.view.BattlefieldView3D;
import com.example.battleship.view.assets.ShotMark3D;
import com.example.battleship.view.ships.Ship3D;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

import java.util.Map;
import java.util.Random;

/**
 * The single entry point that wires the whole game together. Owns the
 * {@link Game} model and the {@link BattlefieldView3D}, and hands off
 * input handling between the two phase-specific controllers --
 * {@link ShipPlacementController} first, then {@link CombatController}
 * once the fleet is fully placed -- rather than doing either job
 * itself. This keeps each controller focused on one HU and free to be
 * tested or reused on its own.
 * <p>
 * Has two entry points, matching the two ways a match can begin:
 * {@link #start()} for a brand-new game (HU-1: fleet placement first),
 * and {@link #resume()} for a game reloaded from disk (HU-5: both
 * fleets are already placed, so this skips placement entirely and
 * jumps straight into combat).
 * </p>
 */
public class MainGameController {

    private Game game;
    private final BattlefieldView3D view;
    private final Node pickSurface;

    /** {@code null} when this controller was created via the "resume a saved game" constructor -- there is no placement phase to run. */
    private final ShipPlacementController placementController;

    private CombatController combatController;
    private Map<Ship, Ship3D> machineShipViews;
    private boolean cheatRevealActive;

    /**
     * Starts a brand-new game at HU-1.
     *
     * @param nickname    the human player's chosen nickname
     * @param view        the 3D battlefield to control
     * @param pickSurface the node mouse/keyboard input is read from (normally {@code view} itself)
     */
    public MainGameController(String nickname, BattlefieldView3D view, Node pickSurface) {
        this.game = new Game(nickname, new HuntShootingStrategy());
        this.view = view;
        this.pickSurface = pickSurface;

        this.placementController = new ShipPlacementController(game.getHuman().getOwnBoard(), view.getPlayerBoardGroup());
        placementController.addListener(new ShipPlacementAdapter() {
            @Override
            public void onFleetPlacementComplete() {
                startCombatPhase();
            }
        });
    }

    /**
     * Resumes an already-in-progress game loaded from disk (HU-5).
     *
     * @param savedGame   a game previously restored via
     *                    {@link com.example.battleship.controller.persistence.SavedGameRepository#load()}
     * @param view        the 3D battlefield to control
     * @param pickSurface the node mouse/keyboard input is read from (normally {@code view} itself)
     */
    public MainGameController(Game savedGame, BattlefieldView3D view, Node pickSurface) {
        this.game = savedGame;
        this.view = view;
        this.pickSurface = pickSurface;
        this.placementController = null;
    }

    /**
     * Starts a new game: hands input over to fleet placement, and
     * starts listening for the Konami Code (HU-3) for the whole rest of
     * the game. Only valid on a controller created with the "new game"
     * constructor.
     */
    public void start() {
        placementController.attachTo(pickSurface);
        listenForKonamiCode();
    }

    /**
     * Jumps straight into combat with an already-placed, already
     * in-progress game (HU-5). Only valid on a controller created with
     * the "resume a saved game" constructor. If the save happened
     * mid-way through the machine's turn, this also kicks the machine
     * back into motion (see {@link CombatController#resumeIfMachinesTurn});
     * otherwise the game would sit idle forever, with neither side able
     * to act.
     */
    public void resume() {
        Map<Ship, Ship3D> playerShipViews = rebuildBoardView(game.getHuman().getOwnBoard(), view.getPlayerBoardGroup(), false);
        machineShipViews = rebuildBoardView(game.getMachine().getOwnBoard(), view.getMachineBoardGroup(), true);

        combatController = new CombatController(game, view, playerShipViews, machineShipViews);
        combatController.attachTo(pickSurface);
        combatController.focusCameraForCurrentTurn();
        combatController.resumeIfMachinesTurn();

        listenForKonamiCode();
    }

    /** Registers the Konami Code (HU-3) as a scene-wide key listener, active for the rest of the game regardless of which controller currently owns mouse input. */
    private void listenForKonamiCode() {
        Scene scene = pickSurface.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, new KonamiCodeDetector(this::toggleCheatReveal));
        }
    }

    /**
     * Called once the human's fleet is fully placed: auto-places the
     * machine's fleet (HU-4), mirrors it into {@link CombatController},
     * and hands input over to it.
     */
    private void startCombatPhase() {
        FleetPlacer.placeFleetRandomly(game.getMachine().getOwnBoard(), new Random());
        machineShipViews = rebuildBoardView(game.getMachine().getOwnBoard(), view.getMachineBoardGroup(), true);

        combatController = new CombatController(game, view, placementController.getShipViews(), machineShipViews);
        combatController.attachTo(pickSurface);
        combatController.focusCameraForCurrentTurn();
    }

    /**
     * Builds the 3D shape for every ship on an already-placed board and
     * adds it to the scene, then replays every shot already resolved on
     * that board -- a miss becomes an X mark, a hit or sunk cell is
     * applied to the matching ship via {@link Ship3D#markHit}. Used for
     * the machine's freshly-placed (and therefore shot-free) fleet in a
     * new game, and for both fleets -- misses, hits and all -- when
     * resuming a saved one.
     *
     * @param board            an already-placed board to mirror into the scene
     * @param boardGroup       the 3D group to add the ships to
     * @param hideIntactHulls  {@code true} to hide each not-yet-sunk ship's
     *                         hull (the machine's board, so the fleet stays
     *                         secret -- see {@link Ship3D#setHullVisible}),
     *                         {@code false} to leave it visible (the
     *                         player's own board)
     * @return every ship on the board, mapped to its new 3D shape
     */
    private Map<Ship, Ship3D> rebuildBoardView(Board board, Group boardGroup, boolean hideIntactHulls) {
        Map<Ship, Ship3D> shipViews = FleetViewBuilder.buildViews(board);
        for (Ship3D ship3D : shipViews.values()) {
            boardGroup.getChildren().add(ship3D);
            if (hideIntactHulls) {
                ship3D.setHullVisible(false);
            }
        }
        replayShotMarks(board, shipViews, boardGroup);
        return shipViews;
    }

    /** Walks every cell of {@code board} and re-applies whatever shot outcome it already recorded (miss, hit, or sunk) to the matching 3D shapes. */
    private void replayShotMarks(Board board, Map<Ship, Ship3D> shipViews, Group boardGroup) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Position position = new Position(row, column);
                Cell cell = board.getCell(position);
                switch (cell.getState()) {
                    case MISS -> boardGroup.getChildren().add(ShotMark3D.waterMiss(row, column));
                    case HIT, SUNK -> replayHit(cell, shipViews, position);
                    default -> { }
                }
            }
        }
    }

    /** Applies a single already-recorded hit/sunk cell to the 3D shape of whichever ship occupies it, via {@link Ship3D#markHit}. */
    private void replayHit(Cell cell, Map<Ship, Ship3D> shipViews, Position position) {
        Ship ship = cell.getOccupyingShip();
        if (ship == null) {
            return;
        }
        Ship3D ship3D = shipViews.get(ship);
        if (ship3D != null) {
            ship3D.markHit(ship.segmentIndexAt(position));
        }
    }

    /**
     * HU-3: toggles revealing every machine ship's hull at once (the
     * "cheat" view), entered via {@link KonamiCodeDetector}. Does
     * nothing before the machine's fleet exists in the scene. Skips any
     * ship that's already sunk: those stay permanently revealed and
     * shouldn't be hidden again just because the cheat got toggled off.
     */
    private void toggleCheatReveal() {
        if (machineShipViews == null) {
            return;
        }
        cheatRevealActive = !cheatRevealActive;
        for (Ship3D ship3D : machineShipViews.values()) {
            if (!ship3D.isSunk()) {
                ship3D.setHullVisible(cheatRevealActive);
            }
        }
    }

    /** @return the game being played */
    public Game getGame() {
        return game;
    }

    /** @return the placement controller, or {@code null} if this game was resumed from a save (no placement phase) */
    public ShipPlacementController getPlacementController() {
        return placementController;
    }

    /** @return the combat controller, or {@code null} before combat has actually started */
    public CombatController getCombatController() {
        return combatController;
    }

    /**
     * Convenience passthrough so callers only need to hold a reference
     * to {@code MainGameController}, not to {@code CombatController}
     * directly, to listen for turn changes and game-over -- returns
     * silently (does nothing) if combat hasn't started yet. Only valid
     * for a "new game" controller; for a resumed game,
     * {@link #getCombatController()} is already non-null immediately
     * after {@link #resume()} returns, so the listener can be attached
     * directly instead.
     *
     * @param listener the listener to attach once combat starts
     */
    public void addCombatListenerWhenReady(CombatListener listener) {
        placementController.addListener(new ShipPlacementAdapter() {
            @Override
            public void onFleetPlacementComplete() {
                combatController.addListener(listener);
            }
        });
    }
}