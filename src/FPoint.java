public class FPoint {
    private Point point;
    private int f;

    public FPoint(Point point, int f){
        this.point = point;
        this.f = f;
    }

    public int getF() {
        return f;
    }

    public Point getPoint() {
        return point;
    }

    public boolean equals(Object other)
    {
        return other instanceof FPoint &&
                this.point.equals(((FPoint)other).point);
    }
}
