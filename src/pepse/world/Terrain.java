package pepse.world;

import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.*;


public class Terrain {

    /********** Terrain Constants  ***************/
    public static final String TERRAIN_TAG = "terrain";
    private static final double TERRAIN_HEIGHT_RATIO = 0.666;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);

    private static final int TERRAIN_DEPTH = 20;  //20


    /*************** Variables *****************/
    private final GameObjectCollection gameObjects;
    private final int groundLayer;
    private int groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;
    private final Vector2 windowsDimensions;

    public Terrain(GameObjectCollection gameObjects, int groundLayer, Vector2 windowDimensions, int seed) {
        this.gameObjects = gameObjects;
        this.groundLayer = groundLayer;
        this.groundHeightAtX0 = 0;
        this.windowsDimensions = windowDimensions;
        this.groundHeightAtX0 = (int) ((int) windowDimensions.y() * TERRAIN_HEIGHT_RATIO);
        this.noiseGenerator = new NoiseGenerator(seed);
    }

    public float groundHeightAt(float x) {
        int blockSize = 30; // size of each block in pixels
        int frequency = 50;
        int numBlocks = (int) (windowsDimensions.x() / blockSize); // number of blocks in the terrain

        double noiseValue = this.noiseGenerator.noise((int) (x / blockSize), numBlocks, frequency);
        // scale the noise value to the desired range
        int terrainHeight = (int) (noiseValue * windowsDimensions.y() / 2.0f);
        float height = this.groundHeightAtX0 - terrainHeight;
        return (float) Math.floor(height / Block.SIZE) * Block.SIZE;
    }

    public void createInRange(int minX, int maxX) {
        for (int curX = minX; curX < maxX; curX += Block.SIZE) {
            float curY = groundHeightAt(curX);
            for (int i = 0; i < TERRAIN_DEPTH; ++i) {
                Renderable renderable = new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
                Block block = new Block(new Vector2(curX, curY), renderable);
                block.setTag(TERRAIN_TAG);
                this.gameObjects.addGameObject(block, groundLayer);
                curY += Block.SIZE;
            }
        }
    }
}
