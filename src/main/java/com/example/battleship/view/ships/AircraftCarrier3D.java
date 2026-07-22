package com.example.battleship.view.ships;

import com.example.battleship.model.ship.Orientation;
import com.example.battleship.view.Config3D;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;

/**
 * Aircraft carrier: the largest ship in the fleet, occupies 4 cells.
 * <p>
 * Built the same "printed layer by layer" way as the destroyer and
 * submarine, but with a different taper curve: instead of growing (or
 * growing then shrinking) over the whole length, the beam rises sharply
 * from a point at the bow and then plateaus at full width for almost
 * the entire hull -- the flat-sided profile of a real carrier, with the
 * flight deck layer overhanging past the hull below it. On top of that
 * deck it carries an off-center island (bridge, funnel, radar mast),
 * two elevators, catapult stripes near the bow, and small deck-edge gun
 * tubs, all glued to the deck's real surface.
 * </p>
 */
public class AircraftCarrier3D extends Ship3D {

    private static final Color HULL_BASE_COLOR = Color.rgb(80, 83, 87);
    private static final Color HULL_MID_COLOR = Color.rgb(110, 113, 117);
    private static final Color DECK_COLOR = Color.rgb(60, 62, 65);
    private static final Color ISLAND_COLOR = Color.rgb(150, 152, 155);
    private static final Color MAST_COLOR = Color.rgb(170, 172, 175);
    private static final Color DARK_METAL_COLOR = Color.rgb(15, 15, 15);
    private static final Color STRIPE_COLOR = Color.rgb(225, 210, 140);
    private static final Color FLAG_COLOR = Color.rgb(180, 40, 40);

    /** Number of "printed" cross-section slices used to shape each hull layer along X. */
    private static final int HULL_SLICES = 8;

    /** Fraction of the length (from the bow) over which the beam ramps up to full width. */
    private static final double PLATEAU_FRACTION = 0.28;

    public AircraftCarrier3D(int row, int column, Orientation orientation) {
        super(4, row, column, orientation);
    }

    @Override
    protected Group buildHull() {
        // Local variables, not instance fields: Ship3D's constructor calls
        // buildHull() before this subclass's own field initializers would
        // run, so anything derived from shipCenterX() must be computed
        // here.
        double hullLength = 4 * Config3D.CELL_SIZE - 4;
        double bowTipX = shipCenterX() - hullLength / 2.0;
        double sternTipX = shipCenterX() + hullLength / 2.0;

        Group group = new Group();

        buildTaperedLayer(group, -4, 8, 26, 4, HULL_BASE_COLOR, hullLength, bowTipX);
        buildTaperedLayer(group, -12, 8, 36, 5, HULL_MID_COLOR, hullLength, bowTipX);
        buildTaperedLayer(group, -19, 4, 46, 6, DECK_COLOR, hullLength, bowTipX);

        double islandX = buildIsland(group, hullLength, bowTipX);
        buildElevators(group, hullLength, bowTipX, islandX);
        buildCatapultStripes(group, bowTipX);
        buildGunTubs(group, hullLength, bowTipX, sternTipX);
        buildSternFlagstaff(group, sternTipX);

        return group;
    }

    /** Beam ramps from {@code minBeam} up to {@code maxBeam} over the first {@code PLATEAU_FRACTION}
     *  of the length, then stays flat -- a sharp bow point followed by parallel sides. */
    private double beamFactor(double t) {
        if (t >= PLATEAU_FRACTION) {
            return 1.0;
        }
        double u = t / PLATEAU_FRACTION;
        return u * u;
    }

    private double beamAtIndex(int index, double maxBeam, double minBeam) {
        double t = (double) index / (HULL_SLICES - 1);
        return minBeam + (maxBeam - minBeam) * beamFactor(t);
    }

    /** Same beam formula, looked up by X position -- used to glue deck details to the real surface. */
    private double beamAt(double x, double hullLength, double bowTipX, double maxBeam, double minBeam) {
        double sliceLength = hullLength / HULL_SLICES;
        int index = (int) Math.floor((x - bowTipX) / sliceLength);
        index = Math.max(0, Math.min(HULL_SLICES - 1, index));
        return beamAtIndex(index, maxBeam, minBeam);
    }

    private void buildTaperedLayer(Group group, double centerY, double layerHeight,
                                   double maxBeam, double minBeam, Color color,
                                   double hullLength, double bowTipX) {
        double sliceLength = hullLength / HULL_SLICES;
        for (int i = 0; i < HULL_SLICES; i++) {
            double beam = beamAtIndex(i, maxBeam, minBeam);
            double xCenter = bowTipX + sliceLength * (i + 0.5);

            Box slice = new Box(sliceLength + 0.2, layerHeight, beam);
            slice.setMaterial(Config3D.intactHullMaterial(color));
            slice.setTranslateX(xCenter);
            slice.setTranslateY(centerY);
            group.getChildren().add(slice);
        }
    }

    private static double deckTopY() {
        return -19 - 2; // flight deck layer: centerY -19, half-height 2
    }

    /** Off-center island (bridge, funnel, radar mast, flag), set aft of center toward starboard. */
    private double buildIsland(Group group, double hullLength, double bowTipX) {
        double islandX = shipCenterX() + hullLength * 0.12;
        double islandZ = 15; // toward starboard, inset from the deck edge
        double deckTop = deckTopY();

        Box island = new Box(14, 22, 10);
        island.setMaterial(Config3D.intactHullMaterial(ISLAND_COLOR));
        island.setTranslateX(islandX);
        island.setTranslateZ(islandZ);
        island.setTranslateY(deckTop - 11);
        group.getChildren().add(island);

        Box window = new Box(1, 1.5, 8);
        window.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        window.setTranslateX(islandX - 7.5);
        window.setTranslateZ(islandZ);
        window.setTranslateY(deckTop - 11);
        group.getChildren().add(window);

        double islandTopY = deckTop - 22;

        Box funnel = new Box(5, 8, 6);
        funnel.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        funnel.setTranslateX(islandX + 3);
        funnel.setTranslateZ(islandZ);
        funnel.setTranslateY(islandTopY - 4);
        group.getChildren().add(funnel);

        Box mast = new Box(1.5, 12, 1.5);
        mast.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        mast.setTranslateX(islandX - 3);
        mast.setTranslateZ(islandZ);
        mast.setTranslateY(islandTopY - 6);
        group.getChildren().add(mast);

        Box radarDish = new Box(1, 6, 6);
        radarDish.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        radarDish.setTranslateX(islandX - 3.5);
        radarDish.setTranslateZ(islandZ);
        radarDish.setTranslateY(islandTopY - 12);
        group.getChildren().add(radarDish);

        Box flag = new Box(2, 2, 3);
        flag.setMaterial(Config3D.intactHullMaterial(FLAG_COLOR));
        flag.setTranslateX(islandX - 3);
        flag.setTranslateZ(islandZ + 1.5);
        flag.setTranslateY(islandTopY - 12);
        group.getChildren().add(flag);

        return islandX;
    }

    /** Two aircraft elevators: dark flat patches set into the deck on the side opposite the island. */
    private void buildElevators(Group group, double hullLength, double bowTipX, double islandX) {
        double[] elevatorXs = {bowTipX + hullLength * 0.32, islandX - 6};
        for (double x : elevatorXs) {
            Box elevator = new Box(20, 0.6, 16);
            elevator.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
            elevator.setTranslateX(x);
            elevator.setTranslateZ(-16); // port side, opposite the island
            elevator.setTranslateY(deckTopY() - 0.3);
            group.getChildren().add(elevator);
        }
    }

    /** Two pale catapult stripes painted on the deck near the bow. */
    private void buildCatapultStripes(Group group, double bowTipX) {
        for (int side = -1; side <= 1; side += 2) {
            Box stripe = new Box(38, 0.4, 1.5);
            stripe.setMaterial(Config3D.intactHullMaterial(STRIPE_COLOR));
            stripe.setTranslateX(bowTipX + 28);
            stripe.setTranslateZ(side * 6);
            stripe.setTranslateY(deckTopY() - 0.2);
            group.getChildren().add(stripe);
        }
    }

    /** Small deck-edge anti-aircraft gun tubs near the bow and stern corners, glued to the deck's real edge. */
    private void buildGunTubs(Group group, double hullLength, double bowTipX, double sternTipX) {
        for (double x : new double[]{bowTipX + 26, sternTipX - 20}) {
            double halfBeam = beamAt(x, hullLength, bowTipX, 46, 6) / 2.0;
            for (int side = -1; side <= 1; side += 2) {
                Box tub = new Box(4, 3, 4);
                tub.setMaterial(Config3D.intactHullMaterial(HULL_MID_COLOR));
                tub.setTranslateX(x);
                tub.setTranslateZ(side * (halfBeam - 2));
                tub.setTranslateY(deckTopY() - 1.5);
                group.getChildren().add(tub);
            }
        }
    }

    /** Thin flagstaff near the stern, with a small flag. */
    private void buildSternFlagstaff(Group group, double sternTipX) {
        double poleX = sternTipX - 8;

        Box pole = new Box(1, 10, 1);
        pole.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        pole.setTranslateX(poleX);
        pole.setTranslateY(deckTopY() - 5);
        group.getChildren().add(pole);

        Box flag = new Box(2, 2, 3);
        flag.setMaterial(Config3D.intactHullMaterial(FLAG_COLOR));
        flag.setTranslateX(poleX);
        flag.setTranslateZ(1.5);
        flag.setTranslateY(deckTopY() - 9);
        group.getChildren().add(flag);
    }
}