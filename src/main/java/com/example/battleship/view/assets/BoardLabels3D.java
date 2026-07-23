package com.example.battleship.view.assets;

import com.example.battleship.view.BoardView3D;
import com.example.battleship.view.Config3D;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Builds the column-letter (A..J) and row-number (1..10) labels that
 * sit just outside a board's two edges, like a spreadsheet's headers.
 * <p>
 * Each label is baked once into a small transparent-background image
 * (via {@link Text#snapshot}) and applied as a texture on a flat 3D
 * quad lying on the water plane -- the same safe technique used for the
 * sky gradient: real 3D geometry with a texture, never a live 2D
 * {@code Text} node sitting directly in the 3D scene graph.
 * </p>
 * <p>
 * Call {@link #build(Layout, Board)} once per board (it returns a fresh
 * {@code Group} each time, since a node can only belong to one parent)
 * and add the result as a child of that board's own group, so the
 * labels line up with that board's local 0..360 coordinates. The
 * {@link Board} argument picks which quad texture mapping to use --
 * the player's board and the machine's board sit on opposite edges of
 * the field, so each needs its own confirmed-correct orientation
 * rather than sharing one.
 * </p>
 */
public final class BoardLabels3D {

    private static final double LABEL_HEIGHT = 22;
    private static final double LABEL_OFFSET = Config3D.CELL_SIZE * 1.3;
    private static final double LABEL_Y = -3;
    private static final double FONT_SIZE = 64;

    private BoardLabels3D() {
        // Utility class: not meant to be instantiated.
    }

    /** Which board these labels are for -- picks which quad texture mapping to use. */
    public enum Board {
        /** The player's own board. */
        PLAYER,
        /** The machine's board. */
        MACHINE
    }

    /**
     * Which edges the letters/numbers sit on, and which direction each
     * one reads in. The four flags are independent of each other --
     * mix and match freely per board.
     *
     * @param lettersOnSouthEdge letters sit past row 10 instead of before row 1
     * @param numbersOnEastEdge  numbers sit past column J instead of before column A
     * @param reverseLetterOrder letters read J..A left-to-right instead of A..J
     * @param reverseNumberOrder numbers read 10..1 top-to-bottom instead of 1..10
     */
    public record Layout(boolean lettersOnSouthEdge, boolean numbersOnEastEdge,
                         boolean reverseLetterOrder, boolean reverseNumberOrder) {

        /** Letters past row 10, numbers past column J, letters read J..A -- the confirmed machine-board layout. */
        public static Layout standard() {
            return new Layout(false, true, true, false);
        }

        /**
         * The confirmed-good layout for the player's own board: letters
         * past row 10 (south edge) in normal A..J order, numbers before
         * column A (west edge) but counting down 10..1.
         */
        public static Layout player() {
            return new Layout(true, false, false, true);
        }
    }

    /**
     * Builds one full set of column-letter and row-number labels for a
     * board, positioned in that board's own local 0..360 coordinates
     * (relative to whatever group the caller adds the result to).
     *
     * @param layout which edges/reading order to use (see {@link Layout})
     * @param board  which board these labels are for (picks the texture mapping)
     * @return a fresh group containing every letter and number label
     */
    public static Group build(Layout layout, Board board) {
        Group group = new Group();

        double lettersZ = layout.lettersOnSouthEdge()
                ? (BoardView3D.ROWS - 1) * Config3D.CELL_SIZE + LABEL_OFFSET
                : -LABEL_OFFSET;
        double numbersX = layout.numbersOnEastEdge()
                ? (BoardView3D.COLUMNS - 1) * Config3D.CELL_SIZE + LABEL_OFFSET
                : -LABEL_OFFSET;

        for (int column = 0; column < BoardView3D.COLUMNS; column++) {
            int letterIndex = layout.reverseLetterOrder() ? (BoardView3D.COLUMNS - 1 - column) : column;
            String letter = String.valueOf((char) ('A' + letterIndex));

            MeshView label = createLabel(letter, board);
            label.setTranslateX(column * Config3D.CELL_SIZE);
            label.setTranslateZ(lettersZ);
            label.setTranslateY(LABEL_Y);
            group.getChildren().add(label);
        }

        for (int row = 0; row < BoardView3D.ROWS; row++) {
            int numberValue = layout.reverseNumberOrder() ? (BoardView3D.ROWS - row) : (row + 1);
            String number = String.valueOf(numberValue);

            MeshView label = createLabel(number, board);
            label.setTranslateX(numbersX);
            label.setTranslateZ(row * Config3D.CELL_SIZE);
            label.setTranslateY(LABEL_Y);
            group.getChildren().add(label);
        }

        return group;
    }

    /** Builds one label's flat textured quad, sized to keep the baked text image's own aspect ratio. */
    private static MeshView createLabel(String text, Board board) {
        Image texture = renderTextTexture(text);
        double aspect = texture.getWidth() / texture.getHeight();
        double height = LABEL_HEIGHT;
        double width = height * aspect;

        MeshView quad = board == Board.PLAYER
                ? buildFlatQuadForPlayer(width, height)
                : buildFlatQuadForMachine(width, height);

        PhongMaterial material = new PhongMaterial();
        // diffuseMap, not selfIlluminationMap: JavaFX's selfIlluminationMap
        // ignores the image's alpha channel entirely, so the "transparent"
        // background rendered as an opaque solid black square instead of
        // showing the water through it. diffuseMap respects alpha
        // correctly, giving a clean cutout with just the glyph visible.
        material.setDiffuseColor(Color.WHITE);
        material.setDiffuseMap(texture);
        quad.setMaterial(material);
        quad.setCullFace(CullFace.NONE);

        return quad;
    }

    /** Renders a single character/word to a small transparent-background image. */
    private static Image renderTextTexture(String text) {
        Text textNode = new Text(text);
        textNode.setFont(Font.font("Arial", FontWeight.BOLD, FONT_SIZE));
        textNode.setFill(Color.WHITE);

        Bounds bounds = textNode.getLayoutBounds();
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(),
                bounds.getWidth(), bounds.getHeight()));

        return textNode.snapshot(params, null);
    }

    /**
     * Flat quad for the player's board labels: this texCoords mapping
     * (both U and V flipped relative to the "obvious" one) is the
     * confirmed-correct orientation for that board's position/camera
     * angle.
     */
    private static MeshView buildFlatQuadForPlayer(double width, double depth) {
        return buildFlatQuad(width, depth, new float[]{
                0, 1,
                1, 1,
                1, 0,
                0, 0,
        });
    }

    /**
     * Flat quad for the machine's board labels. The machine board sits
     * on the opposite side of the field from the player's, so it needs
     * its own texCoords mapping rather than reusing the player's --
     * starting from the un-flipped mapping here; adjust this array the
     * same way the player one was tuned if it still reads wrong.
     */
    /**
     * Flat quad for the machine's board labels: the U (horizontal) axis
     * flipped relative to the un-tuned starting mapping -- that
     * starting version read mirrored left-right. Still V-unflipped; if
     * this comes out upside-down instead of mirrored, flip the second
     * number of each pair too (the same "both flipped" pattern already
     * confirmed correct for the player's board).
     */
    private static MeshView buildFlatQuadForMachine(double width, double depth) {
        return buildFlatQuad(width, depth, new float[]{
                1, 0,
                0, 0,
                0, 1,
                1, 1,
        });
    }

    /** A single flat rectangle (2 triangles, no thickness) lying in the X/Z plane. */
    private static MeshView buildFlatQuad(double width, double depth, float[] texCoords) {
        float halfWidth = (float) (width / 2.0);
        float halfDepth = (float) (depth / 2.0);

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(new float[]{
                -halfWidth, 0, -halfDepth, // 0
                halfWidth, 0, -halfDepth,  // 1
                halfWidth, 0, halfDepth,   // 2
                -halfWidth, 0, halfDepth,  // 3
        });
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(new int[]{
                0, 0, 1, 1, 2, 2,
                0, 0, 2, 2, 3, 3,
        });

        return new MeshView(mesh);
    }
}