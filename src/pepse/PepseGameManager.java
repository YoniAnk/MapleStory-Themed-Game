package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Avatar;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.daynight.Moon;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Leaf;
import pepse.world.trees.Tree;

import java.awt.*;
import java.awt.image.renderable.RenderableImage;
import java.util.Random;
import java.util.function.Consumer;

public class PepseGameManager extends GameManager {

    /************ Game Settings Constants ***************/
    public static final String WINDOWS_NAME = "Pepse Game";
    private static final int BOARD_HEIGHT = 720;
    private static final int BOARD_WIDTH = 1200;
    public static final int RANDOM_SEED = 1234567;

    /************** avatar properties ***************/
    public static final int AVATAR_LAYER = Layer.DEFAULT;

    /************** day/night properties ***************/
    public static final int SUN_LAYER = Layer.BACKGROUND;
    public static final int MOON_LAYER = Layer.BACKGROUND;
    public static final int SKY_LAYER = Layer.BACKGROUND;
    public static final int NIGHT_LAYER = Layer.FOREGROUND;

    private static final float NIGHT_CYCLE_LEN = 48;
    private static final float SUNSET_CYCLE = NIGHT_CYCLE_LEN * 2;
    private static final Color HALO_COLOR = new Color(255, 255, 0, 20);

    /************** Trees properties ***************/
    public static final int TRUNK_LAYER = Layer.STATIC_OBJECTS + 1;
    public static final int LEAVES_LAYER = 1;

    /************** Terrain properties ***************/
    public static final int TERRAIN_LAYER = Layer.STATIC_OBJECTS;

    /************ Class attributes ***********/
    private float worldCenter;
    private int worldLeftEnd, worldRightEnd;
    private Vector2 windowDimensions;
    private Terrain terrain;
    private Avatar avatar;


    /**
     * The constructor of Pepse Game Manager
     *
     * @param windowTitle      The name of the window
     * @param windowDimensions the windows dimension
     */
    public PepseGameManager(String windowTitle, Vector2 windowDimensions) {
        super(windowTitle, windowDimensions);
    }


    /**
     * Initializes the PepseGameManager
     *
     * @param imageReader      the image reader to use for loading images
     * @param soundReader      the sound reader to use for loading sounds
     * @param inputListener    the user input listener to use for handling user input
     * @param windowController the window controller to use for controlling the game window
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {

        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.windowDimensions = windowController.getWindowDimensions();

        worldCenter = windowDimensions.x() / 2f;
        worldLeftEnd = (int) (-windowDimensions.x());
        worldRightEnd = (int) (windowDimensions.x() * 2f);

        skyCreator();
        terrainCreator(worldLeftEnd, worldRightEnd);
        treesCreator(worldLeftEnd, worldRightEnd);
        gameObjects().layers().shouldLayersCollide(LEAVES_LAYER, TERRAIN_LAYER, true);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TRUNK_LAYER, true);
        createAvatar(inputListener, imageReader);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float curPosition = avatar.getCenter().x();

        if (curPosition > worldRightEnd - windowDimensions.x()) {
            createWorld(Direction.right);
            deleteWorld(Direction.left);
            worldRightEnd += windowDimensions.x();
            worldLeftEnd += windowDimensions.x();
        }

        if (curPosition < worldLeftEnd + windowDimensions.x()) {
            createWorld(Direction.left);
            deleteWorld(Direction.right);
            worldRightEnd -= windowDimensions.x();
            worldLeftEnd -= windowDimensions.x();
        }
    }

    private void createWorld(Direction world) {
        int start, end;

        if (world == Direction.right) {     // create right world
            start = worldRightEnd;
            end = (int) (start + windowDimensions.x());
        } else {                            // create left world
            end = worldLeftEnd;
            start = (int) (end - windowDimensions.x());
        }

        this.terrain.createInRange(start, end);
        this.treesCreator(start, end);
    }

    private void deleteObjectsInLayer(Direction world, int layer) {
        Consumer<GameObject> deleteTerrain = (object) -> {
            if (world == Direction.left) {
                if (object.getTopLeftCorner().x() < worldLeftEnd + windowDimensions.x())
                    gameObjects().removeGameObject(object, layer);
            } else if (object.getTopLeftCorner().x() > worldRightEnd - windowDimensions.x())
                gameObjects().removeGameObject(object, layer);
        };
        gameObjects().objectsInLayer(layer).forEach(deleteTerrain);
    }

    private void deleteWorld(Direction world) {
        deleteObjectsInLayer(world, TERRAIN_LAYER);
        deleteObjectsInLayer(world, TRUNK_LAYER);
        deleteObjectsInLayer(world, LEAVES_LAYER);
    }


    private void createAvatar(UserInputListener inputListener, ImageReader imageReader) {
        avatar = Avatar.create(gameObjects(), Layer.DEFAULT, windowDimensions.mult(0.5f), inputListener, imageReader);
        Vector2 distance = windowDimensions.mult(0.5f).subtract(avatar.getTopLeftCorner());
        setCamera(new Camera(avatar, distance, windowDimensions, windowDimensions));
    }

    /**
     * Creates a new terrain and adds it to the list of game objects.
     */
    private void terrainCreator(int start, int end) {
        this.terrain = new Terrain(this.gameObjects(), TERRAIN_LAYER, windowDimensions, RANDOM_SEED);
        terrain.createInRange(start, end);
    }

    /**
     * Creates a new sky and adds it to the list of game objects.
     */
    private void skyCreator() {
        GameObject sky = Sky.create(gameObjects(), windowDimensions, SKY_LAYER);
        GameObject night = Night.create(gameObjects(), NIGHT_LAYER, windowDimensions, NIGHT_CYCLE_LEN);
        GameObject sun = Sun.create(gameObjects(), SUN_LAYER, windowDimensions, SUNSET_CYCLE);
        GameObject sunHalo = SunHalo.create(gameObjects(), SUN_LAYER, sun, HALO_COLOR);
        GameObject moon = Moon.create(gameObjects(), MOON_LAYER, windowDimensions, SUNSET_CYCLE);
    }

    private void treesCreator(int start, int end) {
        for (int curX = start; curX <= end; curX += 2 * Block.SIZE) {
            if (Tree.shouldPlantTree(RANDOM_SEED, curX)) {
                float curY = (float) Math.floor(terrain.groundHeightAt(curX) / Block.SIZE) * Block.SIZE;
                Vector2 position = new Vector2(curX, curY - Block.SIZE);
                Tree.Create(gameObjects(), position, TRUNK_LAYER, LEAVES_LAYER, RANDOM_SEED);
            }
        }
    }

    public static void main(String[] args) {
        new PepseGameManager(WINDOWS_NAME, new Vector2(BOARD_WIDTH, BOARD_HEIGHT)).run();
    }

    private enum Direction {right, left}
}