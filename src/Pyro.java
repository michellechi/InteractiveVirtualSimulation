import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Pyro extends MovingEntity {

    private PathingStrategy strategy = new AStarPathingStrategy();
    private boolean foundCar;

    public Pyro(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod,
            boolean foundCar)
    {
        super(id, position, images, actionPeriod, animationPeriod);
        this.foundCar = foundCar;
    }

    public void setFoundCar(boolean var) {
        this.foundCar = var;
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
            scheduler.unscheduleAllEvents(target);
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
        Optional<Entity> carTarget =
                world.findNearest(this.getPosition(), FixedCar.class);
        long nextPeriod = this.getActionPeriod();

        if (carTarget.isPresent() && this.moveTo(world,
                carTarget.get(), scheduler)) {
            world.removeEntity(this);
            world.removeEntity(carTarget.get());
            FixedCar car = (FixedCar) carTarget.get();
            world.removeEntity(carTarget.get());
            car.transformPyroCar(world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent(this,
                    new Activity(this, world, imageStore),
                    nextPeriod);
        }
    }
}
