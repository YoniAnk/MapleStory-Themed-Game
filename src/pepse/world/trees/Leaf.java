package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Terrain;

import java.util.Random;

public class Leaf extends GameObject {
    private static final int FADEOUT_TIME = 4;
    public static final String LEAF_TAG = "leaf";
    private static final float LEAF_FALLING_SPEED = 70;

    Vector2 leaf_original_position;
    private Transition<Float> horizontalTransition;
    private Transition<Float> rotationTransition;
    private Transition<Vector2> sizeTransition;


    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param renderable    The renderable representing the object. Can be null, in which case
     *                      the GameObject will not be rendered.
     */
    public Leaf(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, new Vector2(Block.SIZE, Block.SIZE), renderable);
        this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        setTag(LEAF_TAG);
        this.leaf_original_position = topLeftCorner;
        applyLeafDropper();
        applyWind();

    }

    private void stopLeaf() {
        this.removeComponent(horizontalTransition);
        this.removeComponent(rotationTransition);
        this.removeComponent(sizeTransition);
        this.transform().setVelocity(0, 0);
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (horizontalTransition != null && other.getTag().equals(Terrain.TERRAIN_TAG))
            new ScheduledTask(this, 0.01f, false, this::stopLeaf);
            return;
    }

    private void startFalling() {
        this.renderer().fadeOut(FADEOUT_TIME);
        int die_time = new Random().nextInt(15) + 5;
        this.transform().setVelocityY(LEAF_FALLING_SPEED);
        this.horizontalTransition = new Transition<>(this, this.transform()::setVelocityX,
                -20f,
                20f,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                1f,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
        new ScheduledTask(this, die_time, false, this::returnToLife);
    }

    private void returnToLife() {
        new ScheduledTask(this, 0.01f, false, ()->this.renderer().setOpaqueness(1f));
        this.setTopLeftCorner(leaf_original_position);
        this.transform().setVelocity(0,0);
        applyLeafDropper();
        applyWind();
    }

    private void applyLeafDropper() {
        // TODO:
        //      1. fix fadeOut
        //      2. make the leaf layer change when drops so collision check will be more efficient
        //      3. end the fall on hit (save transition in Leaf and delete on collision)

        int lifeTime = new Random().nextInt(60) + 5;
        new ScheduledTask(this, lifeTime, false, this::startFalling);
    }

    /**
     * Activates the effect of wind on the leaf
     */
    private void applyWind() {
        // TODO: change to constants
        int cycleLength = 2;

        // The angle
        float startAngle = -7;
        float endAngle = 7;

        // The size
        Vector2 startSize = new Vector2(Block.SIZE * 1.2f, Block.SIZE * 0.9f);
        Vector2 endSize = new Vector2(Block.SIZE, Block.SIZE + 1.1f);

        Runnable run = () -> {
            // transition for rotating the leaf
            this.rotationTransition = new Transition<>(this, this.renderer()::setRenderableAngle,
                    startAngle,
                    endAngle,
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    cycleLength,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);

            // transition for changing the leaf's size
            this.sizeTransition = new Transition<>(this, this::setDimensions,
                    startSize,
                    endSize,
                    Transition.LINEAR_INTERPOLATOR_VECTOR,
                    cycleLength,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
        };
        float timeToStart = new Random().nextFloat() * 2;
        new ScheduledTask(this, timeToStart, false, run);   // TODO: fix random to use seed(?)
    }
}
