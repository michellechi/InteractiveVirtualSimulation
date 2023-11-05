import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Engineer extends MovingEntity {

    //   private PathingStrategy strategy = new SingleStepPathingStrategy();
    private PathingStrategy strategy = new AStarPathingStrategy();

    private static final String QUAKE_KEY = "quake";

    public Engineer(
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
        if (this.getPosition().adjacent(target.getPosition())) {
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
            EventScheduler scheduler)
    {
        Optional<Entity> brokenCarTarget =
                world.findNearest(this.getPosition(), BrokenCar.class);
        long nextPeriod = this.getActionPeriod();

        if (brokenCarTarget.isPresent() && this.moveTo(world, brokenCarTarget.get(), scheduler)) {
            Point tgtPos = brokenCarTarget.get().getPosition();
                Quake quake = new Quake(tgtPos,
                        imageStore.getImageList(QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += this.getActionPeriod();
                quake.scheduleActions(scheduler, world, imageStore);
            }

        else {
            scheduler.scheduleEvent(this,
                    new Activity(this, world, imageStore), nextPeriod);
        }
    }
}
