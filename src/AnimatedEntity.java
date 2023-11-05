import processing.core.PImage;

import java.util.List;

public abstract class AnimatedEntity extends ActiveEntity{

    private int animationPeriod;
    private int imageIndex;

    public AnimatedEntity(String id, Point position, List<PImage> images, int actionPeriod, int animationPeriod){
        super(id, position, images, actionPeriod);
        this.animationPeriod = animationPeriod;
        this.imageIndex = 0;
    }

    protected int getAnimationPeriod() {
        return this.animationPeriod;
    }

    protected void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.getImages().size();
    }

    protected PImage getCurrentImage() {
        return this.getImages().get(imageIndex);
    }
}
