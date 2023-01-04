package pepse.world;

import danogl.collisions.Collision;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Counter;
import danogl.util.Vector2;
import pepse.world.trees.Tree;

import java.awt.event.KeyEvent;

public class Avatar extends GameObject {
    public static final int MASS = 100;
    private static final int MOVEMENT_SPEED = 250;
    private static final int MAX_ENERGY = 100;
    public static final int FILL_ENERGY_AMOUNT = 1;
    public static final int GRAVITY = 500;
    public static final float MAX_FALLING_SPEED = 350f;
    public static final float GIF_FRAME_RATE = 0.1f;
    public static final Vector2 AVATAR_SIZE = new Vector2(100, 100);

    private final GameObjectCollection gameObjects;
    private final int layer;
    private GameObject parachute;
    private final Counter energy;
    private final UserInputListener inputListener;
    private final ImageReader imageReader;
    private boolean decreaseEnergy = true;
    private Transition<Float> horizontalTransition = null;
    private State state = State.moveRight;
    private AnimationRenderable walkLeft;
    private AnimationRenderable walkRight;
    private AnimationRenderable flyNormal;
    private AnimationRenderable flyLeft;
    private AnimationRenderable flyRight;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     */
    private Avatar(GameObjectCollection gameObjects, Vector2 topLeftCorner, Vector2 dimensions,
                   UserInputListener inputListener, ImageReader imageReader, int layer) {
        super(topLeftCorner, dimensions, null);
        this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        this.physics().setMass(MASS);
        this.transform().setAccelerationY(GRAVITY);
        this.gameObjects = gameObjects;
        this.inputListener = inputListener;
        this.imageReader = imageReader;
        this.layer = layer;
        this.energy = new Counter(MAX_ENERGY);
        this.parachute = createParachute();
    }

    public static Avatar create(GameObjectCollection gameObjects,
                                int layer, Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader) {

        Avatar avatar = new Avatar(gameObjects, topLeftCorner, AVATAR_SIZE, inputListener, imageReader, layer);
        avatar.walkLeft = createWalkAnimation(imageReader, State.moveLeft);
        avatar.walkRight = createWalkAnimation(imageReader, State.moveRight);
        avatar.flyLeft = createWalkAnimation(imageReader, State.flyLeft);
        avatar.flyRight = createWalkAnimation(imageReader, State.flyRight);
        avatar.flyNormal = createWalkAnimation(imageReader, State.flyNormal);

        gameObjects.addGameObject(avatar, layer);
        return avatar;
    }


    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        manageFreeFall();

        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
            transform().setVelocityX(-MOVEMENT_SPEED);
            if (isJumpState())
                state = State.jumpLeft;
            else if (isFlightState())
                state = State.flyLeft;
            else
                state = State.moveLeft;
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            transform().setVelocityX(MOVEMENT_SPEED);
            if (isJumpState())
                state = State.jumpRight;
            else if (isFlightState())
                state = State.flyRight;
            else
                state = State.moveRight;
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && inputListener.isKeyPressed(KeyEvent.VK_SHIFT) &&
                energy.value() > 0) {
            state = isFlightState() ? state : State.flyNormal;
            this.transform().setVelocityY(-MOVEMENT_SPEED);
        } else if (isFlightState()) {
            if (state == State.flyLeft)
                state = State.jumpLeft;
            else
                state = State.jumpRight;
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && !isJumpState() && !isFlightState()) {
            state = State.jumpNormal;
            this.transform().setVelocityY(-MOVEMENT_SPEED * 1.5f);
        }
        if (!inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                !inputListener.isKeyPressed(KeyEvent.VK_SHIFT) &&
                !inputListener.isKeyPressed(KeyEvent.VK_LEFT) &&
                !inputListener.isKeyPressed(KeyEvent.VK_RIGHT))
            this.transform().setVelocityX(getVelocity().x());


        updateEnergy();
        updateRenderable();
    }

    private void manageFreeFall() {
        parachute.setCenter(this.getCenter().subtract(new Vector2(0, parachute.getDimensions().y())));
        if (getVelocity().y() > 400f) {
            if (horizontalTransition == null) {
                applyWind();
                gameObjects.addGameObject(parachute, layer);
            }
            this.setVelocity(new Vector2(getVelocity().x(), MAX_FALLING_SPEED));
        }
    }

    private void updateEnergy() {
        decreaseEnergy = !decreaseEnergy;

        if (decreaseEnergy) {
            if (isFlightState())
                energy.decrement();
            if ((state == State.moveRight || state == State.moveLeft) && energy.value() < MAX_ENERGY)
                energy.increaseBy(FILL_ENERGY_AMOUNT);
            if (energy.value() > MAX_ENERGY) {
                energy.reset();
                energy.increaseBy(MAX_ENERGY);
            }
        }
    }

    private void updateRenderable() {
        if (state == State.moveRight || state == State.jumpRight)
            this.renderer().setRenderable(walkRight);
        else if (state == State.moveLeft || state == State.jumpLeft)
            this.renderer().setRenderable(walkLeft);
        else if (state == State.flyNormal)
            this.renderer().setRenderable(flyNormal);
        else if (state == State.flyRight)
            this.renderer().setRenderable(flyRight);
        else if (state == State.flyLeft)
            this.renderer().setRenderable(flyLeft);
    }

    public Counter getEnergy() {
        return energy;
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
        if (other.getTag().equals(Terrain.TERRAIN_TAG) || other.getTag().equals(Tree.TRUNK_TAG)) {
            gameObjects.removeGameObject(parachute, layer);
            if (horizontalTransition != null){
                this.removeComponent(horizontalTransition);
                this.renderer().setRenderableAngle(0);
            }
            horizontalTransition = null;
        }
        new ScheduledTask(this, 0.01f, false, () -> {
            if (state == State.flyLeft || state == State.jumpLeft || state == State.moveLeft)
                this.state = State.moveLeft;
            else
                this.state = State.moveRight;
        });

    }


    private static AnimationRenderable createWalkAnimation(ImageReader imageReader, State state) {
        if (state == State.moveRight) {
            Renderable renderable1 = imageReader.readImage("assets/mushroom/normal/normal_right_1.png", true);
            Renderable renderable2 = imageReader.readImage("assets/mushroom/normal/normal_right_2.png", true);
            Renderable renderable3 = imageReader.readImage("assets/mushroom/normal/normal_right_3.png", true);
            Renderable renderable4 = imageReader.readImage("assets/mushroom/normal/normal_right_4.png", true);
            Renderable renderable5 = imageReader.readImage("assets/mushroom/normal/normal_right_5.png", true);
            Renderable[] renderables = {renderable1, renderable2, renderable3, renderable4, renderable5, renderable1};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.moveLeft) {
            Renderable renderable1 = imageReader.readImage("assets/mushroom/normal/normal_left_1.png", true);
            Renderable renderable2 = imageReader.readImage("assets/mushroom/normal/normal_left_2.png", true);
            Renderable renderable3 = imageReader.readImage("assets/mushroom/normal/normal_left_3.png", true);
            Renderable renderable4 = imageReader.readImage("assets/mushroom/normal/normal_left_4.png", true);
            Renderable renderable5 = imageReader.readImage("assets/mushroom/normal/normal_left_5.png", true);
            Renderable[] renderables = {renderable1, renderable2, renderable3, renderable4, renderable5, renderable1};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.flyRight) {
            Renderable renderable1 = imageReader.readImage("assets/mushroom/fly/FlyRight1.png", true);
            Renderable renderable2 = imageReader.readImage("assets/mushroom/fly/FlyRight2.png", true);
            Renderable[] renderables = {renderable1, renderable2};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.flyLeft) {
            Renderable renderable1 = imageReader.readImage("assets/mushroom/fly/FlyLeft1.png", true);
            Renderable renderable2 = imageReader.readImage("assets/mushroom/fly/FlyLeft2.png", true);
            Renderable[] renderables = {renderable1, renderable2};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.flyNormal) {
            Renderable renderable1 = imageReader.readImage("assets/mushroom/fly/FlyNormal1.png", true);
            Renderable renderable2 = imageReader.readImage("assets/mushroom/fly/FlyNormal2.png", true);
            Renderable[] renderables = {renderable1, renderable2};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        }
        return null;
    }

    private GameObject createParachute() {
        Renderable parachuteImg = imageReader.readImage("assets/parachute.png", true);
        Vector2 parachuteSize = new Vector2(100, 100);
        Vector2 parachutePos = this.getTopLeftCorner().add(
                new Vector2(this.getDimensions().x() / 2, parachuteSize.y()));
        return new GameObject(parachutePos, parachuteSize, parachuteImg);
    }

    private void applyWind() {
        this.horizontalTransition = new Transition<>(this, this.renderer()::setRenderableAngle,
                -30f,
                30f,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                1f,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
    }

    enum State {
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
