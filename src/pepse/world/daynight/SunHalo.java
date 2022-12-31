package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import danogl.components.Component;
import java.awt.*;

public class SunHalo {
    private static final String SUN_HALO_TAG = "sun_halo";
    private static final float HALO_SIZE = 120;

    public static GameObject create(GameObjectCollection gameObjects, int layer, GameObject sun, Color color) {
        Renderable haloRenderable = new OvalRenderable(color);
        Vector2 haloRadius = new Vector2(HALO_SIZE + Sun.SUN_SIZE, HALO_SIZE + Sun.SUN_SIZE);
        GameObject sunHalo = new GameObject(Vector2.ZERO, haloRadius, haloRenderable);
        sunHalo.setTag(SUN_HALO_TAG);

        Component haloMover = (deltaTime) -> sunHalo.setCenter(sun.getCenter());
        sunHalo.addComponent(haloMover);
        gameObjects.addGameObject(sunHalo, layer);
        return sunHalo;
    }
}
