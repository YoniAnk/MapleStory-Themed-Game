package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.Random;

public class Tree {

    private static final int MINIMUM_TRUNK_HEIGHT = Block.SIZE * 6;
    private static final int MAXIMUM_TRUNK_HEIGHT = Block.SIZE * 12;
    private static final int LEAVES_SQUARE_SIZE = Block.SIZE * 8;

    public static final Color BASE_TRUNK_COLOR = new Color(100, 50, 20);
    public static final Color BASE_LEAF_COLOR = new Color(50, 200, 30);

    private static final float ODDS_TO_PLANT_TREE = 0.2f;
    private static final int RANDOM_MAX_BOUND = 100;
    private static final int THRESHOLD = (int) (RANDOM_MAX_BOUND * ODDS_TO_PLANT_TREE);



    public static void Create(GameObjectCollection gameObjects, Vector2 groundPos, int layer) {
        // Create the trunk
        float trunkHeight = getRandomTruckHeight();
        for (float curY = groundPos.y(); curY >= groundPos.y() - trunkHeight; curY-= Block.SIZE) {
            Renderable renderable = new RectangleRenderable(ColorSupplier.approximateColor(BASE_TRUNK_COLOR));
            Block block = new Block(new Vector2(groundPos.x(), curY), renderable);
            gameObjects.addGameObject(block, layer);
        }

        // Create the leaves
        Vector2 center = groundPos.add(new Vector2(0, trunkHeight));

    }

    public static boolean shouldPlantTree() {
        return new Random().nextInt(RANDOM_MAX_BOUND) < THRESHOLD;
    }

    public static int getRandomTruckHeight() {
        return new Random().nextInt(MAXIMUM_TRUNK_HEIGHT - MINIMUM_TRUNK_HEIGHT) + MINIMUM_TRUNK_HEIGHT;
    }

}