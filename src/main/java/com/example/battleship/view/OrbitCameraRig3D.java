package com.example.battleship.view;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * A free-roaming "dome" camera: the user can drag to orbit around a
 * fixed target point at a constant radius -- geometrically, a point
 * moving over the surface of a hemisphere centered on the target (the
 * concept-sketch idea: pick a fixed radius from the target, and every
 * point at that radius traces out the dome the camera is allowed to
 * move on). This is a third-person "orbit" camera, like the one in
 * Mario 64 -- you never see from the target's point of view, you see
 * the target from a point gliding around it.
 * <p>
 * The target itself can be animated from one point to another with
 * {@link #panTo}, which is what lets the camera glide from hovering
 * over one board to hovering over another (e.g. switching whose turn
 * it is) instead of jumping instantly.
 * </p>
 * <p>
 * Implementation: three nested groups, each responsible for exactly one
 * degree of freedom --
 * </p>
 * <pre>
 *   targetPivot   (translated to the point being orbited)
 *     azimuthPivot  (rotates around Y -- spinning around the dome)
 *       elevationPivot (rotates around X -- tilting up/down the dome)
 *         camera (pushed back by a fixed distance = the dome's radius)
 * </pre>
 * <p>
 * IMPORTANT: the azimuth/elevation rotations are applied as explicit
 * {@link Rotate} objects added to each group's {@code transforms} list,
 * NOT via the node's own {@code rotate}/{@code rotationAxis}
 * properties. A {@code Rotate} transform's pivot defaults to the
 * origin (0,0,0); a node's own {@code rotate} property instead pivots
 * around the center of that node's own computed bounds -- which, for a
 * group whose only child is a camera already pushed back by the
 * radius, is effectively the camera's own position. Using the node
 * property there rotates the camera in place around itself (a
 * first-person "look around" feel) instead of moving it around the
 * target (the third-person "orbit" feel this class is supposed to
 * give) -- that mix-up is exactly what caused the camera to look like it
 * was stuck in place instead of orbiting.
 * </p>
 */
public class OrbitCameraRig3D {

    /** Degrees. Keeps the camera from dropping to (or past) the horizon. */
    private static final double MIN_ELEVATION = 12;
    /** Degrees. Keeps the camera from going past straight-overhead (which flips left/right on drag). */
    private static final double MAX_ELEVATION = 82;

    /** How many degrees the camera turns per pixel of mouse drag. */
    private static final double DRAG_SENSITIVITY = 0.35;

    private final double radius;
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private final Group targetPivot = new Group();
    private final Group azimuthPivot = new Group();
    private final Group elevationPivot = new Group();

    /** Pivot is (0,0,0) by default -- exactly what makes this an orbit, not a look-around. */
    private final Rotate azimuthRotate = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate elevationRotate = new Rotate(0, Rotate.X_AXIS);

    private double lastDragSceneX;
    private double lastDragSceneY;

    /** The public, intuitive elevation value (0 = level with the target, 90 = straight overhead). */
    private double currentElevation;

    /**
     * @param radius         fixed distance from the target to the camera (the dome's radius)
     * @param initialTarget  point the camera starts orbiting (usually a board's center)
     * @param initialAzimuth starting horizontal angle, in degrees
     * @param initialElevation starting vertical angle, in degrees (clamped to the dome's valid range)
     */
    public OrbitCameraRig3D(double radius, Point3D initialTarget, double initialAzimuth, double initialElevation) {
        this.radius = radius;

        camera.setNearClip(0.1);
        camera.setFarClip(4000);
        camera.setFieldOfView(45);
        camera.setTranslateZ(-radius);

        elevationPivot.getTransforms().add(elevationRotate);
        elevationPivot.getChildren().add(camera);

        azimuthPivot.getTransforms().add(azimuthRotate);
        azimuthPivot.getChildren().add(elevationPivot);

        targetPivot.getChildren().add(azimuthPivot);
        moveTargetInstantly(initialTarget);
        azimuthRotate.setAngle(initialAzimuth);
        setElevation(initialElevation);
    }

    /** The node to add to the scene graph (holds the whole rig, positioned at the target). */
    public Group getRootNode() {
        return targetPivot;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    /**
     * Wires up mouse-drag orbiting on the given node (normally the
     * {@code SubScene} or a full-size transparent pane on top of it).
     * Dragging left/right spins around the dome (azimuth); dragging
     * up/down tilts along it (elevation), clamped so the camera can't
     * dip below the horizon or flip over the top.
     */
    public void attachDragControls(Node dragSurface) {
        dragSurface.setOnMousePressed(this::onDragStart);
        dragSurface.setOnMouseDragged(this::onDrag);
    }

    private void onDragStart(MouseEvent event) {
        lastDragSceneX = event.getSceneX();
        lastDragSceneY = event.getSceneY();
    }

    private void onDrag(MouseEvent event) {
        double deltaX = event.getSceneX() - lastDragSceneX;
        double deltaY = event.getSceneY() - lastDragSceneY;
        lastDragSceneX = event.getSceneX();
        lastDragSceneY = event.getSceneY();

        azimuthRotate.setAngle(azimuthRotate.getAngle() - deltaX * DRAG_SENSITIVITY);
        setElevation(currentElevation + deltaY * DRAG_SENSITIVITY);
    }

    private double clampElevation(double degrees) {
        return Math.max(MIN_ELEVATION, Math.min(MAX_ELEVATION, degrees));
    }

    /**
     * Sets the public, intuitive elevation (0 = level, 90 = overhead) and
     * applies it to the rotate transform with the sign it actually
     * needs: with this rig's order (rotate around X, then push the
     * camera back along Z), a POSITIVE angle rotates the camera DOWN
     * below the target instead of up above it, so the transform always
     * gets {@code -degrees}, never the raw value.
     */
    private void setElevation(double degrees) {
        currentElevation = clampElevation(degrees);
        elevationRotate.setAngle(-currentElevation);
    }

    private void moveTargetInstantly(Point3D target) {
        targetPivot.setTranslateX(target.getX());
        targetPivot.setTranslateY(target.getY());
        targetPivot.setTranslateZ(target.getZ());
    }

    /** Glides the orbited point to {@code newTarget} over {@code durationMillis}, keeping the current viewing angle. */
    public void panTo(Point3D newTarget, double durationMillis) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(durationMillis),
                new KeyValue(targetPivot.translateXProperty(), newTarget.getX(), Interpolator.EASE_BOTH),
                new KeyValue(targetPivot.translateYProperty(), newTarget.getY(), Interpolator.EASE_BOTH),
                new KeyValue(targetPivot.translateZProperty(), newTarget.getZ(), Interpolator.EASE_BOTH)));
        timeline.play();
    }

    /** Same as {@link #panTo(Point3D, double)}, and also eases the azimuth to {@code azimuthDeg} -- use this to
     *  land facing a consistent default angle on arrival, instead of keeping whatever angle the user left it at. */
    public void panTo(Point3D newTarget, double azimuthDeg, double durationMillis) {
        double currentAzimuth = azimuthRotate.getAngle();
        // Take the shortest path to the target angle instead of
        // interpolating the raw values, so a pan from 350 deg to 35 deg
        // sweeps 45 deg forward instead of spinning 315 deg backward.
        double shortestDelta = ((azimuthDeg - currentAzimuth + 540) % 360) - 180;
        double targetAzimuth = currentAzimuth + shortestDelta;

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(durationMillis),
                new KeyValue(targetPivot.translateXProperty(), newTarget.getX(), Interpolator.EASE_BOTH),
                new KeyValue(targetPivot.translateYProperty(), newTarget.getY(), Interpolator.EASE_BOTH),
                new KeyValue(targetPivot.translateZProperty(), newTarget.getZ(), Interpolator.EASE_BOTH),
                new KeyValue(azimuthRotate.angleProperty(), targetAzimuth, Interpolator.EASE_BOTH)));
        timeline.play();
    }

    public double getRadius() {
        return radius;
    }
}
