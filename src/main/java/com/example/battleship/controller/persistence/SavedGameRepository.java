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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * <p>
 * {@link #saveAsync} runs the actual write on a dedicated background
 * thread (see {@link #SAVE_EXECUTOR}), so autosaving after every shot
 * never blocks the JavaFX Application Thread.
 * </p>
 */
public final class SavedGameRepository {

    private static final Path SAVE_DIRECTORY = Path.of(System.getProperty("user.home"), ".batalla-naval");
    private static final Path GAME_FILE = SAVE_DIRECTORY.resolve("saved-game.ser");
    private static final Path SUMMARY_FILE = SAVE_DIRECTORY.resolve("saved-game-summary.txt");

    /**
     * A single dedicated background thread for every save. Writing to
     * disk is blocking I/O; running it on the JavaFX Application Thread
     * (the same thread driving the camera and every animation) would
     * freeze the interface for however long the write takes. A single
     * thread, not a pool, is deliberate: save requests always run one
     * at a time, in the order they were submitted, so two autosaves can
     * never race to write the same file at once.
     */
    private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "game-autosave");
        thread.setDaemon(true); // never keeps the JVM alive on its own
        return thread;
    });

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

    /**
     * Same as {@link #save(Game)}, but performed on the dedicated
     * background thread ({@link #SAVE_EXECUTOR}) instead of the
     * caller's own -- meant to be called from the JavaFX Application
     * Thread (see {@code CombatController#autoSave}) without blocking
     * it on disk I/O. Any {@link IOException} is caught and logged on
     * the background thread; by the time it could occur, this method
     * has already returned, so there's no way to propagate it back to
     * the caller.
     *
     * @param game the game to save
     */
    public static void saveAsync(Game game) {
        SAVE_EXECUTOR.submit(() -> {
            try {
                save(game);
            } catch (IOException e) {
                System.err.println("Could not auto-save the game: " + e.getMessage());
            }
        });
    }

    /** Writes the whole {@link Game} object to {@link #GAME_FILE} via plain Java serialization, overwriting any previous save. */
    private static void saveSerializedGame(Game game) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(GAME_FILE.toFile())))) {
            out.writeObject(game);
        }
    }

    /** Writes the plain-text nickname/sunk-ships/turn summary to {@link #SUMMARY_FILE}, overwriting any previous one. */
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

    /**
     * Same as {@link #deleteSavedGame()}, but submitted to the same
     * {@link #SAVE_EXECUTOR} queue as {@link #saveAsync}, instead of
     * running immediately on the caller's own thread.
     * <p>
     * This matters specifically because the queue is FIFO and
     * single-threaded: if an earlier {@link #saveAsync} call is still
     * waiting to run when a match ends, deleting synchronously (on the
     * calling thread) could finish BEFORE that queued save actually
     * writes the file -- silently recreating a "saved game" for a
     * match that already ended. Submitting to delete to the same
     * queue instead guarantees it always runs after every save
     * requested before it, no matter how the two threads happen to be
     * scheduled.
     * </p>
     */
    public static void deleteSavedGameAsync() {
        SAVE_EXECUTOR.submit(() -> {
            try {
                deleteSavedGame();
            } catch (IOException e) {
                System.err.println("Could not delete the saved game: " + e.getMessage());
            }
        });
    }
}