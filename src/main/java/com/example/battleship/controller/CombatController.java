package com.example.battleship.controller;

import com.example.battleship.controller.event.BoardListenerAdapter;
import com.example.battleship.controller.event.CombatListener;
import com.example.battleship.model.Game;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.ShotResult;
import com.example.battleship.model.player.Turn;
import com.example.battleship.model.exception.CellAlreadyShotException;
import com.example.battleship.view.BattlefieldView3D;
import com.example.battleship.view.assets.ShotMark3D;
import com.example.battleship.view.assets.Water3D;
import com.example.battleship.view.ships.Ship3D;

import javafx.animation.PauseTransition;
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

    /**
     * @param game              the game being played
     * @param view              the 3D battlefield
     * @param playerShipViews   the human's ships, mapped to their 3D shapes (from {@link ShipPlacementController})
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

    private void onCellClicked(MouseEvent event) {
        if (game.getCurrentTurn() != Turn.HUMAN) {
            return;
        }
        PickResult pick = event.getPickResult();
        Node picked = pick.getIntersectedNode();
        if (!(picked instanceof Water3D water) || water.getParent() != view.getMachineBoardGroup()) {
            return;
        }

        try {
            game.fireAtMachine(new Position(water.getRow(), water.getColumn()));
        } catch (CellAlreadyShotException e) {
            return; // Already resolved cell: ignore the click, nothing changes.
        }
        afterShotResolved();
    }

    private void afterShotResolved() {
        if (game.isOver()) {
            notifyGameOver();
            return;
        }
        notifyTurnChanged();
        focusCameraForCurrentTurn();
        if (game.getCurrentTurn() == Turn.MACHINE) {
            scheduleNextMachineShot();
        }
    }

    private void scheduleNextMachineShot() {
        PauseTransition pause = new PauseTransition(MACHINE_SHOT_DELAY);
        pause.setOnFinished(event -> {
            game.playMachineShot();

            if (game.isOver()) {
                notifyGameOver();
                return;
            }
            notifyTurnChanged();
            focusCameraForCurrentTurn();
            if (game.getCurrentTurn() == Turn.MACHINE) {
                scheduleNextMachineShot(); // hit or sunk: the machine keeps firing
            }
        });
        pause.play();
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

    private void notifyTurnChanged() {
        for (CombatListener listener : listeners) {
            listener.onTurnChanged(game.getCurrentTurn());
        }
    }

    private void notifyGameOver() {
        for (CombatListener listener : listeners) {
            listener.onGameOver(game.getWinner());
        }
    }

    public void addListener(CombatListener listener) {
        listeners.add(listener);
    }

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
