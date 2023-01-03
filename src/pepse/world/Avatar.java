package pepse.world;

import danogl.collisions.Collision;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Avatar extends GameObject {
    private final int MOVEMENT_SPEED = 300;
    private boolean isOnJump = true;

    private State state = State.jumpNormal;
    private final UserInputListener inputListener;
    private Vector2 movementDir = Vector2.DOWN;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     *                      the GameObject will not be rendered.
     */
    private Avatar(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable, UserInputListener inputListener) {
        super(topLeftCorner, dimensions, renderable);
        this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(100);
        this.inputListener = inputListener;
    }

    private void jump() {
        Vector2 startPos = this.getCenter();
        Vector2 endPos = this.getCenter().subtract(new Vector2(0, 150));

        new Transition<Vector2>(this, t -> setCenter(new Vector2(this.getCenter().x(), t.y())),
                startPos,
                endPos,
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                0.45f,
                Transition.TransitionType.TRANSITION_ONCE, null);
    }


    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        Vector2 movementDir = Vector2.DOWN.mult(MOVEMENT_SPEED);

        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
            movementDir = movementDir.add(Vector2.LEFT.mult(MOVEMENT_SPEED));
            if (isJumpState())
                state = State.jumpLeft;
            else if (isFlightState())
                state = State.flyLeft;
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            movementDir = movementDir.add(Vector2.RIGHT.mult(MOVEMENT_SPEED));
            if (isJumpState())
                state = State.jumpRight;
            else if (isFlightState())
                state = State.flyRight;
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && inputListener.isKeyPressed(KeyEvent.VK_SHIFT)) {
            movementDir = movementDir.add(Vector2.UP.mult(MOVEMENT_SPEED * 2));
            state = isFlightState() ? state: State.flyNormal;
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && !isJumpState() && !isFlightState()) {
            state = State.jumpNormal;
            jump();
        }
        setVelocity(movementDir);
        transform().setAccelerationY(500f);
        updateRenderable();
    }

    private void updateRenderable() {

    }

    private boolean isJumpState() {
        return state == State.jumpNormal || state == State.jumpRight || state == State.jumpLeft;
    }

    private boolean isFlightState() {
        return state == State.flyNormal || state == State.flyRight || state == State.flyLeft;
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        new ScheduledTask(this, 0.01f, false, () -> this.state = State.walk);

    }

    public static Avatar create(GameObjectCollection gameObjects,
                                int layer, Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader) {

//        Renderable renderable = imageReader.readImage("assets/front_knight.png", true);
        Renderable renderable = new RectangleRenderable(Color.BLACK);
        Avatar avatar = new Avatar(topLeftCorner, new Vector2(50, 50), renderable, inputListener);
        gameObjects.addGameObject(avatar, layer);
        return avatar;
    }

    enum State {
        walk,
        moveRight,
        moveLeft,
        jumpNormal,
        jumpRight,
        jumpLeft,
        flyNormal,
        flyLeft,
        flyRight
    }
}
