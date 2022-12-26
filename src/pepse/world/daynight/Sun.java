package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.SunMover;

import java.awt.*;
import java.util.function.Consumer;

public class Sun {
    private static final Color SUN_COLOR = Color.YELLOW;
    private static final Vector2 SUN_SIZE = new Vector2(100, 100);

    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        Renderable sunRenderable = new OvalRenderable(SUN_COLOR);

        GameObject sun = new GameObject(Vector2.ZERO, new Vector2(SUN_SIZE, SUN_SIZE), sunRenderable);
        gameObjects.addGameObject(sun, layer);

        SunMover mover = new SunMover(sun,new Vector2(windowDimensions.x()/2, windowDimensions.y()),
                SUN_ROTATION_RADIUS);

        new Transition<Float>(sun, mover::move,
                INIT_SUN_ANGLE,
                END_SUN_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP, null);

        return sun;
    }



}
