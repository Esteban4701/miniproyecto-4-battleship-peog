package com.example.battleship.view.ships;

import com.example.battleship.model.ship.Orientation;
import com.example.battleship.view.Config3D;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;

/**
 * Destroyer: medium-sized ship, occupies 2 cells.
 * <p>
 * Built the same layered way as the frigate, but this time the taper
 * runs along the ship's full length (the X axis, which is also the
 * axis {@link Ship3D} uses to map cell indices): the hull is printed as
 * a series of thin cross-section "slices" from bow to stern, each one
 * wider than the last, so that seen from above the outline reads as a
 * parabola opening from a point at the bow toward a wide, flat stern --
 * combined with the same vertical flare as the frigate (narrow keel,
 * wide deck). The bridge sits at the exact center of the ship, with a
 * gun at both the bow and the stern.
 * </p>
 */
public class Destroyer3D extends Ship3D {

    private static final Color HULL_BASE_COLOR = Color.rgb(95, 98, 102);
    private static final Color HULL_MID_COLOR = Color.rgb(130, 133, 137);
    private static final Color DECK_COLOR = Color.rgb(190, 192, 195);
    private static final Color BRIDGE_COLOR = Color.rgb(200, 202, 205);
    private static final Color MAST_COLOR = Color.rgb(170, 172, 175);
    private static final Color DARK_METAL_COLOR = Color.rgb(15, 15, 15);
    private static final Color TURRET_COLOR = Color.rgb(75, 77, 80);
    private static final Color FUNNEL_COLOR = Color.rgb(55, 53, 50);
    private static final Color FLAG_COLOR = Color.rgb(180, 40, 40);

    /** Deck top sits at this Y; every on-deck detail is built up from here. */
    private static final double DECK_TOP_Y = -17;

    /** Number of "printed" cross-section slices used to taper the hull along X. */
    private static final int HULL_SLICES = 6;

    public Destroyer3D(int row, int column, Orientation orientation) {
        super(2, row, column, orientation);
    }

    @Override
    protected Group buildHull() {
        // NOTE: these are local variables, not instance fields. Ship3D's
        // constructor calls buildHull() before any of Destroyer3D's own
        // field initializers would run, so anything computed from
        // shipCenterX() must be computed here, inside this method, not
        // stored as a field initialized at declaration time.
        double hullLength = 2 * Config3D.CELL_SIZE - 6;
        double bowTipX = shipCenterX() - hullLength / 2.0;
        double sternTipX = shipCenterX() + hullLength / 2.0;

        Group group = new Group();

        // Keel -> lower hull -> deck: same vertical flare idea as the
        // frigate (narrow at the bottom, wide at the top), except now
        // each layer is ALSO tapered along X, bow to stern.
        buildTaperedLayer(group, -3, 6, 22, 4, HULL_BASE_COLOR, hullLength, bowTipX);
        buildTaperedLayer(group, -9, 6, 28, 5, HULL_MID_COLOR, hullLength, bowTipX);
        buildTaperedLayer(group, -14.5, 5, 34, 6, DECK_COLOR, hullLength, bowTipX);

        buildBowGun(group, bowTipX);
        buildSternGun(group, sternTipX);
        double bridgeX = buildBridge(group);
        buildMast(group, bridgeX);
        buildFunnel(group, bridgeX);
        buildPortholes(group, hullLength, bowTipX);
        buildFlagstaff(group, sternTipX);
        buildBowPlank(group, bowTipX);
        buildSternOverhang(group, sternTipX);

        return group;
    }

    /**
     * Builds one vertical hull layer as a row of {@code HULL_SLICES} boxes
     * along X, each wider (in Z, the beam) than the previous one -- a
     * parabola (quadratic growth) from {@code minBeam} at the bow to
     * {@code maxBeam} at the stern.
     */
    private void buildTaperedLayer(Group group, double centerY, double layerHeight,
                                   double maxBeam, double minBeam, Color color,
                                   double hullLength, double bowTipX) {
        double sliceLength = hullLength / HULL_SLICES;
        for (int i = 0; i < HULL_SLICES; i++) {
            double t = (double) i / (HULL_SLICES - 1);
            double beam = minBeam + (maxBeam - minBeam) * t * t;
            double xCenter = bowTipX + sliceLength * (i + 0.5);

            Box slice = new Box(sliceLength + 0.2, layerHeight, beam);
            slice.setMaterial(Config3D.intactHullMaterial(color));
            slice.setTranslateX(xCenter);
            slice.setTranslateY(centerY);
            group.getChildren().add(slice);
        }
    }

    /** Forward deck gun, barrel pointing toward the bow tip. */
    private void buildBowGun(Group group, double bowTipX) {
        double turretX = bowTipX + 11;

        Box turret = new Box(6, 4, 5);
        turret.setMaterial(Config3D.intactHullMaterial(TURRET_COLOR));
        turret.setTranslateX(turretX);
        turret.setTranslateY(DECK_TOP_Y - 2);
        group.getChildren().add(turret);

        Box barrel = new Box(8, 1.5, 1.5);
        barrel.setMaterial(Config3D.intactHullMaterial(TURRET_COLOR.darker()));
        barrel.setTranslateX(turretX - 6.5);
        barrel.setTranslateY(DECK_TOP_Y - 2);
        group.getChildren().add(barrel);
    }

    /** Aft deck gun, barrel pointing toward the stern. */
    private void buildSternGun(Group group, double sternTipX) {
        double turretX = sternTipX - 11;

        Box turret = new Box(6, 4, 5);
        turret.setMaterial(Config3D.intactHullMaterial(TURRET_COLOR));
        turret.setTranslateX(turretX);
        turret.setTranslateY(DECK_TOP_Y - 2);
        group.getChildren().add(turret);

        Box barrel = new Box(8, 1.5, 1.5);
        barrel.setMaterial(Config3D.intactHullMaterial(TURRET_COLOR.darker()));
        barrel.setTranslateX(turretX + 6.5);
        barrel.setTranslateY(DECK_TOP_Y - 2);
        group.getChildren().add(barrel);
    }

    /** Bridge with a single window band, centered on the ship. */
    private double buildBridge(Group group) {
        double bridgeX = shipCenterX();

        Box bridge = new Box(18, 10, 12);
        bridge.setMaterial(Config3D.intactHullMaterial(BRIDGE_COLOR));
        bridge.setTranslateX(bridgeX);
        bridge.setTranslateY(-22);
        group.getChildren().add(bridge);

        Box window = new Box(1, 1.5, 10);
        window.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        window.setTranslateX(bridgeX - 9.5);
        window.setTranslateY(-22);
        group.getChildren().add(window);

        return bridgeX;
    }

    /** Mast with a yard (spanning the beam) and a small radar panel, rising from the bridge. */
    private void buildMast(Group group, double bridgeX) {
        Box mast = new Box(2, 18, 2);
        mast.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        mast.setTranslateX(bridgeX);
        mast.setTranslateY(-36);
        group.getChildren().add(mast);

        Box yard = new Box(2, 2, 16);
        yard.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        yard.setTranslateX(bridgeX);
        yard.setTranslateY(-41);
        group.getChildren().add(yard);

        Box radar = new Box(1, 3, 6);
        radar.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        radar.setTranslateX(bridgeX + 1.5);
        radar.setTranslateY(-38);
        group.getChildren().add(radar);
    }

    /** Funnel (smokestack) just aft of the bridge, with a darker rim on top. */
    private void buildFunnel(Group group, double bridgeX) {
        double funnelX = bridgeX + 10;

        Box stack = new Box(6, 13, 6);
        stack.setMaterial(Config3D.intactHullMaterial(FUNNEL_COLOR));
        stack.setTranslateX(funnelX);
        stack.setTranslateY(DECK_TOP_Y - 6.5);
        group.getChildren().add(stack);

        Box rim = new Box(7, 1.5, 7);
        rim.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        rim.setTranslateX(funnelX);
        rim.setTranslateY(DECK_TOP_Y - 13.75);
        group.getChildren().add(rim);
    }

    /**
     * The beam (Z half-width) of a given hull layer at position {@code x},
     * matching exactly the slice that {@link #buildTaperedLayer} would have
     * drawn there -- used so details "stuck" to the hull (like portholes)
     * land right on its real surface instead of floating in space.
     */
    private double beamAt(double x, double hullLength, double bowTipX, double maxBeam, double minBeam) {
        double sliceLength = hullLength / HULL_SLICES;
        int index = (int) Math.floor((x - bowTipX) / sliceLength);
        index = Math.max(0, Math.min(HULL_SLICES - 1, index));
        double t = (double) index / (HULL_SLICES - 1);
        return minBeam + (maxBeam - minBeam) * t * t;
    }

    /** A row of small dark portholes along each side of the lower hull, glued to its actual surface. */
    private void buildPortholes(Group group, double hullLength, double bowTipX) {
        for (double portholeX : new double[]{2, 14, 26, 38, 50}) {
            double halfBeam = beamAt(portholeX, hullLength, bowTipX, 28, 5) / 2.0;
            for (int side = -1; side <= 1; side += 2) {
                Box porthole = new Box(2, 2, 1);
                porthole.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
                porthole.setTranslateX(portholeX);
                porthole.setTranslateY(-9);
                porthole.setTranslateZ(side * halfBeam);
                group.getChildren().add(porthole);
            }
        }
    }

    /** Thin flagstaff near the stern, with a small flag. */
    private void buildFlagstaff(Group group, double sternTipX) {
        double poleX = sternTipX - 5;

        Box pole = new Box(1, 10, 1);
        pole.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        pole.setTranslateX(poleX);
        pole.setTranslateY(DECK_TOP_Y - 5);
        group.getChildren().add(pole);

        Box flag = new Box(2, 2, 3);
        flag.setMaterial(Config3D.intactHullMaterial(FLAG_COLOR));
        flag.setTranslateX(poleX);
        flag.setTranslateZ(1.5);
        flag.setTranslateY(DECK_TOP_Y - 9);
        group.getChildren().add(flag);
    }

    /** Thin overhang jutting out past the bow tip, just above the waterline. */
    private void buildBowPlank(Group group, double bowTipX) {
        Box bowPlank = new Box(10, 2, 8);
        bowPlank.setMaterial(Config3D.intactHullMaterial(DECK_COLOR));
        bowPlank.setTranslateX(bowTipX - 5);
        bowPlank.setTranslateY(-14.5);
        group.getChildren().add(bowPlank);
    }

    /** Small flat fantail ledge past the stern, matching a typical flat destroyer stern. */
    private void buildSternOverhang(Group group, double sternTipX) {
        Box sternDeck = new Box(6, 2, 20);
        sternDeck.setMaterial(Config3D.intactHullMaterial(DECK_COLOR));
        sternDeck.setTranslateX(sternTipX + 3);
        sternDeck.setTranslateY(-14.5);
        group.getChildren().add(sternDeck);
    }
}