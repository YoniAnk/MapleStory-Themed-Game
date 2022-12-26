package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

public class Sun {
    private static final Color SUN_COLOR = Color.YELLOW;
    private static final Vector2 SUN_SIZE = new Vector2(100, 100);

    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        Renderable sunRenderable = new OvalRenderable(SUN_COLOR);
        GameObject sun = new GameObject(new Vector2(200, 200), SUN_SIZE, sunRenderable);
        return sun;
        
    }

}
