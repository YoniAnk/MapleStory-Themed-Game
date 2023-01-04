package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.*;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.util.NumericEnergyCounter;
import pepse.world.Avatar;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Tree;

import java.awt.*;
import java.util.function.Consumer;

public class PepseGameManager extends GameManager {

    /************ Game Settings Constants ***************/
    public static final String WINDOWS_NAME = "Pepse Game";
    private static final int BOARD_HEIGHT = 690;
    private static final int BOARD_WIDTH = 1020;
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
    public static final int PADDING = 30;

    /************ Class attributes ***********/
    private int worldLeftEnd, worldRightEnd;
    private Vector2 windowDimensions;
    private Terrain terrain;
    private Avatar avatar;
    private NumericEnergyCounter energyCounter;

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
        Sound backgroundSound = soundReader.readSound("assets/mapleStory.wav");
        backgroundSound.playLooped();
        this.windowDimensions = windowController.getWindowDimensions();

        worldLeftEnd = (int) (-windowDimensions.x());
        worldRightEnd = (int) (windowDimensions.x() * 2f);

        skyCreator();
        terrainCreator(worldLeftEnd, worldRightEnd);
        treesCreator(worldLeftEnd, worldRightEnd);
        gameObjects().layers().shouldLayersCollide(LEAVES_LAYER, TERRAIN_LAYER, true);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TRUNK_LAYER, true);
        createAvatar(inputListener, imageReader);
        numericEnergyCreator();
    }

    private void numericEnergyCreator() {
        energyCounter = new NumericEnergyCounter(
                new Vector2(windowDimensions.x() * 0.1f, windowDimensions.y() * 0.1f),
                new Vector2(30f, 30f),
                avatar.getEnergy());
        gameObjects().addGameObject(energyCounter, Layer.UI);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        Vector2 curPosition = avatar.getCenter();
        Sun.circleCenter = new Vector2(camera().getCenter().x(), windowDimensions.y());
        energyCounter.setTopLeftCorner(new Vector2(camera().getTopLeftCorner().x() + PADDING,
                camera().getTopLeftCorner().y() + PADDING));

        if (curPosition.x() > worldRightEnd - windowDimensions.x()) {
            createWorld(Direction.right);
            deleteWorld(Direction.left);
            worldRightEnd += windowDimensions.x();
            worldLeftEnd += windowDimensions.x();
        }

        if (curPosition.x() < worldLeftEnd + windowDimensions.x()) {
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
        float initialX = windowDimensions.x() / 2f;
        Vector2 initialPosition = new Vector2(initialX, terrain.groundHeightAt(initialX) - Block.SIZE * 3);
        avatar = Avatar.create(gameObjects(), Layer.DEFAULT, initialPosition, inputListener, imageReader);
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
    }

    private void treesCreator(int start, int end) {
        for (int curX = start; curX <= end; curX += 2 * Block.SIZE) {
            if (Tree.shouldPlantTree(RANDOM_SEED, curX)) {
                float curY = terrain.groundHeightAt(curX);
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