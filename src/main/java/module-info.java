module com.example.battleship {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.battleship to javafx.fxml;
    exports com.example.battleship;
    exports com.example.battleship.view.ships;
    opens com.example.battleship.view.ships to javafx.fxml;
    exports com.example.battleship.view;
    opens com.example.battleship.view to javafx.fxml;
    exports com.example.battleship.view.assets;
    opens com.example.battleship.view.assets to javafx.fxml;
}