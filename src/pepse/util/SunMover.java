package pepse.util;

import danogl.GameObject;
import danogl.util.Vector2;

public class SunMover {
    private final float radius;     // The radius of the circle
    private final Vector2 center;   // The center point of the circle
    private final GameObject sun;   // The object that we want to move on the circle

    public SunMover(GameObject object, Vector2 center, float radius) {
        this.sun = object;
        this.center = center;
        this.radius = radius;
    }

    public void move(float angle) {
        float x = (float) (center.x() + radius * Math.cos(Math.toRadians(angle)));
        float y = (float) (center.y() + radius * Math.sin(Math.toRadians(angle)));
        sun.setCenter(new Vector2(x, y));
    }
}