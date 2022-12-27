package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.util.Vector2;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;

public class PepseGameManager extends GameManager {

    /************ Game Settings Constants ***************/
    public static final String WINDOWS_NAME = "Pepse Game";
    private static final int BOARD_HEIGHT = 700;
    private static final int BOARD_WIDTH = 1005;

    private static final float NIGHT_CYCLE_LEN = 10;
    private static final float SUNSET_CYCLE = NIGHT_CYCLE_LEN * 2;

    /************** Terrain properties ***************/
    public static final int TERRAIN_SEED = 1000;


    /************ Class Functions ***********/

    /**
     * The constructor of Pepse Game Manager
     *
     * @param windowTitle The name of the window
     * @param windowDimensions the windows dimension
     */
    public PepseGameManager(String windowTitle, Vector2 windowDimensions) {
        super(windowTitle,windowDimensions);
    }


    /**
     * Initializes the PepseGameManager
     *
     * @param imageReader the image reader to use for loading images
     * @param soundReader the sound reader to use for loading sounds
     * @param inputListener the user input listener to use for handling user input
     * @param windowController the window controller to use for controlling the game window
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {

        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        skyCreator(windowController.getWindowDimensions());
        terrainCreator(windowController);
    }
    /**
     * Creates a new terrain and adds it to the list of game objects.
     *
     * @param windowController the window controller that controls the game window
     */
    private void terrainCreator(WindowController windowController) {
        Terrain terrain = new Terrain(this.gameObjects(),Layer.STATIC_OBJECTS,
                windowController.getWindowDimensions(), TERRAIN_SEED);
        terrain.createInRange(0, (int) windowController.getWindowDimensions().x());
    }

    /**
     * Creates a new sky and adds it to the list of game objects.
     *
     * @param windowDimensions the windowDimensions of game
     */
    private void skyCreator(Vector2 windowDimensions) {
        GameObject sky = Sky.create(gameObjects(), windowDimensions, Layer.BACKGROUND);
        GameObject night = Night.create(gameObjects(), Layer.FOREGROUND, windowDimensions, NIGHT_CYCLE_LEN);
        GameObject sun = Sun.create(gameObjects(), Layer.BACKGROUND, windowDimensions, SUNSET_CYCLE);
    }


    public static void main(String[] args) {
        new PepseGameManager(WINDOWS_NAME, new Vector2(BOARD_WIDTH, BOARD_HEIGHT)).run();
    }
}
