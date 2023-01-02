package pepse.world.trees;

import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class Tree {

    /*********** General ***********/
    private static final float ODDS_TO_PLANT_TREE = 0.2f;
    private static final int RANDOM_MAX_BOUND = 100;
    private static final int THRESHOLD = (int) (RANDOM_MAX_BOUND * ODDS_TO_PLANT_TREE);

    /*********** Trunk ***********/
    public static final String TRUNK_TAG = "trunk";
    private static final int MINIMUM_TRUNK_HEIGHT = Block.SIZE * 6;
    private static final int MAXIMUM_TRUNK_HEIGHT = Block.SIZE * 12;
    public static final Color BASE_TRUNK_COLOR = new Color(100, 50, 20);

    /*********** Leaves ***********/
    public static final Color BASE_LEAF_COLOR = new Color(50, 200, 30);
    private static final int LEAVES_SQUARE_SIZE = Block.SIZE * 5;
    private static final int FADEOUT_TIME = 8;
    private static final float LEAF_FALLING_SPEED = 70;


    public static void Create(GameObjectCollection gameObjects, Vector2 groundPos, int trunkLayer,
                              int leavesLayer, int seed) {
        float trunkHeight = getRandomTruckHeight(seed, (int) groundPos.x());
        generateTrunk(gameObjects, groundPos, trunkLayer, trunkHeight);
        generateLeaves(gameObjects, groundPos, leavesLayer, trunkHeight);
    }

    /**
     * Generate the trunk of a tree
     *
     * @param gameObjects the gameObjects collection to add the trunk
     * @param groundPos   the ground position to make the trunk
     * @param trunkLayer  the layer to put the trunk in
     * @param trunkHeight the height of the trunk
     */
    private static void generateTrunk(GameObjectCollection gameObjects, Vector2 groundPos, int trunkLayer, float trunkHeight) {
        for (float curY = groundPos.y(); curY >= groundPos.y() - trunkHeight; curY -= Block.SIZE) {
            Renderable img = new RectangleRenderable(ColorSupplier.approximateColor(BASE_TRUNK_COLOR));
            Block trunk = new Block(new Vector2(groundPos.x(), curY), img);
            trunk.setTag(TRUNK_TAG);
            gameObjects.addGameObject(trunk, trunkLayer);
        }
    }

    /**
     * Generate the leaves around the top of the trunk of the tree
     *
     * @param gameObjects the gameObjects collection to add the trunk
     * @param groundPos   the ground position to make the trunk
     * @param leavesLayer the layer to put the leaves in
     * @param trunkHeight the height of the trunk
     */
    private static void generateLeaves(GameObjectCollection gameObjects, Vector2 groundPos, int leavesLayer, float trunkHeight) {
        Vector2 center = groundPos.subtract(new Vector2(-Block.SIZE / 2f, trunkHeight));
        float startX = center.x() - LEAVES_SQUARE_SIZE / 2f, endX = center.x() + LEAVES_SQUARE_SIZE / 2f;
        float startY = center.y() - LEAVES_SQUARE_SIZE / 2f, endY = center.y() + LEAVES_SQUARE_SIZE / 2f;
        for (float x = startX; x < endX; x += Block.SIZE) {
            for (float y = startY; y < endY; y += Block.SIZE) {
                Renderable img = new RectangleRenderable(ColorSupplier.approximateColor(BASE_LEAF_COLOR));
                Leaf leaf = new Leaf(new Vector2(x, y), img);
                gameObjects.addGameObject(leaf, leavesLayer);
                applyWind(leaf);
                applyLeafDropper(leaf);
            }
        }
    }

    /**
     * Activates the ScheduledTask for leaf dropping
     *
     * @param leaf the leaf to drop
     */
    public static void applyLeafDropper(Leaf leaf) {
        // TODO:
        //      1. fix fadeOut
        //      2. make the leaf layer change when drops so collision check will be more efficient
        //      3. end the fall on hit (save transition in Leaf and delete on collision)

        Vector2 leaf_original_position = leaf.getCenter();
        int lifeTime = new Random().nextInt(60) + 5;
        int die_time = new Random().nextInt(15) + 5;

        Runnable returnToLife = () -> {
            leaf.setCenter(leaf_original_position);
            //leaf.renderer().fadeIn(0.2f);
            //leaf.renderer().setOpaqueness(1);
            leaf.setVelocity(Vector2.ZERO);
        };

        Runnable startFalling = () -> {
            //leaf.renderer().fadeOut(FADEOUT_TIME);
            leaf.transform().setVelocity(0, LEAF_FALLING_SPEED);
            new ScheduledTask(leaf, die_time, false, returnToLife);
        };

        new ScheduledTask(leaf, lifeTime, true, startFalling);
    }

    /**
     * Activates the effect of wind on the leaf
     *
     * @param leaf the leaf to activate the effect
     */
    public static void applyWind(Leaf leaf) {
        // TODO: change to constants
        int cycleLength = 2;

        // The angle
        float startAngle = -7;
        float endAngle = 7;

        // The size
        Vector2 startSize = new Vector2(Block.SIZE * 1.2f, Block.SIZE * 0.9f);
        Vector2 endSize = new Vector2(Block.SIZE, Block.SIZE + 1.1f);

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
                    Transition.LINEAR_INTERPOLATOR_VECTOR,
                    cycleLength,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
        };
        new ScheduledTask(leaf, new Random().nextInt(3), false, run);   //TODO: fix random to use seed(?)
    }

    /**
     * Randomize the decision of planting a tree
     *
     * @param seed the seed for the random
     * @param x    the x position of the tree
     * @return true if should plant a tree, else false
     */
    public static boolean shouldPlantTree(int seed, int x) {
        return new Random(Objects.hash(seed, x)).nextInt(RANDOM_MAX_BOUND) < THRESHOLD;
    }

    /**
     * Randomize the tree's trunk height between MINIMUM_TRUNK_HEIGHT and MAXIMUM_TRUNK_HEIGHT
     *
     * @param seed the seed for the random
     * @param x    the x position of the tree
     * @return the height of the tree
     */
    public static int getRandomTruckHeight(int seed, int x) {
        return new Random(Objects.hash(seed, x)).nextInt(MAXIMUM_TRUNK_HEIGHT - MINIMUM_TRUNK_HEIGHT) +
                MINIMUM_TRUNK_HEIGHT;

    }

}