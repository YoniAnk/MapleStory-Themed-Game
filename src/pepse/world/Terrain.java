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
    private static final double TERRAIN_HEIGHT_RATIO = 0.666;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);

    private static final int TERRAIN_DEPTH = 20;  //TODO: check what is it


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
        return (this.groundHeightAtX0 - terrainHeight);
    }

    public void createInRange(int minX, int maxX) {

        for (int curX = minX; curX < maxX; curX += Block.SIZE) {
            double floorHeight = Math.floor(groundHeightAt(curX) / Block.SIZE) * Block.SIZE;
            for (int curY =(int)this.windowsDimensions.y(); curY >= (int)floorHeight; curY -= Block.SIZE) {
                Renderable renderable = new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
                Block block = new Block(new Vector2(curX, curY), renderable);
                this.gameObjects.addGameObject(block);
            }
        }
    }
}
