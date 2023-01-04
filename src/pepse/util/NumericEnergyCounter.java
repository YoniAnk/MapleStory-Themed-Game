package pepse.util;

import danogl.GameObject;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Counter;
import danogl.util.Vector2;

public class NumericEnergyCounter extends GameObject {

    public static final String ENERGY_TEXT = "Energy: ";
    private TextRenderable textRenderable;
    private final Counter energyCounter;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     */
    public NumericEnergyCounter(Vector2 topLeftCorner, Vector2 dimensions, Counter energyCounter) {
        super(topLeftCorner, dimensions, null);

        this.energyCounter = energyCounter;
        this.textRenderable = new TextRenderable(ENERGY_TEXT + this.energyCounter.value(),null,false,true);
        this.renderer().setRenderable(textRenderable);
    }

    @Override
    public void update(float deltaTime){
        super.update(deltaTime);

        this.textRenderable = new TextRenderable(ENERGY_TEXT + energyCounter.value(),null,false,true);
        this.renderer().setRenderable(textRenderable);
    }

}
