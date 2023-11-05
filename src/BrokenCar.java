import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class BrokenCar extends ActiveEntity {

    private int actionPeriod;
    private int animationPeriod;

    public BrokenCar(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod)
    {
        super(id, position, images, actionPeriod);
        this.animationPeriod = animationPeriod;
    }

    public int getAnimationPeriod(){
        return animationPeriod;
    }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {

        Optional<Entity> engineerTarget =
                world.findNearest(this.getPosition(), Engineer.class);

        if (engineerTarget.isPresent() && this.getPosition().adjacent(engineerTarget.get().getPosition())) {
                transformFixed(world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent(this,
                    new Activity(this, world, imageStore), this.getActionPeriod());
        }
    }

    public void transformFixed(WorldModel world,
                                EventScheduler scheduler,
                                ImageStore imageStore){
        ActiveEntity car = new FixedCar(this.getID(), this.getPosition(), this.getImages(), this.getActionPeriod(),
                this.getAnimationPeriod());

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(car);
        car.scheduleActions(scheduler, world, imageStore);
    }
}
