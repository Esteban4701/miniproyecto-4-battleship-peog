package com.example.battleship.model.player;

import com.example.battleship.model.board.Board;
import com.example.battleship.model.board.CellState;
import com.example.battleship.model.board.Position;

import java.io.Serial;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A smarter alternative to {@link RandomShootingStrategy}: while there
 * is no damaged-but-not-yet-sunk ship on the board, it behaves exactly
 * like plain random targeting. As soon as a shot lands a
 * {@link CellState#HIT} (a hit that hasn't sunk the ship yet), it
 * switches to "hunting":
 * <ul>
 *   <li>With a single, isolated hit, the ship's orientation isn't known
 *       yet, so all four orthogonal neighbors (up/down/left/right) are
 *       candidates -- same as a human player probing around a first hit.</li>
 *   <li>Once two or more hits turn out to be orthogonally connected
 *       (almost certainly the same ship, since every ship is a straight
 *       line), the orientation IS known -- only the two cells extending
 *       that same line are tried first. If both ends turn out to be
 *       unusable (the edge of the board, or already shot), it falls
 *       back to probing every orthogonal neighbor of every hit cell in
 *       the cluster, the same as the single-hit case.</li>
 * </ul>
 * <p>
 * Deliberately stateless: instead of remembering what it hit last turn,
 * it re-scans {@code opponentBoard} on every call for cells still in
 * the {@code HIT} state, and flood-fills each cluster of connected hits
 * to find its shape. The board itself is always the single source of
 * truth for "what's still being hunted right now."
 * </p>
 */
public class HuntShootingStrategy implements ShootingStrategy {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The four orthogonal directions a hit ship's next segment could be in. */
    private static final int[][] ORTHOGONAL_OFFSETS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    private final Random random;
    private final RandomShootingStrategy fallback;

    /** Uses a new, non-seeded {@link Random}. */
    public HuntShootingStrategy() {
        this(new Random());
    }

    /**
     * @param random the random source to draw from (shared with the
     *               fallback random strategy, so a seeded {@code Random}
     *               makes this whole strategy's behavior reproducible for testing)
     */
    public HuntShootingStrategy(Random random) {
        this.random = random;
        this.fallback = new RandomShootingStrategy(random);
    }

    /** {@inheritDoc} */
    @Override
    public Position chooseTarget(Board opponentBoard) {
        List<Position> huntCandidates = findHuntCandidates(opponentBoard);
        if (!huntCandidates.isEmpty()) {
            return huntCandidates.get(random.nextInt(huntCandidates.size()));
        }
        return fallback.chooseTarget(opponentBoard);
    }

    /** Finds every connected cluster of hit cells and collects each one's own hunting candidates. */
    private List<Position> findHuntCandidates(Board board) {
        List<Position> candidates = new ArrayList<>();
        Set<Position> visited = new HashSet<>();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Position position = new Position(row, column);
                if (isHit(board, position) && !visited.contains(position)) {
                    List<Position> cluster = collectHitCluster(board, position, visited);
                    candidates.addAll(candidatesForCluster(board, cluster));
                }
            }
        }
        return candidates;
    }

    /** @return {@code true} if {@code position} has been hit but hasn't sunk its ship yet */
    private boolean isHit(Board board, Position position) {
        return board.getCell(position).getState() == CellState.HIT;
    }

    /**
     * Flood-fills every orthogonally-connected hit cell starting from
     * {@code start}. Since every ship is a straight line, a cluster
     * found this way is (in practice) always exactly one damaged ship.
     */
    private List<Position> collectHitCluster(Board board, Position start, Set<Position> visited) {
        List<Position> cluster = new ArrayList<>();
        Deque<Position> pending = new ArrayDeque<>();
        pending.add(start);
        visited.add(start);

        while (!pending.isEmpty()) {
            Position current = pending.poll();
            cluster.add(current);
            for (Position neighbor : orthogonalNeighborsInBounds(current)) {
                if (!visited.contains(neighbor) && isHit(board, neighbor)) {
                    visited.add(neighbor);
                    pending.add(neighbor);
                }
            }
        }
        return cluster;
    }

    /**
     * A lone hit means the orientation isn't known yet -- probe all
     * four sides. Two or more connected hits mean the orientation IS
     * known, so the two cells extending that established line are tried
     * first -- but if BOTH ends are unusable (off the board, or already
     * shot), that line is exhausted without having sunk the ship, so
     * fall back to probing every orthogonal neighbor of every hit cell
     * in the cluster, the same as the single-hit case.
     */
    private List<Position> candidatesForCluster(Board board, List<Position> cluster) {
        if (cluster.size() == 1) {
            return unshotOrthogonalNeighbors(board, cluster.get(0));
        }
        List<Position> lineCandidates = lineExtensionCandidates(board, cluster);
        if (!lineCandidates.isEmpty()) {
            return lineCandidates;
        }
        return allOrthogonalNeighborsOf(board, cluster);
    }

    /** Every unshot orthogonal neighbor of every cell in the cluster, combined -- the fallback once both line-ends are exhausted. */
    private List<Position> allOrthogonalNeighborsOf(Board board, List<Position> cluster) {
        List<Position> candidates = new ArrayList<>();
        for (Position hit : cluster) {
            for (Position neighbor : unshotOrthogonalNeighbors(board, hit)) {
                if (!candidates.contains(neighbor)) {
                    candidates.add(neighbor);
                }
            }
        }
        return candidates;
    }

    /** @return every orthogonal neighbor of {@code hit} that is still within the board and hasn't been shot at yet */
    private List<Position> unshotOrthogonalNeighbors(Board board, Position hit) {
        List<Position> candidates = new ArrayList<>();
        for (Position neighbor : orthogonalNeighborsInBounds(hit)) {
            if (board.canBeShotAt(neighbor)) {
                candidates.add(neighbor);
            }
        }
        return candidates;
    }

    /** The two cells just past each end of an already-established line of hits. */
    private List<Position> lineExtensionCandidates(Board board, List<Position> cluster) {
        boolean sameRow = cluster.stream().allMatch(p -> p.row() == cluster.get(0).row());
        List<Position> candidates = new ArrayList<>();

        if (sameRow) {
            int row = cluster.get(0).row();
            int minColumn = cluster.stream().mapToInt(Position::column).min().orElseThrow();
            int maxColumn = cluster.stream().mapToInt(Position::column).max().orElseThrow();
            addIfInBoundsAndShootable(board, row, minColumn - 1, candidates);
            addIfInBoundsAndShootable(board, row, maxColumn + 1, candidates);
        } else {
            int column = cluster.get(0).column();
            int minRow = cluster.stream().mapToInt(Position::row).min().orElseThrow();
            int maxRow = cluster.stream().mapToInt(Position::row).max().orElseThrow();
            addIfInBoundsAndShootable(board, minRow - 1, column, candidates);
            addIfInBoundsAndShootable(board, maxRow + 1, column, candidates);
        }
        return candidates;
    }

    /** @return the (up to four) orthogonal neighbors of {@code center} that fall within the board's 0-9 range */
    private List<Position> orthogonalNeighborsInBounds(Position center) {
        List<Position> neighbors = new ArrayList<>(4);
        for (int[] offset : ORTHOGONAL_OFFSETS) {
            int row = center.row() + offset[0];
            int column = center.column() + offset[1];
            if (row >= 0 && row < Board.SIZE && column >= 0 && column < Board.SIZE) {
                neighbors.add(new Position(row, column));
            }
        }
        return neighbors;
    }

    /** Adds {@code (row, column)} to {@code candidates} if it's within the board and hasn't been shot at yet; otherwise does nothing. */
    private void addIfInBoundsAndShootable(Board board, int row, int column, List<Position> candidates) {
        if (row < 0 || row >= Board.SIZE || column < 0 || column >= Board.SIZE) {
            return;
        }
        Position position = new Position(row, column);
        if (board.canBeShotAt(position)) {
            candidates.add(position);
        }
    }

}
