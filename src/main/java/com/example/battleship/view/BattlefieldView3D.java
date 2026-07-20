package com.example.battleship.view;

import com.example.battleship.view.assets.BoardLabels3D;
import com.example.battleship.view.assets.HoverMarker3D;
import com.example.battleship.view.assets.Water3D;
import com.example.battleship.view.ships.Ship3D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

/**
 * Full 3D battlefield: both boards (the player's and the machine's)
 * exist in the same scene at all times, placed apart from each other,
 * floating over one continuous "sea" slab, inside a box "sky" made of
 * ordinary walls (not a SubScene fill gradient -- see
 * {@link #createGradientTexture} for why). Only one board is actually
 * in view at a time, because the {@link OrbitCameraRig3D} is always
 * hovering over just one of them -- calling {@link #setTurn} glides
 * the camera from one board's dome over to the other's.
 */
public class BattlefieldView3D extends SubScene {

    /** Board size, reused from {@link BoardView3D} so both boards stay in sync with it. */
    private static final int BOARD_SIZE = BoardView3D.ROWS;

    /** Gap left empty between the two boards, along Z. */
    private static final double BOARD_GAP = 140;

    // 800 units keeps the sea/sky walls comfortably beyond the camera's
    // reach: at CAMERA_RADIUS=550 and the shallowest allowed elevation
    // (12 deg), the camera's horizontal distance from its target is
    // ~550*cos(12deg) =~ 538 -- so anything much smaller than 800 here
    // risks the camera ending up outside the walls.
    private static final double SEA_MARGIN = 800;
    private static final double SEA_THICKNESS = 20;
    private static final Color SEA_COLOR = Color.rgb(12, 35, 60);

    private static final double SKY_WALL_HEIGHT = 1400;
    private static final Color SKY_TOP = Color.rgb(18, 30, 55);
    private static final Color SKY_HORIZON = Color.rgb(70, 108, 148);

    private static final double CAMERA_RADIUS = 550;
    private static final double DEFAULT_ELEVATION = 42;
    // Negated from the original +35: azimuth spins the camera around the
    // vertical axis, so flipping its sign mirrors the default view
    // left-right -- moving the numbers to the left edge and the letters
    // to read left-to-right, without touching the label placement logic
    // itself (which already puts numbers on the west edge and letters on
    // the north edge, the standard layout).
    private static final double PLAYER_AZIMUTH = -35;
    private static final double MACHINE_AZIMUTH = -35;

    /** Whose board the camera should be looking at. */
    public enum Turn {
        PLAYER,
        MACHINE
    }

    private final Group playerBoardGroup = new Group();
    private final Group machineBoardGroup = new Group();
    private final OrbitCameraRig3D cameraRig;

    private final HoverMarker3D playerHoverMarker = new HoverMarker3D();

    private final Point3D playerBoardCenter;
    private final Point3D machineBoardCenter;

    private Turn currentTurn = Turn.PLAYER;

    public BattlefieldView3D(double width, double height) {
        super(new Group(), width, height, true, SceneAntialiasing.BALANCED);
        Group root3D = (Group) getRoot();

        double boardSpan = (BOARD_SIZE - 1) * Config3D.CELL_SIZE;
        double boardCenterOffset = boardSpan / 2.0;

        // Player board sits at the scene's local origin; the machine
        // board is offset further along Z, with a gap in between.
        machineBoardGroup.setTranslateZ(boardSpan + BOARD_GAP);

        buildWaterGrid(playerBoardGroup);
        buildWaterGrid(machineBoardGroup);
        playerBoardGroup.getChildren().add(playerHoverMarker);
        playerBoardGroup.getChildren().add(BoardLabels3D.build(BoardLabels3D.Layout.player(), BoardLabels3D.Board.PLAYER));
        machineBoardGroup.getChildren().add(BoardLabels3D.build(BoardLabels3D.Layout.standard(), BoardLabels3D.Board.MACHINE));
        root3D.getChildren().addAll(playerBoardGroup, machineBoardGroup);

        double totalFieldDepth = boardSpan * 2 + BOARD_GAP;
        buildSeaFloor(root3D, boardSpan, totalFieldDepth);
        buildSkyWalls(root3D, boardSpan, totalFieldDepth);

        this.playerBoardCenter = new Point3D(boardCenterOffset, -10, boardCenterOffset);
        this.machineBoardCenter = new Point3D(boardCenterOffset, -10,
                machineBoardGroup.getTranslateZ() + boardCenterOffset);

        setupLights(root3D);

        cameraRig = new OrbitCameraRig3D(CAMERA_RADIUS, playerBoardCenter, PLAYER_AZIMUTH, DEFAULT_ELEVATION);
        root3D.getChildren().add(cameraRig.getRootNode());
        setCamera(cameraRig.getCamera());

        // Plain solid color, NOT a gradient: SubScene.setFill() with a
        // LinearGradient is what caused the ghosting/duplicate bug when
        // the camera moved. This solid fallback only shows through any
        // tiny gap beyond the sky walls; the walls carry the real
        // gradient look, baked into a texture instead.
        setFill(SKY_TOP);

        setOnMouseMoved(this::onMouseMoved);
    }

    private void buildWaterGrid(Group boardGroup) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                boardGroup.getChildren().add(new Water3D(row, column));
            }
        }
    }

    /** One big flat slab under both boards, standing in for the open sea around them. */
    private void buildSeaFloor(Group root3D, double boardSpan, double totalFieldDepth) {
        PhongMaterial seaMaterial = new PhongMaterial(SEA_COLOR);
        seaMaterial.setSpecularColor(Color.rgb(45, 75, 105));

        double width = boardSpan + SEA_MARGIN * 2;
        double depth = totalFieldDepth + SEA_MARGIN * 2;

        Box sea = new Box(width, SEA_THICKNESS, depth);
        sea.setMaterial(seaMaterial);
        sea.setTranslateX(boardSpan / 2.0);
        sea.setTranslateZ(totalFieldDepth / 2.0);
        // Top face lands at y = +3, just below the water tiles (y = +2) --
        // a deliberate small gap, not flush, to avoid z-fighting.
        sea.setTranslateY(SEA_THICKNESS / 2.0 + 3);
        root3D.getChildren().add(sea);
    }

    /**
     * Four tall wall panels around the sea, standing in for a sky. Uses a
     * gradient baked into a texture image (via {@link #createGradientTexture}),
     * applied as a normal {@code diffuseMap} -- the same well-tested code
     * path as any other textured 3D shape. Deliberately does NOT use a
     * {@code LinearGradient} as a live {@code Paint} anywhere in the 3D
     * scene, since that's what caused the ghosting bug on {@code SubScene.setFill()}.
     * <p>
     * These are true flat planes ({@link #createWallQuad}), not {@code Box}
     * shapes: a thin box has a front face and a back face only a few
     * units apart, and viewed edge-on those two nearly-coincident
     * surfaces z-fight, showing as flickering horizontal stripes. A
     * single-sided plane has no second surface to fight with.
     * </p>
     */
    private void buildSkyWalls(Group root3D, double boardSpan, double totalFieldDepth) {
        double seaWidth = boardSpan + SEA_MARGIN * 2;
        double seaDepth = totalFieldDepth + SEA_MARGIN * 2;

        double centerX = boardSpan / 2.0;
        double centerZ = totalFieldDepth / 2.0;
        double minX = centerX - seaWidth / 2.0;
        double maxX = centerX + seaWidth / 2.0;
        double minZ = centerZ - seaDepth / 2.0;
        double maxZ = centerZ + seaDepth / 2.0;

        double wallBottomY = 20; // just below the sea's top surface, to hide any seam
        double wallCenterY = wallBottomY - SKY_WALL_HEIGHT / 2.0;

        Image skyTexture = createGradientTexture(SKY_TOP, SKY_HORIZON, 4, 512);
        PhongMaterial skyMaterial = new PhongMaterial();
        // Self-illumination instead of a normal diffuse map: the sky
        // shouldn't dim based on distance from the boards' point lights
        // (that's what made the far walls look "missing" -- they were
        // just badly lit, not actually absent). This keeps every wall
        // at the same constant brightness no matter where the camera is.
        skyMaterial.setDiffuseColor(Color.BLACK);
        skyMaterial.setSelfIlluminationMap(skyTexture);

        MeshView northWall = createWallQuad(seaWidth, SKY_WALL_HEIGHT);
        northWall.setTranslateX(centerX);
        northWall.setTranslateZ(minZ);

        MeshView southWall = createWallQuad(seaWidth, SKY_WALL_HEIGHT);
        southWall.setTranslateX(centerX);
        southWall.setTranslateZ(maxZ);

        // East/west walls need their local X axis (the quad's own
        // "width" direction) turned to point along world Z instead.
        MeshView westWall = createWallQuad(seaDepth, SKY_WALL_HEIGHT);
        westWall.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        westWall.setTranslateX(minX);
        westWall.setTranslateZ(centerZ);

        MeshView eastWall = createWallQuad(seaDepth, SKY_WALL_HEIGHT);
        eastWall.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        eastWall.setTranslateX(maxX);
        eastWall.setTranslateZ(centerZ);

        for (MeshView wall : new MeshView[]{northWall, southWall, westWall, eastWall}) {
            wall.setMaterial(skyMaterial);
            wall.setTranslateY(wallCenterY);
            // The camera sits INSIDE this enclosure, looking at the
            // inward face; a plane's default cull face would hide
            // exactly that face if the camera is on its "back" side,
            // so both sides need to render.
            wall.setCullFace(CullFace.NONE);
            root3D.getChildren().add(wall);
        }
    }

    /** A single flat rectangle (2 triangles, no thickness), lying in its local XY plane. */
    private MeshView createWallQuad(double width, double height) {
        float halfWidth = (float) (width / 2.0);
        float halfHeight = (float) (height / 2.0);

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(new float[]{
                -halfWidth, -halfHeight, 0, // 0: top-left
                halfWidth, -halfHeight, 0,  // 1: top-right
                halfWidth, halfHeight, 0,   // 2: bottom-right
                -halfWidth, halfHeight, 0,  // 3: bottom-left
        });
        mesh.getTexCoords().setAll(new float[]{
                0, 0, // 0: top-left -> top of the gradient image
                1, 0, // 1: top-right
                1, 1, // 2: bottom-right -> bottom of the gradient image
                0, 1, // 3: bottom-left
        });
        mesh.getFaces().setAll(new int[]{
                0, 0, 1, 1, 2, 2,
                0, 0, 2, 2, 3, 3,
        });

        return new MeshView(mesh);
    }

    /**
     * Bakes a vertical two-color gradient into a small bitmap once, up
     * front -- a texture, not a live gradient Paint. This is the safe
     * way to get a gradient look in this scene: apply it as a
     * {@code diffuseMap} on ordinary 3D geometry, the same code path
     * already confirmed stable for the sea floor and every ship, rather
     * than feeding a {@code LinearGradient} into a {@code Paint}
     * property that gets re-evaluated during rendering (which is what
     * broke {@code SubScene.setFill()} when the camera moved).
     */
    private Image createGradientTexture(Color top, Color bottom, int width, int height) {
        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, top), new Stop(1, bottom)));

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return rectangle.snapshot(params, null);
    }

    private void setupLights(Group root3D) {
        AmbientLight ambientLight = new AmbientLight(Color.rgb(150, 150, 160));
        root3D.getChildren().add(ambientLight);

        // One point light roughly above each board, so both stay lit
        // even though only one is ever in frame.
        for (Point3D center : new Point3D[]{playerBoardCenter, machineBoardCenter}) {
            PointLight light = new PointLight(Color.WHITE);
            light.setTranslateX(center.getX());
            light.setTranslateZ(center.getZ());
            light.setTranslateY(-400);
            root3D.getChildren().add(light);
        }
    }

    /** Shows a white ring on whichever player-board water cell the mouse is currently over, hides it otherwise. */
    private void onMouseMoved(MouseEvent event) {
        PickResult pick = event.getPickResult();
        Node picked = pick.getIntersectedNode();

        if (picked instanceof Water3D water && water.getParent() == playerBoardGroup) {
            playerHoverMarker.moveToCell(water.getRow(), water.getColumn());
        } else {
            playerHoverMarker.hide();
        }
    }

    /** Adds a ship to the player's board (in the player board's own local 0..360 coordinates). */
    public void addShipToPlayerBoard(Ship3D ship) {
        playerBoardGroup.getChildren().add(ship);
    }

    /** Adds a ship to the machine's board (in the machine board's own local 0..360 coordinates). */
    public void addShipToMachineBoard(Ship3D ship) {
        machineBoardGroup.getChildren().add(ship);
    }

    /** Adds any other node (e.g. a shot mark) to the player's board. */
    public void addToPlayerBoard(Node node) {
        playerBoardGroup.getChildren().add(node);
    }

    /** Adds any other node (e.g. a shot mark) to the machine's board. */
    public void addToMachineBoard(Node node) {
        machineBoardGroup.getChildren().add(node);
    }

    /** Glides the camera from whichever dome it's currently over to the other board's dome. */
    public void setTurn(Turn turn) {
        if (turn == currentTurn) {
            return;
        }
        currentTurn = turn;
        if (turn == Turn.PLAYER) {
            cameraRig.panTo(playerBoardCenter, PLAYER_AZIMUTH, 1400);
        } else {
            cameraRig.panTo(machineBoardCenter, MACHINE_AZIMUTH, 1400);
        }
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }

    public OrbitCameraRig3D getCameraRig() {
        return cameraRig;
    }
}