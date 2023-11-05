import processing.core.PImage;

import java.util.List;

public abstract class MinerEntity extends MovingEntity{

    private int resourceLimit;
    private int resourceCount;
    //   private PathingStrategy strategy = new SingleStepPathingStrategy();
    private PathingStrategy strategy = new AStarPathingStrategy();

    public MinerEntity(String id, Point position, List<PImage> images, int actionPeriod, int animationPeriod,
                       int resourceLimit, int resourceCount){
        super(id, position, images, actionPeriod, animationPeriod);
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
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

    protected int getResourceLimit(){
        return this.resourceLimit;
    }

    protected int getResourceCount(){
        return this.resourceCount;
    }

    protected void incrementResourceCount(){
        this.resourceCount ++;
    }
}
