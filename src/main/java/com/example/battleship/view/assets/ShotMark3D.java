package com.example.battleship.view.assets;

import com.example.battleship.view.Config3D;

import com.example.battleship.view.ships.Ship3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Builds the 3D marks placed on a cell after a shot. Hits on a ship are
 * painted directly on the hull by {@link Ship3D#markHit(int)}; this
 * class is for marks that sit on top of the water sheet (the "water
 * miss" case always, and it can also be reused to highlight a hit
 * cell).
 */
public final class ShotMark3D {

    private ShotMark3D() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Miss shot on the water: a red "X" made of two crossed cylinders.
     *
     * @param row    board row of the missed cell
     * @param column board column of the missed cell
     * @return a group containing the mark, already positioned over that cell
     */
    public static Group waterMiss(int row, int column) {
        Group group = new Group();
        PhongMaterial material = new PhongMaterial(Color.rgb(200, 25, 25));
        material.setSpecularColor(Color.rgb(255, 120, 120));

        Cylinder bar1 = new Cylinder(1.5, 22);
        bar1.setMaterial(material);
        bar1.setRotationAxis(Rotate.Z_AXIS);
        bar1.setRotate(45);

        Cylinder bar2 = new Cylinder(1.5, 22);
        bar2.setMaterial(material);
        bar2.setRotationAxis(Rotate.Z_AXIS);
        bar2.setRotate(-45);

        group.getChildren().addAll(bar1, bar2);
        group.setTranslateX(column * Config3D.CELL_SIZE);
        group.setTranslateZ(row * Config3D.CELL_SIZE);
        group.setTranslateY(-8);
        return group;
    }

    /**
     * Extra hit marker (small flame) shown above a cell where a ship was hit.
     *
     * @param row    board row of the hit cell
     * @param column board column of the hit cell
     * @return a group containing the mark, already positioned over that cell
     */
    public static Group hit(int row, int column) {
        Group group = new Group();
        Sphere flame = new Sphere(5);
        PhongMaterial material = new PhongMaterial(Color.ORANGERED);
        material.setSpecularColor(Color.YELLOW);
        flame.setMaterial(material);
        group.getChildren().add(flame);
        group.setTranslateX(column * Config3D.CELL_SIZE);
        group.setTranslateZ(row * Config3D.CELL_SIZE);
        group.setTranslateY(-24);
        return group;
    }
}