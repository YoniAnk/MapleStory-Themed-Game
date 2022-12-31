package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.SunMover;

import java.awt.*;

public class Sun {
    private static final String SUN_TAG = "sun";
    private static final Color SUN_COLOR = Color.YELLOW;
    public static final float SUN_SIZE = 150;
    private static final float SUN_ROTATION_RADIUS = 550;
    private static final float INIT_SUN_ANGLE = 270;
    private static final float END_SUN_ANGLE = 630;

    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        Renderable sunRenderable = new OvalRenderable(SUN_COLOR);
        GameObject sun = new GameObject(Vector2.ZERO, new Vector2(SUN_SIZE, SUN_SIZE), sunRenderable);
        sun.setTag(SUN_TAG);
        gameObjects.addGameObject(sun, layer);
        Vector2 circleCenter = new Vector2(windowDimensions.x() / 2, windowDimensions.y());
        SunMover mover = new SunMover(sun, circleCenter, SUN_ROTATION_RADIUS);
        new Transition<>(sun, mover::move,
                INIT_SUN_ANGLE,
                END_SUN_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP, null);
        return sun;
    }


}