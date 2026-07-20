package com.example.battleship.view.assets;

import com.example.battleship.view.Config3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * A white ring that hovers just above a board cell, used to highlight
 * whichever cell the mouse is currently over.
 * <p>
 * Built as a real 3D ring mesh ({@link MeshView} + {@link TriangleMesh}),
 * not a 2D {@code Circle}: a 2D shape's rendering path isn't meant to be
 * repositioned every mouse-move inside a 3D {@code SubScene}, and doing
 * so left visible "ghosts" at its previous positions instead of cleanly
 * moving. A proper 3D mesh uses the same rendering path as every other
 * ship and water tile in the scene, so it updates cleanly.
 * </p>
 * <p>
 * A single instance is meant to be reused per board: call
 * {@link #moveToCell(int, int)} to reposition and show it, and
 * {@link #hide()} when the mouse leaves the board.
 * </p>
 */
public class HoverMarker3D extends MeshView {

    private static final double OUTER_RADIUS = 16;
    private static final double INNER_RADIUS = 13;
    private static final int SEGMENTS = 24;

    /** How far above the water surface the ring floats, so it never z-fights with the water tiles. */
    private static final double HOVER_Y = -3;

    public HoverMarker3D() {
        TriangleMesh mesh = buildRingMesh(INNER_RADIUS, OUTER_RADIUS, SEGMENTS);
        setMesh(mesh);

        PhongMaterial material = new PhongMaterial(Color.WHITE);
        material.setSpecularColor(Color.WHITE);
        setMaterial(material);
        setCullFace(CullFace.NONE); // visible from both above and below, whatever the camera angle

        setTranslateY(HOVER_Y);
        setMouseTransparent(true); // never itself intercepts the hover pick
        setVisible(false);
    }

    /** Repositions the ring over the given cell and makes it visible. */
    public void moveToCell(int row, int column) {
        setTranslateX(column * Config3D.CELL_SIZE);
        setTranslateZ(row * Config3D.CELL_SIZE);
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    /** Builds a flat ring (annulus) lying in the X/Z plane, centered on the local origin. */
    private static TriangleMesh buildRingMesh(double innerRadius, double outerRadius, int segments) {
        TriangleMesh mesh = new TriangleMesh();

        float[] points = new float[segments * 2 * 3];
        for (int i = 0; i < segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            float cosValue = (float) Math.cos(angle);
            float sinValue = (float) Math.sin(angle);

            int outerBase = i * 6;
            points[outerBase] = (float) (outerRadius * cosValue);
            points[outerBase + 1] = 0f;
            points[outerBase + 2] = (float) (outerRadius * sinValue);

            int innerBase = outerBase + 3;
            points[innerBase] = (float) (innerRadius * cosValue);
            points[innerBase + 1] = 0f;
            points[innerBase + 2] = (float) (innerRadius * sinValue);
        }

        float[] texCoords = new float[]{0f, 0f};

        int trianglesPerSegment = 2;
        int vertsPerTriangle = 3;
        int valuesPerVert = 2; // point index + tex coord index
        int[] faces = new int[segments * trianglesPerSegment * vertsPerTriangle * valuesPerVert];

        int faceIndex = 0;
        for (int i = 0; i < segments; i++) {
            int outerCurrent = i * 2;
            int innerCurrent = i * 2 + 1;
            int nextIndex = (i + 1) % segments;
            int outerNext = nextIndex * 2;
            int innerNext = nextIndex * 2 + 1;

            faceIndex = writeTriangle(faces, faceIndex, outerCurrent, innerCurrent, outerNext);
            faceIndex = writeTriangle(faces, faceIndex, innerCurrent, innerNext, outerNext);
        }

        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);

        return mesh;
    }

    /** Writes one triangle (3 vertices, each as a point-index/tex-index pair) into the faces array. */
    private static int writeTriangle(int[] faces, int startIndex, int pointA, int pointB, int pointC) {
        int index = startIndex;
        faces[index] = pointA;
        faces[index + 1] = 0;
        faces[index + 2] = pointB;
        faces[index + 3] = 0;
        faces[index + 4] = pointC;
        faces[index + 5] = 0;
        return index + 6;
    }
}
