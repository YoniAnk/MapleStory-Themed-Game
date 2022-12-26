package pepse.util;

import danogl.GameObject;
import danogl.util.Vector2;

public class SunMover {
    // The radius of the circle
    private final float radius;

    // The current angle (in radians) of the object on the circle
    private float angle;

    // The center point of the circle
    private final Vector2 center;

    // The object that we want to move on the circle
    private final GameObject sun;

    public SunMover(GameObject object, Vector2 center, float radius) {
        this.sun = object;
        this.center = center;
        this.radius = radius;
    }

    public void move(float angleIncrement) {
        // Update the current angle
        angle = angleIncrement;

        // Calculate the new x and y coordinates for the object based on the angle and radius
        float x = (float) (center.x() + radius * Math.cos(Math.toRadians(angle)));
        float y = (float) (center.y() + radius * Math.sin(Math.toRadians(angle)));

        // Update the object's position
        sun.setCenter(new Vector2(x,y));
    }

}