import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AStarPathingStrategy
        implements PathingStrategy
{


    public List<Point> computePath(Point start, Point end,
                                   Predicate<Point> canPassThrough,
                                   BiPredicate<Point, Point> withinReach,
                                   Function<Point, Stream<Point>> potentialNeighbors) {
        ArrayList<FPoint> openList = new ArrayList<>();
        List<Point> closedList = new LinkedList<>();
        LinkedList<Point> path = new LinkedList<>();

        Comparator<FPoint> comp = new SortByFValue();

        HashMap<Point, Point> cameFrom = new HashMap<>();
        HashMap<Point, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);

        openList.add(new FPoint(start, distance(start, end)));

        while (openList.size() > 0) {
            FPoint current = openList.remove(0);
            if (withinReach.test(current.getPoint(), end)) {
                Point currPt = current.getPoint();
                path.addFirst(currPt);
                while (cameFrom.containsKey(currPt)) {
                    currPt = cameFrom.get(currPt);
                    if (!currPt.equals(start)) {
                        path.addFirst(currPt);
                    }
                }
                return path;
            }
            List<Point> adjPoints = potentialNeighbors.apply(current.getPoint())
                    .filter(canPassThrough)
                    .filter(pt -> !closedList.contains(pt))
                    .collect(Collectors.toList());

            for (Point pt : adjPoints) {
                int g = gScore.get(current.getPoint()) + 1;
                if (!gScore.containsKey(pt)){
                    gScore.put(pt, Integer.MAX_VALUE);
                }
                if (g < gScore.get(pt)) {
                    cameFrom.put(pt, current.getPoint());
                    gScore.put(pt, g);
                    FPoint ptF = new FPoint(pt, g + distance(pt, end));
                    if (!openList.contains(ptF)) {
                        openList.add(ptF);
                        openList.sort(comp);
                    }
                }
            }
            closedList.add(current.getPoint());
        }
        return path;
    }

    private int distance(Point a, Point b){
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}

