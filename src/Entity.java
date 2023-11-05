import processing.core.PImage;

import java.util.List;

public abstract class Entity
{
    private String id;
    public Point position;
    private List<PImage> images;

    public Entity(String id, Point position, List<PImage> images){
        this.id = id;
        this.position = position;
        this.images = images;
    }

    protected String getID(){
        return this.id;
    }

    protected Point getPosition(){
        return this.position;
    }

    protected void setPosition(Point p) {
        this.position = p;
    }

    protected PImage getCurrentImage() {
        return this.images.get(0);
    }

    protected List<PImage> getImages(){
        return this.images;
    }
}
