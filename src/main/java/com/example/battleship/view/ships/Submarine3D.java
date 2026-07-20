package com.example.battleship.view.ships;

import com.example.battleship.view.Config3D;
import com.example.battleship.view.Orientation;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Submarine: occupies 3 cells.
 * <p>
 * Built the same "printed layer by layer" way as the destroyer, but
 * instead of a monotonic taper the hull radius follows a parabola that
 * opens downward: narrow at the bow, widest at the middle, narrow again
 * at the stern -- the classic torpedo silhouette. On top of that hull
 * it carries a sail (conning tower) with a periscope and radar, a bow
 * sonar dome, bow planes, a stern rudder/plane cross with a propeller
 * hub, and portholes glued to the hull's real curved surface.
 * </p>
 */
public class Submarine3D extends Ship3D {

    private static final Color HULL_COLOR = Color.rgb(55, 70, 65);
    private static final Color SAIL_COLOR = Color.rgb(48, 62, 58);
    private static final Color DARK_METAL_COLOR = Color.rgb(15, 15, 15);
    private static final Color FIN_COLOR = Color.rgb(40, 52, 49);

    /** Y of the hull's centerline; everything else is measured from here. */
    private static final double HULL_CENTER_Y = -10;

    /** Number of "printed" cross-section slices used to shape the hull along X. */
    private static final int HULL_SLICES = 8;

    private static final double MAX_RADIUS = 13;
    private static final double MIN_RADIUS = 2;

    public Submarine3D(int row, int column, Orientation orientation) {
        super(3, row, column, orientation);
    }

    @Override
    protected Group buildHull() {
        // Local variables, not instance fields: Ship3D's constructor calls
        // buildHull() before Destroyer/Submarine's own field initializers
        // would run, so anything derived from shipCenterX() must be
        // computed here.
        double hullLength = 3 * Config3D.CELL_SIZE - 6;
        double bowTipX = shipCenterX() - hullLength / 2.0;
        double sternTipX = shipCenterX() + hullLength / 2.0;

        Group group = new Group();

        buildPressureHull(group, hullLength, bowTipX);
        double sailX = buildSail(group, bowTipX, hullLength);
        buildBowSonarDome(group, bowTipX);
        buildBowPlanes(group, bowTipX);
        buildSternCross(group, sternTipX);
        buildPortholes(group, hullLength, bowTipX);

        return group;
    }

    /**
     * The hull's radius at a given "printed slice" index: a downward
     * parabola over t in [0, 1] (bow to stern), narrow at both ends and
     * widest at the middle -- the torpedo shape.
     */
    private double radiusAtIndex(int index) {
        double t = (double) index / (HULL_SLICES - 1);
        double factor = 1 - Math.pow(2 * t - 1, 2);
        return Math.max(MIN_RADIUS, MAX_RADIUS * factor);
    }

    /** Same radius formula, but looked up by X position -- used to glue details to the real hull surface. */
    private double radiusAt(double x, double hullLength, double bowTipX) {
        double sliceLength = hullLength / HULL_SLICES;
        int index = (int) Math.floor((x - bowTipX) / sliceLength);
        index = Math.max(0, Math.min(HULL_SLICES - 1, index));
        return radiusAtIndex(index);
    }

    /** Pressure hull: a row of tapered cylinders lying along X, forming the torpedo silhouette. */
    private void buildPressureHull(Group group, double hullLength, double bowTipX) {
        double sliceLength = hullLength / HULL_SLICES;
        for (int i = 0; i < HULL_SLICES; i++) {
            double radius = radiusAtIndex(i);
            double xCenter = bowTipX + sliceLength * (i + 0.5);

            Cylinder slice = new Cylinder(radius, sliceLength + 0.3);
            slice.setMaterial(Config3D.intactHullMaterial(HULL_COLOR));
            slice.setRotationAxis(Rotate.Z_AXIS);
            slice.setRotate(90);
            slice.setTranslateX(xCenter);
            slice.setTranslateY(HULL_CENTER_Y);
            group.getChildren().add(slice);
        }
    }

    /** Sail (conning tower), set slightly forward of center, with a window, periscope and radar. */
    private double buildSail(Group group, double bowTipX, double hullLength) {
        double sailX = shipCenterX() - 3;
        double sailRadius = radiusAt(sailX, hullLength, bowTipX);
        double sailHeight = 15;
        double sailBottomY = HULL_CENTER_Y - sailRadius;
        double sailCenterY = sailBottomY - sailHeight / 2.0;

        Box sail = new Box(16, sailHeight, 8);
        sail.setMaterial(Config3D.intactHullMaterial(SAIL_COLOR));
        sail.setTranslateX(sailX);
        sail.setTranslateY(sailCenterY);
        group.getChildren().add(sail);

        Box window = new Box(1, 1.5, 6);
        window.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        window.setTranslateX(sailX - 8.5);
        window.setTranslateY(sailCenterY);
        group.getChildren().add(window);

        double sailTopY = sailCenterY - sailHeight / 2.0;

        Box periscope = new Box(1.5, 14, 1.5);
        periscope.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        periscope.setTranslateX(sailX - 3);
        periscope.setTranslateY(sailTopY - 7);
        group.getChildren().add(periscope);

        Box radar = new Box(1, 4, 8);
        radar.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        radar.setTranslateX(sailX + 3);
        radar.setTranslateY(sailTopY - 2);
        group.getChildren().add(radar);

        return sailX;
    }

    /** Small sonar dome bulging out at the very bow tip. */
    private void buildBowSonarDome(Group group, double bowTipX) {
        Sphere dome = new Sphere(3.5);
        dome.setMaterial(Config3D.intactHullMaterial(HULL_COLOR.darker()));
        dome.setTranslateX(bowTipX + 2);
        dome.setTranslateY(HULL_CENTER_Y + 3);
        group.getChildren().add(dome);
    }

    /** A pair of small bow planes (fins) sticking out of the hull near the bow. */
    private void buildBowPlanes(Group group, double bowTipX) {
        Box planes = new Box(2, 1, 24);
        planes.setMaterial(Config3D.intactHullMaterial(FIN_COLOR));
        planes.setTranslateX(bowTipX + 22);
        planes.setTranslateY(HULL_CENTER_Y);
        group.getChildren().add(planes);
    }

    /** Stern control surfaces: a plus-shaped cross of fins plus the propeller hub. */
    private void buildSternCross(Group group, double sternTipX) {
        double crossX = sternTipX - 8;

        Box verticalFin = new Box(2, 20, 2);
        verticalFin.setMaterial(Config3D.intactHullMaterial(FIN_COLOR));
        verticalFin.setTranslateX(crossX);
        verticalFin.setTranslateY(HULL_CENTER_Y);
        group.getChildren().add(verticalFin);

        Box horizontalFin = new Box(2, 2, 20);
        horizontalFin.setMaterial(Config3D.intactHullMaterial(FIN_COLOR));
        horizontalFin.setTranslateX(crossX);
        horizontalFin.setTranslateY(HULL_CENTER_Y);
        group.getChildren().add(horizontalFin);

        Sphere hub = new Sphere(2.5);
        hub.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        hub.setTranslateX(sternTipX - 1);
        hub.setTranslateY(HULL_CENTER_Y);
        group.getChildren().add(hub);
    }

    /** A row of small dark portholes along each side of the hull, glued to its actual curved surface. */
    private void buildPortholes(Group group, double hullLength, double bowTipX) {
        for (double portholeX : new double[]{-4, 10, 24, 38, 52, 66, 80}) {
            double radius = radiusAt(portholeX, hullLength, bowTipX);
            for (int side = -1; side <= 1; side += 2) {
                Box porthole = new Box(1, 2, 2);
                porthole.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
                porthole.setTranslateX(portholeX);
                porthole.setTranslateY(HULL_CENTER_Y);
                porthole.setTranslateZ(side * radius);
                group.getChildren().add(porthole);
            }
        }
    }
}