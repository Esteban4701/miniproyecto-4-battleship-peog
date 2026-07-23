package com.example.battleship.controller;

import com.example.battleship.controller.event.BoardListenerAdapter;
import com.example.battleship.controller.event.CombatListener;
import com.example.battleship.controller.persistence.SavedGameRepository;
import com.example.battleship.model.Game;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.player.ShotResult;
import com.example.battleship.model.player.Turn;
import com.example.battleship.model.exception.CellAlreadyShotException;
import com.example.battleship.view.BattlefieldView3D;
import com.example.battleship.view.assets.HoverMarker3D;
import com.example.battleship.view.assets.ShotMark3D;
import com.example.battleship.view.assets.Water3D;
import com.example.battleship.view.ships.Ship3D;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles HU-2 (the human player firing at the machine's board) and
 * HU-4 (the machine automatically firing back). Translates every
 * resolved shot into the matching 3D feedback: a red X on a miss, or a
 * {@link com.example.battleship.view.assets.WreckageDebris3D} cluster
 * on a hit -- via {@link Ship3D#markHit}, which already handles that.
 * <p>
 * Listens to both boards through the model's own
 * {@code BoardListener} (an {@link BoardListenerAdapter} subclass per
 * board), rather than relying only on the return value of
 * {@link Game#fireAtMachine}: that's what lets the machine's own shots
 * (fired from {@link #scheduleNextMachineShot}) drive the same visual
 * feedback path as the player's shots, with no duplicated code.
 * </p>
 * <p>
 * Also drives HU-5's autosave: every resolved shot writes the game to
 * disk via {@link SavedGameRepository} (see {@link #autoSave}), and the
 * save is deleted once the match actually ends.
 * </p>
 * <p>
 * The pause before each machine shot ({@link #scheduleNextMachineShot})
 * runs on its own dedicated background thread, which then hands control
 * back to the JavaFX Application Thread via {@link javafx.application.Platform#runLater}
 * to actually resolve the shot -- the standard, correct way to combine
 * a background thread with JavaFX, since the scene graph may only ever
 * be touched from its own thread.
 * </p>
 */
public class CombatController {

    /**
     * Pause before each machine shot. Set high enough (well past the
     * camera's own 1400ms pan) that the very first shot of the
     * machine's turn never resolves -- and shows its hit/miss mark --
     * before the camera has actually finished traveling to the board
     * being shot at.
     */
    private static final Duration MACHINE_SHOT_DELAY = Duration.seconds(3);

    private final Game game;
    private final BattlefieldView3D view;
    private final Map<Ship, Ship3D> playerShipViews;
    private final Map<Ship, Ship3D> machineShipViews;
    private final List<CombatListener> listeners = new ArrayList<>();
    private final HoverMarker3D targetMarker = new HoverMarker3D();

    /** The cell picked on the first click, waiting for a second click on the same cell to confirm. */
    private Position selectedPosition;

    /**
     * @param game              the game being played
     * @param view              the 3D battlefield
     * @param playerShipViews   the human ships, mapped to their 3D shapes (from {@link ShipPlacementController})
     * @param machineShipViews  the machine's ships, mapped to their 3D shapes (from {@link FleetViewBuilder})
     */
    public CombatController(Game game, BattlefieldView3D view,
                            Map<Ship, Ship3D> playerShipViews, Map<Ship, Ship3D> machineShipViews) {
        this.game = game;
        this.view = view;
        this.playerShipViews = playerShipViews;
        this.machineShipViews = machineShipViews;

        // The human fires at the machine's board.
        game.getMachine().getOwnBoard().addListener(new MachineBoardListener());
        // The machine fires at the human's board.
        game.getHuman().getOwnBoard().addListener(new HumanBoardListener());

        view.getMachineBoardGroup().getChildren().add(targetMarker);
    }

    /**
     * Kicks off the machine's turn if the game is currently in the
     * middle of it. Needed for HU-5's "Continuar": a saved game could
     * have been exited at exactly the moment between the human's last
     * shot (which passed the turn to the machine) and the machine
     * actually firing back. Without this call, nothing would ever
     * prompt the machine to take that turn -- the human can't fire
     * either, since {@link #onCellClicked} already correctly refuses to
     * act out of turn -- and the game would simply sit idle forever.
     * Does nothing if it's currently the human's turn.
     */
    public void resumeIfMachinesTurn() {
        if (game.getCurrentTurn() == Turn.MACHINE) {
            scheduleNextMachineShot();
        }
    }

    /**
     * Wires this controller's mouse handling onto {@code pickSurface}
     * (normally the {@code BattlefieldView3D} itself), taking over from
     * whichever controller was handling input before.
     *
     * @param pickSurface the node to listen on
     */
    public void attachTo(Node pickSurface) {
        pickSurface.setOnMouseClicked(this::onCellClicked);
    }

    /**
     * First click on a cell selects it (shows {@link #targetMarker} there)
     * without firing; a second click on that SAME cell confirms and
     * fires. Clicking a different cell just moves the selection there
     * instead of firing -- this is what stops an accidental shot from a
     * click that was really meant to start dragging the camera, since a
     * single stray click during a drag only ever selects, never fires.
     */
    private void onCellClicked(MouseEvent event) {
        if (game.getCurrentTurn() != Turn.HUMAN) {
            return;
        }
        PickResult pick = event.getPickResult();
        Node picked = pick.getIntersectedNode();
        if (!(picked instanceof Water3D water) || water.getParent() != view.getMachineBoardGroup()) {
            return;
        }

        Position clicked = new Position(water.getRow(), water.getColumn());
        if (!game.getMachine().getOwnBoard().canBeShotAt(clicked)) {
            return; // Already resolved cell: nothing to select or confirm.
        }

        if (!clicked.equals(selectedPosition)) {
            selectedPosition = clicked;
            targetMarker.moveToCell(clicked.row(), clicked.column());
            return;
        }

        selectedPosition = null;
        targetMarker.hide();

        try {
            game.fireAtMachine(clicked);
        } catch (CellAlreadyShotException e) {
            return; // Defensive: shouldn't happen given the canBeShotAt check above.
        }
        afterShotResolved();
    }

    /**
     * Runs after the human shot resolves: ends the match if that shot
     * finished off the machine's whole fleet, otherwise auto saves,
     * notifies listeners of the (possible) turn change, repositions the
     * camera, and hands off to the machine if it's now its turn.
     */
    private void afterShotResolved() {
        if (game.isOver()) {
            deleteSaveOnGameOver();
            notifyGameOver();
            return;
        }
        autoSave();
        notifyTurnChanged();
        focusCameraForCurrentTurn();
        if (game.getCurrentTurn() == Turn.MACHINE) {
            scheduleNextMachineShot();
        }
    }

    /**
     * Waits {@link #MACHINE_SHOT_DELAY} on a dedicated background
     * thread (so the delay never ties up the JavaFX Application Thread,
     * even though nothing else happens to need it during those few
     * seconds), then hands control back to the FX thread via
     * {@link Platform#runLater} to actually resolve the shot.
     * <p>
     * {@code Platform.runLater} is not optional here: {@link #game},
     * the 3D scene, and every {@code CombatListener} may only be
     * touched from the JavaFX Application Thread. Calling
     * {@link #resolveNextMachineShot()} directly from this background
     * thread would corrupt the scene graph (JavaFX explicitly forbids
     * touching it off its own thread) instead of just running slowly.
     * </p>
     */
    private void scheduleNextMachineShot() {
        Thread delayThread = new Thread(() -> {
            try {
                Thread.sleep((long) MACHINE_SHOT_DELAY.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            Platform.runLater(this::resolveNextMachineShot);
        }, "machine-turn-delay");
        delayThread.setDaemon(true); // never keeps the JVM alive on its own
        delayThread.start();
    }

    /** Resolves exactly one machine shot -- always called back on the JavaFX Application Thread, never directly from {@link #scheduleNextMachineShot}'s background thread. */
    private void resolveNextMachineShot() {
        game.playMachineShot();

        if (game.isOver()) {
            deleteSaveOnGameOver();
            notifyGameOver();
            return;
        }
        autoSave();
        notifyTurnChanged();
        focusCameraForCurrentTurn();
        if (game.getCurrentTurn() == Turn.MACHINE) {
            scheduleNextMachineShot(); // hit or sunk: the machine keeps firing
        }
    }

    /**
     * HU-5: writes the current game state to disk after every resolved
     * shot, so "Continuar" on the main menu always has the freshest
     * possible state to offer -- there's no separate "save" action to
     * remember to use. Delegates to {@link SavedGameRepository#saveAsync},
     * which does the actual write on its own background thread, so this
     * call returns immediately and never stalls the game while a shot's
     * animation is still playing.
     */
    private void autoSave() {
        SavedGameRepository.saveAsync(game);
    }

    /**
     * Once a match is actually won or lost, there's nothing left to
     * "continue" -- clear the save so the menu reflects that. Uses
     * {@link SavedGameRepository#deleteSavedGameAsync()} (not the
     * synchronous version) specifically so to delete is queued behind
     * any autosave still pending from an earlier shot, instead of
     * possibly racing ahead of it and getting silently undone.
     */
    private void deleteSaveOnGameOver() {
        SavedGameRepository.deleteSavedGameAsync();
    }

    /**
     * Points the camera at whichever board is currently being shot AT --
     * the opposite board from whoever's turn it is, not the same one.
     * On the human's turn, the human is aiming at the machine's board,
     * so that's what the camera should show (and vice versa on the
     * machine's turn, so the player can watch the incoming shot land on
     * their own board). Public so {@link MainGameController} can call it
     * once, right when combat starts, to move the camera off the
     * player's own board (where it sat during placement) and onto the
     * machine's board for the human's first turn.
     */
    public void focusCameraForCurrentTurn() {
        if (game.getCurrentTurn() == Turn.HUMAN) {
            view.setTurn(BattlefieldView3D.Turn.MACHINE);
        } else {
            view.setTurn(BattlefieldView3D.Turn.PLAYER);
        }
    }

    /** Notifies every registered {@link CombatListener} of the current turn. */
    private void notifyTurnChanged() {
        for (CombatListener listener : listeners) {
            listener.onTurnChanged(game.getCurrentTurn());
        }
    }

    /** Notifies every registered {@link CombatListener} that the match is over, naming the winner. */
    private void notifyGameOver() {
        for (CombatListener listener : listeners) {
            listener.onGameOver(game.getWinner());
        }
    }

    /** @param listener a listener to notify of future turn changes and game-over */
    public void addListener(CombatListener listener) {
        listeners.add(listener);
    }

    /** @param listener a previously added listener to stop notifying */
    public void removeListener(CombatListener listener) {
        listeners.remove(listener);
    }

    /** Shots landing on the machine's board -- fired by the human player. */
    private class MachineBoardListener extends BoardListenerAdapter {
        @Override
        public void onShotResolved(Position position, ShotResult result, Ship ship) {
            applyShotFeedback(view.getMachineBoardGroup(), machineShipViews, position, result, ship);
        }
    }

    /** Shots landing on the human's board -- fired by the machine. */
    private class HumanBoardListener extends BoardListenerAdapter {
        @Override
        public void onShotResolved(Position position, ShotResult result, Ship ship) {
            applyShotFeedback(view.getPlayerBoardGroup(), playerShipViews, position, result, ship);
        }
    }

    /**
     * Applies the correct 3D feedback for one resolved shot: a red X
     * on the water for a miss, or -- via {@link Ship3D#markHit} -- the
     * matching wreckage/hull change on the ship that got hit.
     *
     * @param boardGroup the 3D group the shot landed on (whichever board was targeted)
     * @param shipViews  that board's ships, mapped to their 3D shapes
     * @param position   the cell that was shot at
     * @param result     the outcome of that shot
     * @param ship       the ship that was hit, or {@code null} if the shot was a miss
     */
    private void applyShotFeedback(Group boardGroup, Map<Ship, Ship3D> shipViews,
                                   Position position, ShotResult result, Ship ship) {
        if (result == ShotResult.WATER) {
            boardGroup.getChildren().add(ShotMark3D.waterMiss(position.row(), position.column()));
            return;
        }
        Ship3D ship3D = shipViews.get(ship);
        if (ship3D != null) {
            ship3D.markHit(ship.segmentIndexAt(position));
        }
    }
}