package com.example.battleship;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Application entry point. Loads {@code main-view.fxml} -- the single
 * screen the whole game runs on -- into the primary stage.
 * <p>
 * {@link #loadMainScreen(Stage)} is also used by
 * {@link com.example.battleship.controller.GameScreenController} to
 * return to a fresh start menu after a game ends, by reloading the FXML
 * from scratch rather than trying to manually reset every piece of
 * 3D/game state by hand.
 * </p>
 */
public class Main extends Application {

    private static final String FXML_PATH = "/com/example/battleship/main-view.fxml";
    private static final String CSS_PATH = "/com/example/battleship/styles.css";

    @Override
    public void start(Stage stage) throws IOException {
        loadMainScreen(stage);
        stage.setTitle("Batalla Naval");
        stage.show();
    }

    /**
     * (Re)loads a fresh main screen into {@code stage}.
     *
     * @param stage the window to load the screen into
     * @throws IOException if the FXML fails to load
     */
    public static void loadMainScreen(Stage stage) throws IOException {
        URL fxmlUrl = Main.class.getResource(FXML_PATH);
        if (fxmlUrl == null) {
            throw new IOException("Could not find " + FXML_PATH + " on the classpath");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 1100, 800);

        URL cssUrl = Main.class.getResource(CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
