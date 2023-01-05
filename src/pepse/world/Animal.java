package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.ImageReader;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.trees.Tree;

import java.util.Random;

public class Animal extends GameObject {
    private static final int GRAVITY = 500;
    private static final Vector2 PIG_SIZE = new Vector2(70, 70);
    private static final int MAX_TIME_UNTIL_JUMP = 8;
    private static final int JUMP_SPEED = 300;
    private static final int MOVEMENT_SPEED = 250;
    private static final Random random = new Random();
    private int direction;
    private boolean isOnAir = true;
    private final ImageReader imageReader;

    private Animal(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable, ImageReader imageReader) {
        super(topLeftCorner, dimensions, renderable);
        transform().setAccelerationY(GRAVITY);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(100);
        this.direction = random.nextBoolean() ? MOVEMENT_SPEED : -MOVEMENT_SPEED;
        this.imageReader = imageReader;
    }

    public static Animal create(Animals animal, Vector2 position, ImageReader imageReader) {
        switch (animal) {
            case pig:
                return createPig(imageReader, position);
            case snail:
                return createSnail(imageReader, position);
            default:
                return null;
        }
    }

    private static Animal createSnail(ImageReader imageReader, Vector2 position) {
        return null;
    }

    private static Animal createPig(ImageReader imageReader, Vector2 position) {
        Animal pig = new Animal(position, PIG_SIZE, null, imageReader);
        pig.updateRenderable();
        applyMovement(pig);
        return pig;
    }

    private static void applyMovement(Animal animal) {
        animal.transform().setVelocityX(animal.direction);
        new ScheduledTask(animal, random.nextInt(MAX_TIME_UNTIL_JUMP), true, () -> {
            if (!animal.isOnAir) {
                animal.transform().setVelocityY(-JUMP_SPEED);
                animal.isOnAir = true;
            }
            new ScheduledTask(animal, 0.3f, false, () -> animal.transform().setVelocityX(animal.direction));
        }
        );
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (other.getTag().equals(Tree.TRUNK_TAG)) {
            direction = -direction;
            updateRenderable();
        }
        if (other.getTag().equals(Terrain.TERRAIN_TAG))
            isOnAir = false;
    }

    private void updateRenderable() {
        if (direction < 0)
        {
            Renderable img1 = imageReader.readImage("assets/pig/pig_left_1.png", true);
            Renderable img2 = imageReader.readImage("assets/pig/pig_left_2.png", true);
            Renderable img3 = imageReader.readImage("assets/pig/pig_left_3.png", true);
            AnimationRenderable img = new AnimationRenderable(new Renderable[]{img1, img2, img3}, 0.1f);
            this.renderer().setRenderable(img);
        }
        else {
            Renderable img1 = imageReader.readImage("assets/pig/pig_right_1.png", true);
            Renderable img2 = imageReader.readImage("assets/pig/pig_right_2.png", true);
            Renderable img3 = imageReader.readImage("assets/pig/pig_right_3.png", true);
            AnimationRenderable img = new AnimationRenderable(new Renderable[]{img1, img2, img3}, 0.1f);
            this.renderer().setRenderable(img);
        }
    }
}

