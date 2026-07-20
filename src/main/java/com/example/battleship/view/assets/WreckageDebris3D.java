package com.example.battleship.view.assets;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * A scattered cluster of broken hull fragments with fire and smoke,
 * shown on a cell where a ship has been hit -- replaces the old plain
 * scorch mark with something that reads as an actual wreck.
 * <p>
 * Everything here is hand-placed (fixed offsets), not randomized: a
 * designed scatter pattern looks intentional every time, instead of
 * occasionally clumping together the way a naive random placement
 * could.
 * </p>
 */
public final class WreckageDebris3D {

    private static final Color FRAGMENT_COLOR = Color.rgb(210, 208, 202);
    private static final Color FRAGMENT_DARK_COLOR = Color.rgb(90, 88, 84);
    private static final Color FLAME_BASE_COLOR = Color.rgb(230, 90, 20);
    private static final Color FLAME_TIP_COLOR = Color.rgb(250, 200, 60);
    private static final Color SMOKE_COLOR = Color.rgb(40, 40, 40);

    /** One fragment: position, size, and how it's tipped over. */
    private record FragmentSpec(double x, double z, double size, double rotationY, double tilt, boolean dark) {
    }

    private static final FragmentSpec[] FRAGMENTS = {
            new FragmentSpec(-11, -7, 7, 20, 12, false),
            new FragmentSpec(9, -9, 5, -35, 18, true),
            new FragmentSpec(-8, 8, 6, 60, 8, false),
            new FragmentSpec(10, 7, 4, -10, 22, false),
            new FragmentSpec(-1, -13, 5, 45, 15, true),
            new FragmentSpec(2, 12, 6, -50, 10, false),
    };

    private WreckageDebris3D() {
        // Utility class: not meant to be instantiated.
    }

    /** Builds one wreckage cluster, centered on the local origin (the caller positions it over a cell). */
    public static Group create() {
        Group group = new Group();

        for (FragmentSpec spec : FRAGMENTS) {
            group.getChildren().add(buildFragment(spec));
        }
        group.getChildren().add(buildFlame(-3, -2));
        group.getChildren().add(buildFlame(4, 3));
        group.getChildren().add(buildSmoke(0, 0));

        return group;
    }

    private static Box buildFragment(FragmentSpec spec) {
        Box fragment = new Box(spec.size(), spec.size() * 0.35, spec.size() * 0.8);
        PhongMaterial material = new PhongMaterial(spec.dark() ? FRAGMENT_DARK_COLOR : FRAGMENT_COLOR);
        material.setSpecularColor(Color.rgb(230, 230, 230));
        fragment.setMaterial(material);

        fragment.setTranslateX(spec.x());
        fragment.setTranslateZ(spec.z());
        fragment.setTranslateY(-1.5);

        // Two rotations: spun flat (Y) so fragments don't all point the
        // same way, and tipped up on one edge (Z) so they read as
        // broken debris rather than neat flat tiles.
        fragment.getTransforms().addAll(
                new Rotate(spec.rotationY(), Rotate.Y_AXIS),
                new Rotate(spec.tilt(), Rotate.X_AXIS));

        return fragment;
    }

    /** A small two-tone flame: a wider orange base sphere with a smaller yellow tip riding on top. */
    private static Group buildFlame(double x, double z) {
        Group flame = new Group();

        Sphere base = new Sphere(4.5);
        PhongMaterial baseMaterial = new PhongMaterial(FLAME_BASE_COLOR);
        baseMaterial.setSpecularColor(Color.YELLOW);
        base.setMaterial(baseMaterial);
        base.setTranslateY(-4);

        Sphere tip = new Sphere(2.5);
        PhongMaterial tipMaterial = new PhongMaterial(FLAME_TIP_COLOR);
        tipMaterial.setSpecularColor(Color.WHITE);
        tip.setMaterial(tipMaterial);
        tip.setTranslateY(-8.5);

        flame.getChildren().addAll(base, tip);
        flame.setTranslateX(x);
        flame.setTranslateZ(z);
        return flame;
    }

    /** A thin dark sliver angled upward, standing in for a wisp of smoke. */
    private static Box buildSmoke(double x, double z) {
        Box smoke = new Box(2, 14, 2);
        PhongMaterial material = new PhongMaterial(SMOKE_COLOR);
        smoke.setMaterial(material);
        smoke.setOpacity(0.55);

        smoke.setTranslateX(x);
        smoke.setTranslateZ(z);
        smoke.setTranslateY(-14);
        smoke.setRotationAxis(Rotate.Z_AXIS);
        smoke.setRotate(12);

        return smoke;
    }
}
