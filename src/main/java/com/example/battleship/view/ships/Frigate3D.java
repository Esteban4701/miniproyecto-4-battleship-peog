package com.example.battleship.view.ships;

import com.example.battleship.view.Config3D;
import com.example.battleship.view.Orientation;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;

/**
 * Frigate: the smallest ship in the fleet, occupies 1 single cell.
 * <p>
 * Built the same way a Minecraft ship would be built out of blocks and
 * slabs: several stacked boxes form the hull, narrow at the keel and
 * flaring outward toward the deck (not tapering to a point). On top of
 * that base hull it carries the small details that make a warship read
 * as a warship: a bow deck gun, a bridge set back toward the stern, a
 * funnel, a mast with a radar panel, a row of portholes along each
 * side, and a stern flagstaff.
 * </p>
 */
public class Frigate3D extends Ship3D {

    private static final Color HULL_BASE_COLOR = Color.rgb(110, 108, 105);
    private static final Color HULL_MID_COLOR = Color.rgb(150, 148, 145);
    private static final Color DECK_COLOR = Color.rgb(214, 210, 205);
    private static final Color BRIDGE_COLOR = Color.rgb(225, 222, 217);
    private static final Color MAST_COLOR = Color.rgb(190, 187, 182);
    private static final Color DARK_METAL_COLOR = Color.rgb(15, 15, 15);
    private static final Color TURRET_COLOR = Color.rgb(95, 93, 90);
    private static final Color FUNNEL_COLOR = Color.rgb(60, 58, 55);
    private static final Color FLAG_COLOR = Color.rgb(180, 40, 40);

    /** Deck top sits at this Y; every on-deck detail is built up from here. */
    private static final double DECK_TOP_Y = -17;

    /** Bow faces -Z; the whole hull is centered on X = 0. */
    public Frigate3D(int row, int column, Orientation orientation) {
        super(1, row, column, orientation);
    }

    @Override
    protected Group buildHull() {
        Group group = new Group();

        buildHullLayers(group);
        buildBowGun(group);
        double bridgeZ = buildBridge(group);
        buildMast(group, bridgeZ);
        buildFunnel(group, bridgeZ);
        buildPortholes(group);
        buildFlagstaff(group);
        buildBowPlank(group);

        return group;
    }

    /** Hull: 3 stacked layers, each WIDER than the one below -- narrow keel flaring
     *  outward toward the deck (a parabola that opens upward, not a pyramid). */
    private void buildHullLayers(Group group) {
        Box keel = new Box(20, 6, 18);
        keel.setMaterial(Config3D.intactHullMaterial(HULL_BASE_COLOR));
        keel.setTranslateY(-3);

        Box lowerHull = new Box(26, 6, 22);
        lowerHull.setMaterial(Config3D.intactHullMaterial(HULL_MID_COLOR));
        lowerHull.setTranslateY(-9);

        Box deck = new Box(32, 5, 26);
        deck.setMaterial(Config3D.intactHullMaterial(DECK_COLOR));
        deck.setTranslateY(-14.5);

        group.getChildren().addAll(keel, lowerHull, deck);
    }

    /** Small deck gun right at the bow, with a thin barrel pointing forward. */
    private void buildBowGun(Group group) {
        double turretZ = -11;

        Box turret = new Box(6, 4, 5);
        turret.setMaterial(Config3D.intactHullMaterial(TURRET_COLOR));
        turret.setTranslateZ(turretZ);
        turret.setTranslateY(DECK_TOP_Y - 2);
        group.getChildren().add(turret);

        Box barrel = new Box(1.5, 1.5, 8);
        barrel.setMaterial(Config3D.intactHullMaterial(TURRET_COLOR.darker()));
        barrel.setTranslateZ(turretZ - 6.5);
        barrel.setTranslateY(DECK_TOP_Y - 2);
        group.getChildren().add(barrel);
    }

    /** Bridge with a single window band. Set back toward the stern half of the deck. */
    private double buildBridge(Group group) {
        double bridgeZ = 3;

        Box bridge = new Box(16, 9, 10);
        bridge.setMaterial(Config3D.intactHullMaterial(BRIDGE_COLOR));
        bridge.setTranslateZ(bridgeZ);
        bridge.setTranslateY(-21.5);
        group.getChildren().add(bridge);

        Box window = new Box(10, 1.5, 1);
        window.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        window.setTranslateY(-21.5);
        window.setTranslateZ(bridgeZ - 5.5);
        group.getChildren().add(window);

        return bridgeZ;
    }

    /** Mast with a yard (the cross shape) and a small radar panel, rising from the bridge. */
    private void buildMast(Group group, double bridgeZ) {
        Box mast = new Box(2, 18, 2);
        mast.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        mast.setTranslateZ(bridgeZ);
        mast.setTranslateY(-35);
        group.getChildren().add(mast);

        Box yard = new Box(14, 2, 2);
        yard.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        yard.setTranslateZ(bridgeZ);
        yard.setTranslateY(-40);
        group.getChildren().add(yard);

        Box radar = new Box(6, 3, 1);
        radar.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        radar.setTranslateZ(bridgeZ + 1.5);
        radar.setTranslateY(-37);
        group.getChildren().add(radar);
    }

    /** Funnel (smokestack) just aft of the bridge, with a darker rim on top. */
    private void buildFunnel(Group group, double bridgeZ) {
        double funnelZ = bridgeZ + 9;

        Box stack = new Box(5, 12, 5);
        stack.setMaterial(Config3D.intactHullMaterial(FUNNEL_COLOR));
        stack.setTranslateZ(funnelZ);
        stack.setTranslateY(DECK_TOP_Y - 6);
        group.getChildren().add(stack);

        Box rim = new Box(6, 1.5, 6);
        rim.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
        rim.setTranslateZ(funnelZ);
        rim.setTranslateY(DECK_TOP_Y - 12.75);
        group.getChildren().add(rim);
    }

    /** A row of small dark portholes along each side of the lower hull. */
    private void buildPortholes(Group group) {
        for (double portholeX : new double[]{-13, 13}) {
            for (double portholeZ : new double[]{-6, 0, 6}) {
                Box porthole = new Box(1, 2, 2);
                porthole.setMaterial(Config3D.intactHullMaterial(DARK_METAL_COLOR));
                porthole.setTranslateX(portholeX);
                porthole.setTranslateY(-9);
                porthole.setTranslateZ(portholeZ);
                group.getChildren().add(porthole);
            }
        }
    }

    /** Thin flagstaff at the stern, with a small flag. */
    private void buildFlagstaff(Group group) {
        double sternZ = 12;

        Box pole = new Box(1, 10, 1);
        pole.setMaterial(Config3D.intactHullMaterial(MAST_COLOR));
        pole.setTranslateZ(sternZ);
        pole.setTranslateY(DECK_TOP_Y - 5);
        group.getChildren().add(pole);

        Box flag = new Box(3, 2, 0.5);
        flag.setMaterial(Config3D.intactHullMaterial(FLAG_COLOR));
        flag.setTranslateX(1.5);
        flag.setTranslateZ(sternZ);
        flag.setTranslateY(DECK_TOP_Y - 9);
        group.getChildren().add(flag);
    }

    /** Thin overhang jutting out past the hull's front edge, just above the waterline. */
    private void buildBowPlank(Group group) {
        Box bowPlank = new Box(8, 2, 10);
        bowPlank.setMaterial(Config3D.intactHullMaterial(DECK_COLOR));
        bowPlank.setTranslateZ(-20);
        bowPlank.setTranslateY(-14.5);
        group.getChildren().add(bowPlank);
    }
}