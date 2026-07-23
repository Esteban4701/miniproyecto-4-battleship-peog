package com.example.battleship.controller.persistence;

import com.example.battleship.model.Game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists a {@link Game} to disk (HU-5), split across two files:
 * <ul>
 *   <li>{@code saved-game.ser} -- the whole {@code Game} object, written
 *       with plain Java serialization. Every model class it reaches
 *       ({@code Board}, {@code Player}, {@code Ship}, {@code ShootingStrategy}...)
 *       is already {@code Serializable}, so this single call captures
 *       the complete game state: both fleets, every cell's state, and
 *       whose turn it is.</li>
 *   <li>{@code saved-game-summary.txt} -- a small, human-readable plain
 *       text file with just the nickname and each side's sunk-ship
 *       count, kept separate from the serialized state as its own
 *       independent record of where the match stands.</li>
 * </ul>
 * <p>
 * Files live under a folder in the user's home directory rather than
 * the working directory, so where the game happens to have been
 * launched from doesn't affect whether a previous save can be found.
 * </p>
 */
public final class SavedGameRepository {

    private static final Path SAVE_DIRECTORY = Path.of(System.getProperty("user.home"), ".batalla-naval");
    private static final Path GAME_FILE = SAVE_DIRECTORY.resolve("saved-game.ser");
    private static final Path SUMMARY_FILE = SAVE_DIRECTORY.resolve("saved-game-summary.txt");

    private SavedGameRepository() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Writes both the serialized game and the plain-text summary,
     * overwriting any previous save.
     *
     * @param game the game to save
     * @throws IOException if either file can't be written
     */
    public static void save(Game game) throws IOException {
        Files.createDirectories(SAVE_DIRECTORY);
        saveSerializedGame(game);
        saveSummaryText(game);
    }

    private static void saveSerializedGame(Game game) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(GAME_FILE.toFile())))) {
            out.writeObject(game);
        }
    }

    private static void saveSummaryText(Game game) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(SUMMARY_FILE.toFile()))) {
            writer.println("Nickname: " + game.getHuman().getNickname());
            writer.println("Barcos hundidos (jugador): " + game.getHuman().countSunkShips() + "/10");
            writer.println("Barcos hundidos (maquina): " + game.getMachine().countSunkShips() + "/10");
            writer.println("Turno actual: " + game.getCurrentTurn());
        }
    }

    /** @return {@code true} if a saved game exists on disk right now */
    public static boolean hasSavedGame() {
        return Files.exists(GAME_FILE);
    }

    /**
     * Reads back the previously saved {@link Game}, exactly as it was
     * when {@link #save} was called -- both boards, every ship's hit
     * history, and whose turn it is. Model-level state only: the
     * caller is responsible for rebuilding the matching 3D shapes and
     * re-attaching any listeners (the model's own listeners are
     * intentionally not part of what gets saved).
     *
     * @return the restored game
     * @throws IOException            if the save file can't be read
     * @throws ClassNotFoundException if the save file doesn't match the current model classes
     */
    public static Game load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(GAME_FILE.toFile())))) {
            return (Game) in.readObject();
        }
    }

    /** Deletes any saved game -- called once a match is actually finished, so a completed game can't be "continued". */
    public static void deleteSavedGame() throws IOException {
        Files.deleteIfExists(GAME_FILE);
        Files.deleteIfExists(SUMMARY_FILE);
    }
}
