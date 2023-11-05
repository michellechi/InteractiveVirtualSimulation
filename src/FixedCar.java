import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class FixedCar extends MovingEntity {

    //   private PathingStrategy strategy = new SingleStepPathingStrategy();
    private PathingStrategy strategy = new AStarPathingStrategy();

    public FixedCar(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod)
    {
        super(id, position, images, actionPeriod, animationPeriod);
    }

    public Point nextPosition(WorldModel world, Point destPos) {
        Point pos = this.getPosition();
        List<Point> points;

        points = strategy.computePath(pos, destPos,
                p -> world.withinBounds(p) && !world.isOccupied(p),
                (p1, p2) -> p1.adjacent(p2),
                PathingStrategy.CARDINAL_NEIGHBORS);
        //DIAGONAL_NEIGHBORS);
        //DIAGONAL_CARDINAL_NEIGHBORS);

        if (points.size() == 0) {
            return pos;
        }
        else {
            return points.get(0);
        }
    }

    public boolean moveTo(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (this.getPosition().adjacent(target.getPosition())) { // FIGURE THIS OUT
            return true;
        }
        else {
            Point nextPos = this.nextPosition(world, target.getPosition());

            if (!this.getPosition().equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    public void executeActivity(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler) {

        Optional<Entity> pyroTarget =
                world.findNearest(this.getPosition(), Pyro.class);

        if (pyroTarget.isPresent() && this.getPosition().adjacent(pyroTarget.get().getPosition())) {
            world.removeEntity(pyroTarget.get());
                transformPyroCar(world, scheduler, imageStore);
            Quake quake = new Quake(this.getPosition(), imageStore.getImageList("quake"));
            world.addEntity(quake);
            quake.scheduleActions(scheduler, world, imageStore);
            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);
        }
        else {
            scheduler.scheduleEvent(this,
                    new Activity(this, world, imageStore), this.getActionPeriod());
        }
    }

    public void transformPyroCar(WorldModel world,
                               EventScheduler scheduler,
                               ImageStore imageStore){

        Pyro pyro = new Pyro("pyro", this.getPosition(), getImages(), 8, 9, false);
        ActiveEntity car = new PyroCar(this.getID(), this.getPosition(), imageStore.getImageList("car"), this.getActionPeriod(),
                this.getAnimationPeriod(), pyro);

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(car);
        car.scheduleActions(scheduler, world, imageStore);
    }

}
