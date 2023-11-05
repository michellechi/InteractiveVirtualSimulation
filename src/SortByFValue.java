import java.util.Comparator;

public class SortByFValue implements Comparator<FPoint> {
    public int compare(FPoint a, FPoint b){
        return a.getF() - b.getF();
    }
}
