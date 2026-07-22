package com.example.battleship.controller;

import com.example.battleship.controller.event.CombatListener;
import com.example.battleship.controller.event.ShipPlacementAdapter;
import com.example.battleship.model.Game;
import com.example.battleship.model.player.HuntShootingStrategy;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.FleetPlacer;
import com.example.battleship.view.BattlefieldView3D;
import com.example.battleship.view.ships.Ship3D;

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
 */
public class MainGameController {

    private final Game game;
    private final BattlefieldView3D view;
    private final Node pickSurface;

    private final ShipPlacementController placementController;
    private CombatController combatController;
    private Map<Ship, Ship3D> machineShipViews;
    private boolean cheatRevealActive;

    /**
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
     * Starts the game at HU-1: hands input over to fleet placement, and
     * starts listening for the Konami Code (HU-3) for the whole rest of
     * the game -- {@link #toggleCheatReveal} itself is a no-op until
     * combat actually starts, so it's safe to listen this early.
     */
    public void start() {
        placementController.attachTo(pickSurface);

        Scene scene = pickSurface.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, new KonamiCodeDetector(this::toggleCheatReveal));
        }
    }

    /**
     * Called once the human's fleet is fully placed: auto-places the
     * machine's fleet (HU-4), mirrors both fleets' ships into
     * {@link CombatController}, and hands input over to it.
     * <p>
     * The machine's ships are added to the scene but start with their
     * hulls hidden (only a sunk ship, or the HU-3 cheat, reveals them --
     * see {@link com.example.battleship.view.ships.Ship3D#setHullVisible}) --
     * otherwise the player could see the whole enemy fleet from the
     * first moment of combat, which defeats the point of the game.
     * </p>
     */
    private void startCombatPhase() {
        FleetPlacer.placeFleetRandomly(game.getMachine().getOwnBoard(), new Random());
        machineShipViews = FleetViewBuilder.buildViews(game.getMachine().getOwnBoard());
        for (Ship3D ship3D : machineShipViews.values()) {
            view.addShipToMachineBoard(ship3D);
            ship3D.setHullVisible(false);
        }

        combatController = new CombatController(game, view, placementController.getShipViews(), machineShipViews);
        combatController.attachTo(pickSurface);
        combatController.focusCameraForCurrentTurn();
    }

    /**
     * HU-3: toggles revealing every machine ship's hull at once (the
     * "cheat" view), entered via {@link KonamiCodeDetector}. Does
     * nothing if combat hasn't started yet -- there is no machine fleet
     * to reveal during placement. Skips any ship that's already sunk:
     * those stay permanently revealed and shouldn't be hidden again
     * just because the cheat got toggled off.
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

    public Game getGame() {
        return game;
    }

    public ShipPlacementController getPlacementController() {
        return placementController;
    }

    /** @return the combat controller, or {@code null} before the fleet has been fully placed */
    public CombatController getCombatController() {
        return combatController;
    }

    /**
     * Convenience passthrough so callers only need to hold a reference
     * to {@code MainGameController}, not to {@code CombatController}
     * directly, to listen for turn changes and game-over -- returns
     * silently (does nothing) if combat hasn't started yet.
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
