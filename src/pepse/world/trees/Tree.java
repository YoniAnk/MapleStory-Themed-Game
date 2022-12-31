package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
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
    private static final int LEAVES_SQUARE_SIZE = Block.SIZE * 5;

    public static final Color BASE_TRUNK_COLOR = new Color(100, 50, 20);
    public static final Color BASE_LEAF_COLOR = new Color(50, 200, 30);

    private static final float ODDS_TO_PLANT_TREE = 0.2f;
    private static final int RANDOM_MAX_BOUND = 100;
    private static final int THRESHOLD = (int) (RANDOM_MAX_BOUND * ODDS_TO_PLANT_TREE);


    public static void Create(GameObjectCollection gameObjects, Vector2 groundPos, int layer, int seed) {
        // Create the trunk
        float trunkHeight = getRandomTruckHeight(seed);
        for (float curY = groundPos.y(); curY >= groundPos.y() - trunkHeight; curY -= Block.SIZE) {
            Renderable img = new RectangleRenderable(ColorSupplier.approximateColor(BASE_TRUNK_COLOR));
            Block block = new Block(new Vector2(groundPos.x(), curY), img);
            gameObjects.addGameObject(block, layer);
        }

        // Create the leaves
        Vector2 center = groundPos.subtract(new Vector2(-Block.SIZE / 2f, trunkHeight));
        float startX = center.x() - LEAVES_SQUARE_SIZE / 2f, endX = center.x() + LEAVES_SQUARE_SIZE / 2f;
        float startY = center.y() - LEAVES_SQUARE_SIZE / 2f, endY = center.y() + LEAVES_SQUARE_SIZE / 2f;
        for (float x = startX; x < endX; x += Block.SIZE) {
            for (float y = startY; y < endY; y += Block.SIZE) {
                Renderable img = new RectangleRenderable(ColorSupplier.approximateColor(BASE_LEAF_COLOR));
                Block leaf = new Block(new Vector2(x, y), img);
                gameObjects.addGameObject(leaf, layer);
                applyLeafMoover(leaf);
            }
        }

    }

    public static void applyLeafMoover(Block leaf) {
        int cycleLength = 2;

        // The angle
        float startAngle = -7;
        float endAngle = 7;

        // The size
        Vector2 startSize = new Vector2(Block.SIZE * 0.9f, Block.SIZE * 1.2f);
        Vector2 endSize = new Vector2(Block.SIZE * 1.1f, Block.SIZE);

        Runnable run = () -> {
            new Transition<>(leaf, leaf.renderer()::setRenderableAngle,
                    startAngle,
                    endAngle,
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    cycleLength,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);

            new Transition<>(leaf, leaf::setDimensions,
                    startSize,
                    endSize,
                    Transition.CUBIC_INTERPOLATOR_VECTOR,
                    cycleLength,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
        };
        new ScheduledTask(leaf, new Random().nextInt(3), false, run);
    }


    public static boolean shouldPlantTree(int seed) {
        //TODO: use seed
        return new Random().nextInt(RANDOM_MAX_BOUND) < THRESHOLD;
    }

    public static int getRandomTruckHeight(int seed) {
        return new Random().nextInt(MAXIMUM_TRUNK_HEIGHT - MINIMUM_TRUNK_HEIGHT) + MINIMUM_TRUNK_HEIGHT;
    }

}