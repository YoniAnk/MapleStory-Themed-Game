package pepse.world.Monsters;

import danogl.gui.ImageReader;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.util.Objects;
import java.util.Random;

public class MonsterFactory {
    private static final Vector2 PIG_SIZE = new Vector2(80, 70);
    private static final Vector2 SNAIL_SIZE = new Vector2(80, 70);


    private final ImageReader imageReader;
    private final int seed;

    public MonsterFactory(ImageReader imageReader, int seed) {
        this.imageReader = imageReader;
        this.seed = seed;
    }

    public Monster create(Monsters monster, Vector2 topLeftCorner) {
        switch (monster) {
            case pig:
                return createPig(topLeftCorner);
            case snail:
                return createSnail(topLeftCorner);
            default:
                return null;
        }
    }

    private Monster createPig(Vector2 topLeftCorner) {
        Renderable leftImg1 = imageReader.readImage("assets/pig/pig_left_1.png", true);
        Renderable leftImg2 = imageReader.readImage("assets/pig/pig_left_2.png", true);
        Renderable leftImg3 = imageReader.readImage("assets/pig/pig_left_3.png", true);
        AnimationRenderable leftImg = new AnimationRenderable(new Renderable[]{leftImg1, leftImg2, leftImg3}, 0.1f);

        Renderable rightImg1 = imageReader.readImage("assets/pig/pig_right_1.png", true);
        Renderable rightImg2 = imageReader.readImage("assets/pig/pig_right_2.png", true);
        Renderable rightImg3 = imageReader.readImage("assets/pig/pig_right_3.png", true);
        AnimationRenderable rightImg = new AnimationRenderable(new Renderable[]{rightImg1, rightImg2, rightImg3}, 0.1f);

        Vector2 pigPosition = new Vector2(topLeftCorner.x(), topLeftCorner.y() - PIG_SIZE.y());
        return new Monster(pigPosition, PIG_SIZE, leftImg, rightImg);
    }

    private Monster createSnail(Vector2 topLeftCorner) {
        Renderable leftImg1 = imageReader.readImage("assets/snail/snail_left_1.png", true);
        Renderable leftImg2 = imageReader.readImage("assets/snail/snail_left_2.png", true);
        Renderable leftImg3 = imageReader.readImage("assets/snail/snail_left_3.png", true);
        AnimationRenderable leftImg = new AnimationRenderable(new Renderable[]{leftImg1, leftImg2, leftImg3}, 0.1f);

        Renderable rightImg1 = imageReader.readImage("assets/snail/snail_right_1.png", true);
        Renderable rightImg2 = imageReader.readImage("assets/snail/snail_right_1.png", true);
        Renderable rightImg3 = imageReader.readImage("assets/snail/snail_right_1.png", true);
        AnimationRenderable rightImg = new AnimationRenderable(new Renderable[]{rightImg1, rightImg2, rightImg3}, 0.3f);

        Vector2 snailPosition = new Vector2(topLeftCorner.x(), topLeftCorner.y() - SNAIL_SIZE.y());
        return new Monster(snailPosition, SNAIL_SIZE, leftImg, rightImg);
    }

    public Monster getRandomMonster(int x, Vector2 topLeftCor) {
        Monsters[] monsters = Monsters.values();
        Monsters monster = monsters[new Random(Objects.hash(seed, x, topLeftCor)).nextInt(monsters.length)];
        return create(monster, topLeftCor);
    }

}
