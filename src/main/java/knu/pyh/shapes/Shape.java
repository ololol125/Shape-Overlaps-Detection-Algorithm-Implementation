package knu.pyh.shapes;

import org.json.JSONObject;
import java.util.List;

public abstract class Shape {
    protected Point center;
    protected String color;
    protected double radius;
    protected String id;

    public Shape(Point center, double radius) {
        this.center = center;
        this.radius = radius;
        this.id = generateId();
        this.color = generateRandomColor();
    }

    protected String generateId() {
        return "shape_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    protected String generateRandomColor() {
        int r = (int)(Math.random() * 256);
        int g = (int)(Math.random() * 256);
        int b = (int)(Math.random() * 256);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public Point getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    // TODO: 학생 과제 - 이 메서드를 구현하세요
    public abstract boolean overlaps(Shape other);

    public abstract JSONObject toJSON();
    public abstract String getShapeType();
    public abstract List<Point> getVertices();
}
