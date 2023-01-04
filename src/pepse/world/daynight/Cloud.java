package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.Transition;
import danogl.gui.ImageReader;
import danogl.gui.rendering.ImageRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class Cloud {

    public static final float CLOUD_HEIGHT = 150f;
    public static final float CLOUD_WIDTH = 200f;

    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength, ImageReader imageReader, Vector2 topLeftCorner,
                                    float startX)
    {
        Renderable cloudRenderable = imageReader.readImage("assets/Clouds/Cloud.png",true);
        GameObject cloud = new GameObject(topLeftCorner,new Vector2(CLOUD_WIDTH, CLOUD_HEIGHT),cloudRenderable);
        gameObjects.addGameObject(cloud,layer);

        CloudMover mover = movement -> {
            cloud.setCenter(new Vector2(movement, cloud.getCenter().y()));
        };

        new Transition<>(cloud, mover::move,
                startX, windowDimensions.x(), Transition.LINEAR_INTERPOLATOR_FLOAT, cycleLength,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);

        return cloud;
    }
}

@FunctionalInterface
interface CloudMover{
    void move(float movement);
}
