module com.example.battleship {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.battleship to javafx.fxml, javafx.graphics;
    exports com.example.battleship;

    exports com.example.battleship.view.ships;
    opens com.example.battleship.view.ships to javafx.fxml;

    exports com.example.battleship.view;
    opens com.example.battleship.view to javafx.fxml;

    exports com.example.battleship.view.assets;
    opens com.example.battleship.view.assets to javafx.fxml;

    exports com.example.battleship.controller;
    opens com.example.battleship.controller to javafx.fxml;

    exports com.example.battleship.controller.event;
    opens com.example.battleship.controller.event to javafx.fxml;

    exports com.example.battleship.model;
    opens com.example.battleship.model to javafx.fxml;
    exports com.example.battleship.controller.persistence;
    opens com.example.battleship.controller.persistence to javafx.fxml;
    exports com.example.battleship.model.player;
    opens com.example.battleship.model.player to javafx.fxml;
}