import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Car extends MovingEntity {

    //   private PathingStrategy strategy = new SingleStepPathingStrategy();
    private PathingStrategy strategy = new AStarPathingStrategy();

    public Car(
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
            EventScheduler scheduler)
    {
        Optional<Entity> veinTarget =
                world.findNearest(this.getPosition(), Vein.class);

        if (veinTarget.isPresent() && this.moveTo(world,
                veinTarget.get(), scheduler)) {
                transformBroken(world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent(this,
                    new Activity(this, world, imageStore), this.getActionPeriod());
        }
    }

    public void transformBroken(WorldModel world,
                                   EventScheduler scheduler,
                                   ImageStore imageStore){
        ActiveEntity brokenCar = new BrokenCar(this.getID(), this.getPosition(), imageStore.getImageList("car"), this.getActionPeriod(),
                this.getAnimationPeriod());

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(brokenCar);
        brokenCar.scheduleActions(scheduler, world, imageStore);
        Quake quake = new Quake(this.getPosition(), imageStore.getImageList("quake"));
        world.addEntity(quake);
        quake.scheduleActions(scheduler, world, imageStore);

        Optional<Entity> blob = world.findNearest(this.getPosition(), Ore_Blob.class);
        Optional<Entity> miner = world.findNearest(this.getPosition(), Miner_not_Full.class);
        if (blob.isPresent()) {
            Point pos = blob.get().getPosition();
            world.removeEntity(blob.get());
            scheduler.unscheduleAllEvents(blob.get());
            Engineer engineer = new Engineer("engineer", pos, imageStore.getImageList("engineer"), 8, 9);
            world.addEntity(engineer);
            engineer.scheduleActions(scheduler, world, imageStore);
        }
        if (miner.isPresent()) {
            Point pos = miner.get().getPosition();
            world.removeEntity(miner.get());
            scheduler.unscheduleAllEvents(miner.get());
            Engineer engineer = new Engineer("engineer", pos, imageStore.getImageList("engineer"), 8, 9);
            world.addEntity(engineer);
            engineer.scheduleActions(scheduler, world, imageStore);
        }
    }
}
