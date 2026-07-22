package com.example.battleship;

import com.example.battleship.model.ship.Orientation;
import com.example.battleship.model.ship.ShipType;
import com.example.battleship.view.BoardView3D;
import com.example.battleship.view.assets.ShotMark3D;
import com.example.battleship.view.ships.Ship3D;
import com.example.battleship.view.ships.ShipFactory3D;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Demo class: NOT part of the final game, it only serves to preview the
 * full fleet (1 aircraft carrier, 2 submarines, 3 destroyers,
 * 4 frigates) already placed on the 3D board, and to visually verify
 * that "hit" and "sunk" marks render correctly.
 * <p>
 * Run this class directly (it has {@code main}) to see the result in a
 * window.
 * </p>
 */
public class DemoShapes3D extends Application {

    @Override
    public void start(Stage stage) {
        BoardView3D board = new BoardView3D(800, 700);

        // Place a sample player's full fleet, without overlaps.
        Ship3D carrier = ShipFactory3D.create(ShipType.AIRCRAFT_CARRIER, 1, 1, Orientation.HORIZONTAL);

        Ship3D submarine1 = ShipFactory3D.create(ShipType.SUBMARINE, 3, 2, Orientation.HORIZONTAL);
        Ship3D submarine2 = ShipFactory3D.create(ShipType.SUBMARINE, 5, 6, Orientation.VERTICAL);

        Ship3D destroyer1 = ShipFactory3D.create(ShipType.DESTROYER, 0, 7, Orientation.VERTICAL);
        Ship3D destroyer2 = ShipFactory3D.create(ShipType.DESTROYER, 7, 0, Orientation.HORIZONTAL);
        Ship3D destroyer3 = ShipFactory3D.create(ShipType.DESTROYER, 8, 3, Orientation.HORIZONTAL);

        Ship3D frigate1 = ShipFactory3D.create(ShipType.FRIGATE, 0, 0, Orientation.HORIZONTAL);
        Ship3D frigate2 = ShipFactory3D.create(ShipType.FRIGATE, 9, 9, Orientation.HORIZONTAL);
        Ship3D frigate3 = ShipFactory3D.create(ShipType.FRIGATE, 4, 9, Orientation.HORIZONTAL);
        Ship3D frigate4 = ShipFactory3D.create(ShipType.FRIGATE, 6, 4, Orientation.HORIZONTAL);

        board.addShip(carrier);
        board.addShip(submarine1);
        board.addShip(submarine2);
        board.addShip(destroyer1);
        board.addShip(destroyer2);
        board.addShip(destroyer3);
        board.addShip(frigate1);
        board.addShip(frigate2);
        board.addShip(frigate3);
        board.addShip(frigate4);

        // Sample states: a water miss, a hit cell, and a sunk ship.
        board.addMark(ShotMark3D.waterMiss(2, 5));
        submarine1.markHit(0);
        frigate2.markHit(0); // frigate only has 1 cell -> immediately sunk

        StackPane root = new StackPane(board);
        Scene scene = new Scene(root, 800, 700);
        stage.setTitle("Battleship 3D Shapes Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}