package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

public class Night {
    private static final Float MIDNIGHT_OPACITY = 0.5f;
    private static final Float DAY_OPACITY = 0f;


    public static GameObject create(
            GameObjectCollection gameObjects, int layer, Vector2 windowDimensions, float cycleLength) {
        Renderable nightRenderable = new RectangleRenderable(Color.BLACK);
        GameObject night = new GameObject(Vector2.ZERO, windowDimensions, nightRenderable);
        night.setTag("Night");
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(night, layer);

        new Transition<>(night, night.renderer()::setOpaqueness,
                DAY_OPACITY,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);

        return night;
    }
}
