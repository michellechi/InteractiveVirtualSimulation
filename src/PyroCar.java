import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class PyroCar extends MovingEntity {

    private Entity occupant;

    //   private PathingStrategy strategy = new SingleStepPathingStrategy();
    private PathingStrategy strategy = new AStarPathingStrategy();

    public PyroCar(
            String carId,
            Point position,
            List<PImage> carImages,
            int carActionPeriod,
            int carAnimationPeriod,
            Entity occupant) {
        super(carId, position, carImages, carActionPeriod, carAnimationPeriod);
        this.occupant = occupant;
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
        } else {
            return points.get(0);
        }
    }

    public boolean moveTo(
            WorldModel world,
            Entity target,
            EventScheduler scheduler) {
        if (this.getPosition().adjacent(target.getPosition())) { // FIGURE THIS OUT
            return true;
        } else {
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
        Optional<Entity> officeTarget =
                world.findNearest(this.getPosition(), Office.class); // MAKE OFFICE AND FIX
//        long nextPeriod = this.getActionPeriod();

        if (officeTarget.isPresent() && this.moveTo(world, officeTarget.get(), scheduler)) {
//            Pyro pyro = new Pyro(this.getID(), this.getPosition(), this.getImages(),
//                    this.getActionPeriod(), this.getAnimationPeriod(), false);

//            world.addEntity(pyro);
//            nextPeriod += this.getActionPeriod();
//            pyro.scheduleActions(scheduler, world, imageStore);
            Point tgtPos = officeTarget.get().getPosition();
            Quake quake = new Quake(tgtPos,
                    imageStore.getImageList("quake"));

            world.addEntity(quake);
            quake.scheduleActions(scheduler, world, imageStore);
        } else {
            scheduler.scheduleEvent(this,
                    new Activity(this, world, imageStore), getActionPeriod());
        }
//
//        scheduler.scheduleEvent( this,
//                new Activity(this, world, imageStore),
//                nextPeriod);
    }
}

