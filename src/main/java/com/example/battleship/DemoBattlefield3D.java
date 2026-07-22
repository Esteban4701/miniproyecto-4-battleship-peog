package com.example.battleship;

import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.ShipType;
import com.example.battleship.view.BattlefieldView3D;
import com.example.battleship.view.ships.ShipFactory3D;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Demo class: NOT part of the final game. Shows the free/orbit camera
 * and the two-board battlefield in action -- drag with the mouse to
 * orbit the dome, and use the buttons to trigger the "Turno Jugador" /
 * "Turno Maquina" camera pan between boards, same as the concept
 * sketch.
 */
public class DemoBattlefield3D extends Application {

    @Override
    public void start(Stage stage) {
        BattlefieldView3D battlefield = new BattlefieldView3D(900, 700);
        battlefield.getCameraRig().attachDragControls(battlefield);

        // A few ships on each board, just so there's something to look at.
        battlefield.addShipToPlayerBoard(ShipFactory3D.create(ShipType.AIRCRAFT_CARRIER, 1, 1, Orientation.HORIZONTAL));
        battlefield.addShipToPlayerBoard(ShipFactory3D.create(ShipType.DESTROYER, 4, 6, Orientation.VERTICAL));
        battlefield.addShipToPlayerBoard(ShipFactory3D.create(ShipType.SUBMARINE, 7, 1, Orientation.HORIZONTAL));

        battlefield.addShipToMachineBoard(ShipFactory3D.create(ShipType.AIRCRAFT_CARRIER, 2, 2, Orientation.HORIZONTAL));
        battlefield.addShipToMachineBoard(ShipFactory3D.create(ShipType.DESTROYER, 5, 5, Orientation.VERTICAL));
        battlefield.addShipToMachineBoard(ShipFactory3D.create(ShipType.SUBMARINE, 0, 7, Orientation.HORIZONTAL));

        Button playerTurnButton = new Button("Turno Jugador");
        playerTurnButton.setOnAction(e -> battlefield.setTurn(BattlefieldView3D.Turn.PLAYER));

        Button machineTurnButton = new Button("Turno Maquina");
        machineTurnButton.setOnAction(e -> battlefield.setTurn(BattlefieldView3D.Turn.MACHINE));

        HBox controls = new HBox(12, playerTurnButton, machineTurnButton);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new javafx.geometry.Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(battlefield);
        root.setBottom(controls);

        Scene scene = new Scene(root, 900, 760);
        stage.setTitle("Battlefield 3D - Free Camera Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
