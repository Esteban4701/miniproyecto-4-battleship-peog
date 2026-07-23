package com.example.battleship.controller;

import com.example.battleship.Main;
import com.example.battleship.controller.event.CombatAdapter;
import com.example.battleship.controller.event.ShipPlacementAdapter;
import com.example.battleship.controller.persistence.SavedGameRepository;
import com.example.battleship.model.Game;
import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.player.Player;
import com.example.battleship.model.ship.Ship;
import com.example.battleship.model.ship.ShipType;
import com.example.battleship.view.BattlefieldView3D;
import com.example.battleship.view.ships.Ship3D;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The controller behind {@code main-view.fxml}: the single screen the
 * whole game runs on. Owns the persistent {@link BattlefieldView3D}
 * (added to the scene graph in code, not in the FXML -- see
 * {@link #initialize}) and layers the right 2D overlay on top of it
 * for whichever {@link GamePhase} is active.
 * <p>
 * Delegates all actual game logic to {@link MainGameController}; this
 * class's only job is translating between screen state (which pane is
 * showing) and that controller's events
 * ({@link com.example.battleship.controller.event.ShipPlacementListener},
 * {@link com.example.battleship.controller.event.CombatListener}).
 * </p>
 */
public class GameScreenController {

    @FXML
    private StackPane rootStack;

    @FXML
    private VBox startMenuPane;
    @FXML
    private TextField nicknameField;
    @FXML
    private Button continueButton;
    @FXML
    private Label startMenuHintLabel;

    @FXML
    private VBox placementHudPane;
    @FXML
    private Label placementInstructionLabel;

    @FXML
    private StackPane gameOverPane;
    @FXML
    private Label gameOverLabel;

    private BattlefieldView3D battlefield;
    private MainGameController mainGameController;
    private GamePhase phase = GamePhase.START_MENU;

    /** Called automatically by {@link javafx.fxml.FXMLLoader} once every {@code @FXML} field above is injected. */
    @FXML
    private void initialize() {
        battlefield = new BattlefieldView3D(rootStack.getPrefWidth(), rootStack.getPrefHeight());
        battlefield.widthProperty().bind(rootStack.widthProperty());
        battlefield.heightProperty().bind(rootStack.heightProperty());
        // Inserted at index 0: every pane the FXML already declared
        // (the menu, the HUD, the game-over overlay) stays drawn on top
        // of it, since StackPane paints later children over earlier ones.
        rootStack.getChildren().add(0, battlefield);

        // The instructional HUD during placement should never block
        // clicks meant for the board cells underneath it.
        placementHudPane.setMouseTransparent(true);

        // HU-5: "Continuar" is only ever enabled when there's actually
        // something saved on disk to continue.
        refreshContinueButtonState();
    }

    private void refreshContinueButtonState() {
        boolean hasSavedGame = SavedGameRepository.hasSavedGame();
        continueButton.setDisable(!hasSavedGame);
        continueButton.setTooltip(new Tooltip(hasSavedGame ? "Continuar la partida guardada" : "No hay partida guardada"));
    }

    @FXML
    private void onNewGameClicked() {
        String nickname = nicknameField.getText();
        if (nickname == null || nickname.isBlank()) {
            startMenuHintLabel.setText("Escribe un nombre antes de continuar");
            return;
        }
        startPlacementPhase(nickname.trim());
    }

    /** HU-5: loads the saved game from disk and jumps straight into combat with it, skipping placement entirely. */
    @FXML
    private void onContinueClicked() {
        Game savedGame;
        try {
            savedGame = SavedGameRepository.load();
        } catch (IOException | ClassNotFoundException e) {
            startMenuHintLabel.setText("No se pudo cargar la partida guardada");
            refreshContinueButtonState();
            return;
        }

        mainGameController = new MainGameController(savedGame, battlefield, battlefield);
        mainGameController.resume();
        mainGameController.getCombatController().addListener(new CombatAdapter() {
            @Override
            public void onGameOver(Player winner) {
                showGameOver(winner);
            }
        });

        phase = GamePhase.COMBAT;
        setPaneVisible(startMenuPane, false);
        battlefield.getCameraRig().attachDragControls(battlefield);
    }

    @FXML
    private void onBackToMenuClicked() {
        try {
            Stage stage = (Stage) rootStack.getScene().getWindow();
            Main.loadMainScreen(stage);
        } catch (IOException e) {
            throw new IllegalStateException("Could not reload the main screen", e);
        }
    }

    private void startPlacementPhase(String nickname) {
        phase = GamePhase.PLACEMENT;
        setPaneVisible(startMenuPane, false);
        setPaneVisible(placementHudPane, true);

        mainGameController = new MainGameController(nickname, battlefield, battlefield);

        mainGameController.getPlacementController().addListener(new ShipPlacementAdapter() {
            @Override
            public void onShipPlaced(Ship ship, Ship3D ship3D) {
                updatePlacementInstruction();
            }

            @Override
            public void onFleetPlacementComplete() {
                startCombatPhase();
            }
        });

        mainGameController.addCombatListenerWhenReady(new CombatAdapter() {
            @Override
            public void onGameOver(Player winner) {
                showGameOver(winner);
            }
        });

        updatePlacementInstruction();
        mainGameController.start();
    }

    private void updatePlacementInstruction() {
        ShipType type = mainGameController.getPlacementController().getCurrentShipType();
        if (type == null) {
            return;
        }
        Orientation orientation = mainGameController.getPlacementController().getCurrentOrientation();
        String orientationLabel = orientation == Orientation.HORIZONTAL ? "horizontal" : "vertical";
        placementInstructionLabel.setText("Coloca: " + shipDisplayName(type) + " (" + orientationLabel + ")");
    }

    private String shipDisplayName(ShipType type) {
        return switch (type) {
            case AIRCRAFT_CARRIER -> "Portaaviones";
            case SUBMARINE -> "Submarino";
            case DESTROYER -> "Destructor";
            case FRIGATE -> "Fragata";
        };
    }

    private void startCombatPhase() {
        phase = GamePhase.COMBAT;
        setPaneVisible(placementHudPane, false);
        // Free camera orbiting is only enabled once combat starts, so
        // the player can't accidentally spin away from their own board
        // mid-placement.
        battlefield.getCameraRig().attachDragControls(battlefield);
    }

    private void showGameOver(Player winner) {
        phase = GamePhase.GAME_OVER;
        boolean playerWon = winner == mainGameController.getGame().getHuman();
        gameOverLabel.setText(playerWon ? "Ganaste!" : "Perdiste");
        setPaneVisible(gameOverPane, true);
    }

    private void setPaneVisible(Node pane, boolean visible) {
        pane.setVisible(visible);
        pane.setManaged(visible);
    }

    public GamePhase getPhase() {
        return phase;
    }
}
