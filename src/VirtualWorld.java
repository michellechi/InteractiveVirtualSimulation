import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Optional;

import processing.core.*;

public final class VirtualWorld extends PApplet
{
    private static final int TILE_SIZE = 32;

    public static final int TIMER_ACTION_PERIOD = 100;

    public static final int VIEW_WIDTH = 640;
    public static final int VIEW_HEIGHT = 480;
    public static final int TILE_WIDTH = 32;
    public static final int TILE_HEIGHT = 32;
    public static final int WORLD_WIDTH_SCALE = 2;
    public static final int WORLD_HEIGHT_SCALE = 2;

    public static final int VIEW_COLS = VIEW_WIDTH / TILE_WIDTH;
    public static final int VIEW_ROWS = VIEW_HEIGHT / TILE_HEIGHT;
    public static final int WORLD_COLS = VIEW_COLS * WORLD_WIDTH_SCALE;
    public static final int WORLD_ROWS = VIEW_ROWS * WORLD_HEIGHT_SCALE;

    public static final String IMAGE_LIST_FILE_NAME = "imagelist";
    public static final String DEFAULT_IMAGE_NAME = "background_default";
    public static final int DEFAULT_IMAGE_COLOR = 0x808080;

    public static final String LOAD_FILE_NAME = "world.sav";

    public static final String FAST_FLAG = "-fast";
    public static final String FASTER_FLAG = "-faster";
    public static final String FASTEST_FLAG = "-fastest";
    public static final double FAST_SCALE = 0.5;
    public static final double FASTER_SCALE = 0.25;
    public static final double FASTEST_SCALE = 0.10;

    public static double timeScale = 1.0;

    public ImageStore imageStore;
    public WorldModel world;
    public WorldView view;
    public EventScheduler scheduler;

    public long nextTime;

    private GridValues[][] grid;

    private static enum GridValues { BACKGROUND, OBSTACLE, ENTITY};

    public void settings() {
        size(VIEW_WIDTH, VIEW_HEIGHT);
    }

    /*
       Processing entry point for "sketch" setup.
    */
    public void setup() {
        this.imageStore = new ImageStore(
                createImageColored(TILE_WIDTH, TILE_HEIGHT,
                                   DEFAULT_IMAGE_COLOR));
        this.world = new WorldModel(WORLD_ROWS, WORLD_COLS,
                                    createDefaultBackground(imageStore));
        this.view = new WorldView(VIEW_ROWS, VIEW_COLS, this, world, TILE_WIDTH,
                                  TILE_HEIGHT);
        this.scheduler = new EventScheduler(timeScale);

        loadImages(IMAGE_LIST_FILE_NAME, imageStore, this);
        loadWorld(world, LOAD_FILE_NAME, imageStore);

        scheduleActions(world, scheduler, imageStore);

        nextTime = System.currentTimeMillis() + TIMER_ACTION_PERIOD;

        grid = new GridValues[WORLD_ROWS][WORLD_COLS];
    }

    public void draw() {
        long time = System.currentTimeMillis();
        if (time >= nextTime) {
            this.scheduler.updateOnTime(time);
            nextTime = time + TIMER_ACTION_PERIOD;
        }

        view.drawViewport();
    }

    public void keyPressed() {
        if (key == CODED) {
            int dx = 0;
            int dy = 0;

            switch (keyCode) {
                case UP:
                    dy = -1;
                    break;
                case DOWN:
                    dy = 1;
                    break;
                case LEFT:
                    dx = -1;
                    break;
                case RIGHT:
                    dx = 1;
                    break;
            }
            view.shiftView(dx, dy);
        }
    }

    public void mousePressed()
    {
        Point pr = mouseToPoint(mouseX, mouseY);
        Point pressed = view.viewportToWorld(pr.x, pr.y);

        List<Point> around = new ArrayList<>();
        around.add(pressed);
        around.add(new Point(pressed.x - 1, pressed.y - 1));
        around.add(new Point(pressed.x + 1, pressed.y + 1));
        around.add(new Point(pressed.x - 1, pressed.y + 1));
        around.add(new Point(pressed.x + 1, pressed.y - 1));
        around.add(new Point(pressed.x, pressed.y - 1));
        around.add(new Point(pressed.x, pressed.y + 1));
        around.add(new Point(pressed.x - 1, pressed.y));
        around.add(new Point(pressed.x + 1, pressed.y));

        for (Point p : around) {
            world.setBackground(p, new Background("asphalt", imageStore.getImageList("asphalt")));
        }

        Car car = new Car("car", pressed, imageStore.getImageList("car"), 2, 3);
        world.addEntity(car);
        car.scheduleActions(scheduler, world, imageStore);

        Optional<Entity> smith = world.findNearest(pressed, Blacksmith.class);
        if (smith.isPresent()) {
            Point pos1 = smith.get().getPosition();
            world.removeEntity(smith.get());
            scheduler.unscheduleAllEvents(smith.get());
            Office office = new Office("office", pos1, imageStore.getImageList("office"));
            world.addEntity(office);
        }

        Optional<Entity> miner = world.findNearest(pressed, Miner_not_Full.class);
        if (miner.isPresent()) {
            Point pos2 = miner.get().getPosition();
            world.removeEntity(miner.get());
            scheduler.unscheduleAllEvents(miner.get());
            Pyro pyro = new Pyro("pyro", pos2, imageStore.getImageList("pyro"), 8, 9, false);
            world.addEntity(pyro);
            pyro.scheduleActions(scheduler, world, imageStore);
        }

//        if (grid[pressed.y][pressed.x] == GridValues.OBSTACLE)
//            grid[pressed.y][pressed.x] = GridValues.BACKGROUND;
//        else if (grid[pressed.y][pressed.x] == GridValues.BACKGROUND)
//            grid[pressed.y][pressed.x] = GridValues.OBSTACLE;

        view.drawViewport();

    }

    private Point mouseToPoint(int x, int y)
    {
        return new Point(mouseX/TILE_SIZE, mouseY/TILE_SIZE);
    }

    public static Background createDefaultBackground(ImageStore imageStore) {
        return new Background(DEFAULT_IMAGE_NAME,
                              imageStore.getImageList(DEFAULT_IMAGE_NAME));
    }

    public static PImage createImageColored(int width, int height, int color) {
        PImage img = new PImage(width, height, RGB);
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++) {
            img.pixels[i] = color;
        }
        img.updatePixels();
        return img;
    }

    private static void loadImages(
            String filename, ImageStore imageStore, PApplet screen)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            imageStore.loadImages(in, screen);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void loadWorld(
            WorldModel world, String filename, ImageStore imageStore)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            world.load(in, imageStore);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void scheduleActions(
            WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        for (Entity entity: world.entities) {
            if (entity instanceof ActiveEntity) {
                ((ActiveEntity) entity).scheduleActions(scheduler, world, imageStore);
            }
        }
    }

    public static void parseCommandLine(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case FAST_FLAG:
                    timeScale = Math.min(FAST_SCALE, timeScale);
                    break;
                case FASTER_FLAG:
                    timeScale = Math.min(FASTER_SCALE, timeScale);
                    break;
                case FASTEST_FLAG:
                    timeScale = Math.min(FASTEST_SCALE, timeScale);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        parseCommandLine(args);
        PApplet.main(VirtualWorld.class);
    }
}
