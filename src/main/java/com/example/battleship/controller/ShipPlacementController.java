package com.example.battleship.controller;

import com.example.battleship.controller.event.ShipPlacementListener;
import com.example.battleship.model.board.Board;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.board.Position;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.ship.ShipFactory;
import com.example.battleship.model.ship.ShipType;
import com.example.battleship.model.exception.InvalidShipPlacementException;
import com.example.battleship.view.assets.Water3D;
import com.example.battleship.view.ships.Ship3D;
import com.example.battleship.view.ships.ShipFactory3D;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles HU-1: letting the human player place their fleet manually,
 * one ship at a time, by clicking a cell on their own board. The ship
 * type to place always comes from the front of the fleet queue (so the
 * order is fixed: aircraft carrier first, frigates last); the "R" key
 * toggles the orientation the next ship will be placed with.
 * <p>
 * While the mouse hovers a cell on the player's own board, a flat,
 * pale-cyan "ghost" copy of the ship about to be placed follows the
 * cursor (see {@link #rebuildPreview}), showing the current
 * orientation live -- so pressing "R" is immediately visible instead of
 * being an invisible state change. The ghost turns red whenever the
 * model ({@link Board#canPlaceShip}) reports that this exact placement
 * would fail, so the player sees the invalid spot before clicking it.
 * </p>
 * <p>
 * Talks to the model ({@link Board#placeShip}) and mirrors every
 * successful placement into the 3D scene as a {@link Ship3D}. Reports
 * progress through {@link ShipPlacementListener} rather than holding a
 * reference to any other controller -- see
 * {@link com.example.battleship.controller.event.ShipPlacementAdapter}
 * for the easy way to listen for just one of its callbacks.
 * </p>
 */
public class ShipPlacementController {

    private final Board board;
    private final Group boardGroup;
    private final List<ShipType> remainingTypes = new ArrayList<>(ShipFactory.createFullFleetTypes());
    private final Map<Ship, Ship3D> shipViews = new LinkedHashMap<>();
    private final List<ShipPlacementListener> listeners = new ArrayList<>();
    private final RotateKeyHandler rotateKeyHandler = new RotateKeyHandler();

    private Orientation currentOrientation = Orientation.HORIZONTAL;

    private Ship3D previewShip;
    private Integer previewRow;
    private Integer previewColumn;

    private Scene attachedScene;

    /**
     * @param board      the human player's own (empty) board
     * @param boardGroup the 3D group representing that same board in the scene
     */
    public ShipPlacementController(Board board, Group boardGroup) {
        this.board = board;
        this.boardGroup = boardGroup;
    }

    /**
     * Wires this controller's mouse and keyboard handling onto
     * {@code pickSurface} (normally the {@code BattlefieldView3D}
     * itself). Mouse handlers go on {@code pickSurface} directly, but
     * "R" is registered as an event filter on the whole {@link Scene}
     * instead -- a {@code SubScene} (which is what {@code pickSurface}
     * normally is) doesn't reliably keep keyboard focus even after
     * {@code requestFocus()}, so listening at the scene level is what
     * actually guarantees the key press is seen, regardless of which
     * node currently has focus.
     *
     * @param pickSurface the node to listen on; must already be part of a {@code Scene}
     */
    public void attachTo(Node pickSurface) {
        pickSurface.setOnMouseClicked(this::onCellClicked);
        pickSurface.setOnMouseMoved(this::onMouseMoved);

        Scene scene = pickSurface.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, rotateKeyHandler);
            attachedScene = scene;
        }
    }

    /** Stops listening for the "R" key -- call this once placement is done, so a later phase's own key handling isn't shadowed. */
    public void detach() {
        if (attachedScene != null) {
            attachedScene.removeEventFilter(KeyEvent.KEY_PRESSED, rotateKeyHandler);
            attachedScene = null;
        }
    }

    private void onCellClicked(MouseEvent event) {
        if (isComplete()) {
            return;
        }
        PickResult pick = event.getPickResult();
        Node picked = pick.getIntersectedNode();
        if (!(picked instanceof Water3D water) || water.getParent() != boardGroup) {
            return;
        }
        attemptPlacement(water.getRow(), water.getColumn());
    }

    private void onMouseMoved(MouseEvent event) {
        if (isComplete()) {
            clearPreview();
            return;
        }
        PickResult pick = event.getPickResult();
        Node picked = pick.getIntersectedNode();
        if (picked instanceof Water3D water && water.getParent() == boardGroup) {
            // TEMPORARY diagnostic: confirms the view (this exact Water3D
            // tile, wherever it's drawn on screen) and the model (the
            // row/column Board will actually validate against) agree.
            // Move the mouse across the board and compare this printout
            // against the cell you're actually looking at -- count from
            // the corner where column letter "A" is (column 0) and from
            // whichever edge the board starts at (row 0), NOT from the
            // on-screen number labels (those are known to be reversed).
            // Remove once confirmed.
            if (!Integer.valueOf(water.getRow()).equals(previewRow)
                    || !Integer.valueOf(water.getColumn()).equals(previewColumn)) {
                System.out.println("[hover] model says: row=" + water.getRow() + ", column=" + water.getColumn());
            }
            previewRow = water.getRow();
            previewColumn = water.getColumn();
            rebuildPreview();
        } else {
            clearPreview();
        }
    }

    private void attemptPlacement(int row, int column) {
        ShipType type = remainingTypes.get(0);
        try {
            Position origin = new Position(row, column);
            Ship ship = ShipFactory.createShip(type, origin, currentOrientation);
            board.placeShip(ship);

            Ship3D ship3D = ShipFactory3D.create(type, row, column, currentOrientation);
            boardGroup.getChildren().add(ship3D);
            shipViews.put(ship, ship3D);
            remainingTypes.remove(0);

            for (ShipPlacementListener listener : listeners) {
                listener.onShipPlaced(ship, ship3D);
            }
            if (isComplete()) {
                clearPreview();
                detach();
                for (ShipPlacementListener listener : listeners) {
                    listener.onFleetPlacementComplete();
                }
            } else {
                rebuildPreview(); // now showing the next ship in the queue, same hovered cell
            }
        } catch (InvalidShipPlacementException e) {
            for (ShipPlacementListener listener : listeners) {
                listener.onPlacementRejected(e.getMessage());
            }
        }
    }

    /**
     * Rebuilds the preview ghost at {@link #previewRow}/{@link #previewColumn}
     * for the current ship type and orientation. Called on every mouse
     * move over a new cell, every "R" press, and after every placement
     * (since the next ship in the queue is a different type/size).
     * <p>
     * Always asks the model ({@link Board#canPlaceShip}) whether this
     * exact placement would actually succeed, and colors the ghost
     * accordingly -- cyan if it fits, red if it would run off the board
     * or overlap another ship.
     * </p>
     */
    private void rebuildPreview() {
        removePreviewNode();
        if (previewRow == null || isComplete()) {
            return;
        }
        ShipType type = getCurrentShipType();
        Position origin = new Position(previewRow, previewColumn);
        Ship candidate = ShipFactory.createShip(type, origin, currentOrientation);

        Ship3D ghost = ShipFactory3D.create(type, previewRow, previewColumn, currentOrientation);
        if (board.canPlaceShip(candidate)) {
            ghost.markAsPreview();
        } else {
            ghost.markAsInvalidPreview();
        }
        ghost.setMouseTransparent(true); // never itself intercepts clicks/picks meant for the water beneath it
        boardGroup.getChildren().add(ghost);
        previewShip = ghost;
    }

    private void clearPreview() {
        previewRow = null;
        previewColumn = null;
        removePreviewNode();
    }

    private void removePreviewNode() {
        if (previewShip != null) {
            boardGroup.getChildren().remove(previewShip);
            previewShip = null;
        }
    }

    /** @return {@code true} once every ship in the fleet has been placed */
    public boolean isComplete() {
        return remainingTypes.isEmpty();
    }

    /** @return the type of the next ship to be placed, or {@code null} once the fleet is complete */
    public ShipType getCurrentShipType() {
        return isComplete() ? null : remainingTypes.get(0);
    }

    public Orientation getCurrentOrientation() {
        return currentOrientation;
    }

    /** @return every ship placed so far, mapped to its 3D shape, in placement order */
    public Map<Ship, Ship3D> getShipViews() {
        return Collections.unmodifiableMap(shipViews);
    }

    public void addListener(ShipPlacementListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ShipPlacementListener listener) {
        listeners.remove(listener);
    }

    /**
     * Toggles {@link #currentOrientation} on the "R" key and refreshes
     * the preview ghost so the rotation is immediately visible -- a
     * private inner class (rather than a lambda) so the rotate behavior
     * reads as a named, self-contained unit of keyboard handling.
     */
    private class RotateKeyHandler implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            if (event.getCode() != KeyCode.R) {
                return;
            }
            currentOrientation = currentOrientation == Orientation.HORIZONTAL
                    ? Orientation.VERTICAL
                    : Orientation.HORIZONTAL;
            rebuildPreview();
            event.consume();
        }
    }
}