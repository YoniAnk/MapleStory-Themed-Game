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
import pepse.PepseGameManager;
import pepse.world.trees.Tree;

import java.awt.event.KeyEvent;

public class Avatar extends GameObject {
    public static final int MASS = 100;
    private static final int MOVEMENT_SPEED = 250;
    private static final int MAX_ENERGY = 100;
    public static final int GRAVITY = 500;
    public static final float MAX_FALLING_SPEED = 350f;
    public static final float GIF_FRAME_RATE = 0.1f;
    public static final Vector2 AVATAR_SIZE = new Vector2(100, 100);
    public static final float VELOCITY_TO_START_PARACHUTE = 420f;

    /******************** Assets Pathes ********************************/
    public static final String NORMAL_RIGHT_1_PATH = "assets/mushroom/normal/normal_right_1.png";
    public static final String NORMAL_RIGHT_2_PATH = "assets/mushroom/normal/normal_right_2.png";
    public static final String NORMAL_RIGHT_3_PATH = "assets/mushroom/normal/normal_right_3.png";
    public static final String NORMAL_RIGHT_4_PATH = "assets/mushroom/normal/normal_right_4.png";
    public static final String NORMAL_RIGHT_5_PATH = "assets/mushroom/normal/normal_right_5.png";
    public static final String NORMAL_LEFT_1_PATH = "assets/mushroom/normal/normal_left_1.png";
    public static final String NORMAL_LEFT_2_PATH = "assets/mushroom/normal/normal_left_2.png";
    public static final String NORMAL_LEFT_3_PATH = "assets/mushroom/normal/normal_left_3.png";
    public static final String NORMAL_LEFT_4_PATH = "assets/mushroom/normal/normal_left_4.png";
    public static final String NORMAL_LEFT_5_PATH = "assets/mushroom/normal/normal_left_5.png";
    public static final String FLY_RIGHT_1_PATH = "assets/mushroom/fly/FlyRight1.png";
    public static final String FLY_RIGHT_2_PATH = "assets/mushroom/fly/FlyRight2.png";
    public static final String FLY_LEFT_1_PATH = "assets/mushroom/fly/FlyLeft1.png";
    public static final String FLY_LEFT_2_PATH = "assets/mushroom/fly/FlyLeft2.png";
    public static final String FLY_NORMAL_1_PATH = "assets/mushroom/fly/FlyNormal1.png";
    public static final String FLY_NORMAL_2_PATH = "assets/mushroom/fly/FlyNormal2.png";
    public static final String PARACHUTE_PATH = "assets/parachute.png";

    private final GameObjectCollection gameObjects;
    private final int layer;
    private final GameObject parachute;
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
     * Create and return a new Avatar instance.
     *
     * @param gameObjects   Collection of game objects in the game.
     * @param topLeftCorner Top-left corner position of the avatar.
     * @param inputListener Listener for user input events.
     * @param imageReader   Utility for reading images from the file system.
     * @param avatarLayer The layer of the avatar
     */
    private Avatar(GameObjectCollection gameObjects, Vector2 topLeftCorner, Vector2 dimensions,
                   UserInputListener inputListener, ImageReader imageReader, int avatarLayer) {
        super(topLeftCorner, dimensions, null);
        this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        this.physics().setMass(MASS);
        this.transform().setAccelerationY(GRAVITY);
        this.gameObjects = gameObjects;
        this.inputListener = inputListener;
        this.imageReader = imageReader;
        this.layer = avatarLayer;
        this.energy = new Counter(MAX_ENERGY);
        this.parachute = createParachute();
    }

    /**
     * Creates a new Avatar instance and adds it to the specified collection of game objects.
     *
     * @param gameObjects the collection of game objects to add the avatar to
     * @param layer the layer to add the avatar to
     * @param topLeftCorner the position of the avatar in window coordinates (pixels)
     * @param inputListener the user input listener to use for the avatar
     * @param imageReader the image reader to use for creating the avatar's animations
     * @return the newly created Avatar instance
     */
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


    /**
     * Updates the avatar movement
     * @param deltaTime unused
     */
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
            this.transform().setVelocityX(0);


        updateEnergy();
        updateRenderable();
    }

    /**
     * function that manage what's happen when the avatar falls.
     */
    private void manageFreeFall() {
        parachute.setCenter(this.getCenter().subtract(new Vector2(0, parachute.getDimensions().y())));
        if (getVelocity().y() > VELOCITY_TO_START_PARACHUTE) {
            if (horizontalTransition == null) {
                applyWind();
                gameObjects.addGameObject(parachute, PepseGameManager.PARACHUTE_LAYER);
            }
            this.setVelocity(new Vector2(getVelocity().x(), MAX_FALLING_SPEED));
        }
    }

    /**
     * Function that updates the energy when needed
     */
    private void updateEnergy() {
        decreaseEnergy = !decreaseEnergy;

        if (decreaseEnergy) {
            if (isFlightState())
                energy.decrement();
            if ((state == State.moveRight || state == State.moveLeft) && energy.value() < MAX_ENERGY)
                energy.increment();

            if (energy.value() > MAX_ENERGY) {
                energy.reset();
                energy.increaseBy(MAX_ENERGY);
            }
        }
    }

    /**
     * Function that is responsible for updating the avatar's renderable depends on avatar's direction
     */
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

    /**
     * getter for the energy counter
     * @return the Counter for the energy counter
     */
    public Counter getEnergy() {
        return energy;
    }

    /**
     * checks if the avatar is in a jump state
     * @return boolean - true if is in jump state, else - false
     */
    private boolean isJumpState() {
        return state == State.jumpNormal || state == State.jumpRight || state == State.jumpLeft;
    }

    /**
     * checks if the avatar is in a Flight state
     * @return boolean - true if is in flight state, else - false
     */
    private boolean isFlightState() {
        return state == State.flyNormal || state == State.flyRight || state == State.flyLeft;
    }

    /**
     * deals with the logic of what happen when the object enter a collision
     * @param other The GameObject with which a collision occurred.
     * @param collision Information regarding this collision.
     *                  A reasonable elastic behavior can be achieved with:
     *                  setVelocity(getVelocity().flipped(collision.getNormal()));
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (other.getTag().equals(Terrain.TERRAIN_TAG) || other.getTag().equals(Tree.TRUNK_TAG)) {
            gameObjects.removeGameObject(parachute, PepseGameManager.PARACHUTE_LAYER);
            new ScheduledTask(this, 0.01f, false, this::stopRotating);
        }
        if (state == State.flyLeft || state == State.jumpLeft || state == State.moveLeft)
            this.state = State.moveLeft;
        else
            this.state = State.moveRight;
    }

    /**
     * make the avatar stop rotating when hitting the ground (after the parachute opens)
     */
    private void stopRotating() {
        if (horizontalTransition != null) {
            this.removeComponent(horizontalTransition);
            this.renderer().setRenderableAngle(0);
        }
        horizontalTransition = null;
    }


    /**
     * Creates the movable renderable of the avatar - for each state
     * @param imageReader
     * @param state
     * @return
     */
    private static AnimationRenderable createWalkAnimation(ImageReader imageReader, State state) {
        if (state == State.moveRight) {
            Renderable renderable1 = imageReader.readImage(NORMAL_RIGHT_1_PATH, true);
            Renderable renderable2 = imageReader.readImage(NORMAL_RIGHT_2_PATH, true);
            Renderable renderable3 = imageReader.readImage(NORMAL_RIGHT_3_PATH, true);
            Renderable renderable4 = imageReader.readImage(NORMAL_RIGHT_4_PATH, true);
            Renderable renderable5 = imageReader.readImage(NORMAL_RIGHT_5_PATH, true);
            Renderable[] renderables = {renderable1, renderable2, renderable3, renderable4, renderable5, renderable1};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.moveLeft) {
            Renderable renderable1 = imageReader.readImage(NORMAL_LEFT_1_PATH, true);
            Renderable renderable2 = imageReader.readImage(NORMAL_LEFT_2_PATH, true);
            Renderable renderable3 = imageReader.readImage(NORMAL_LEFT_3_PATH, true);
            Renderable renderable4 = imageReader.readImage(NORMAL_LEFT_4_PATH, true);
            Renderable renderable5 = imageReader.readImage(NORMAL_LEFT_5_PATH, true);
            Renderable[] renderables = {renderable1, renderable2, renderable3, renderable4, renderable5, renderable1};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.flyRight) {
            Renderable renderable1 = imageReader.readImage(FLY_RIGHT_1_PATH, true);
            Renderable renderable2 = imageReader.readImage(FLY_RIGHT_2_PATH, true);
            Renderable[] renderables = {renderable1, renderable2};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.flyLeft) {
            Renderable renderable1 = imageReader.readImage(FLY_LEFT_1_PATH, true);
            Renderable renderable2 = imageReader.readImage(FLY_LEFT_2_PATH, true);
            Renderable[] renderables = {renderable1, renderable2};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        } else if (state == State.flyNormal) {
            Renderable renderable1 = imageReader.readImage(FLY_NORMAL_1_PATH, true);
            Renderable renderable2 = imageReader.readImage(FLY_NORMAL_2_PATH, true);
            Renderable[] renderables = {renderable1, renderable2};
            return new AnimationRenderable(renderables, GIF_FRAME_RATE);
        }
        return null;
    }

    /**
     * Creates the parachute for the avatar when it falls
     * @return the parachute object
     */
    private GameObject createParachute() {
        Renderable parachuteImg = imageReader.readImage(PARACHUTE_PATH, true);
        Vector2 parachuteSize = new Vector2(100, 100);
        Vector2 parachutePos = this.getTopLeftCorner().add(
                new Vector2(this.getDimensions().x() / 2, parachuteSize.y()));
        return new GameObject(parachutePos, parachuteSize, parachuteImg);
    }

    /**
     * makes the avatar rotate when the parachute opens
     */
    private void applyWind() {
        this.horizontalTransition = new Transition<>(this, this.renderer()::setRenderableAngle,
                -30f,
                30f,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                1f,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
    }

    /**
     * Enum for all available states of the avatar
     */
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
