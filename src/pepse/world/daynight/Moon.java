package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

public class Moon {

    private static final String MOON_TAG = "moon";
    private static final Color MOON_COLOR = Color.LIGHT_GRAY;
    public static final float MOON_SIZE = 150;
    private static final float MOON_ROTATION_RADIUS = 550;
    private static final float INIT_MOON_ANGLE = 90;
    private static final float END_MOON_ANGLE = 470;

    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        Renderable sunRenderable = new OvalRenderable(MOON_COLOR);
        GameObject moon = new GameObject(Vector2.ZERO, new Vector2(MOON_SIZE, MOON_SIZE), sunRenderable);
        moon.setTag(MOON_TAG);
        gameObjects.addGameObject(moon, layer);
        Vector2 circleCenter = new Vector2(windowDimensions.x() / 2, windowDimensions.y()); // TODO: change to screen center (to stay center if player moves)

        Moon.MoonMover mover = angle -> {
            float x = (float) (circleCenter.x() + MOON_ROTATION_RADIUS * Math.cos(Math.toRadians(angle)));
            float y = (float) (circleCenter.y() + MOON_ROTATION_RADIUS * Math.sin(Math.toRadians(angle)));
            moon.setCenter(new Vector2(x, y));
        };

        new Transition<>(moon, mover::rotate,
                INIT_MOON_ANGLE,
                END_MOON_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP, null);
        return moon;
    }


    /**
     * This interface is a FunctionalInterface that contains a function that rotates the moon
     */
    @FunctionalInterface
    interface MoonMover {
        /**
         * Rotates the moon position
         *
         * @param angle the angle to rotate
         */
        void rotate(float angle);
    }
}
