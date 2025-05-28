package knu.pyh.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Circle extends Shape {

    public Circle(Point center, double radius) {
        super(center, radius);
    }

    // TODO: 학생 과제 - 원의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        if (other instanceof Circle) {
            // Circle vs Circle 겹침 검사
            Circle otherCircle = (Circle) other;
            double dx = this.center.getX() - otherCircle.center.getX();
            double dy = this.center.getY() - otherCircle.center.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            return distance <= (this.radius + otherCircle.radius);
        } else {
            // Circle vs Polygon 겹침 검사
            List<Point> vertices = other.getVertices();
            int n = vertices.size();

            // 1. 다각형의 각 변이 원과 겹치는지 확인
            for (int i = 0; i < n; i++) {
                Point p1 = vertices.get(i);
                Point p2 = vertices.get((i + 1) % n);

                // 선분 p1-p2와 원의 교차 여부 검사
                double dx = p2.getX() - p1.getX();
                double dy = p2.getY() - p1.getY();

                double fx = p1.getX() - center.getX();
                double fy = p1.getY() - center.getY();

                double a = dx * dx + dy * dy;
                double b = 2 * (fx * dx + fy * dy);
                double c = (fx * fx + fy * fy) - radius * radius;

                double discriminant = b * b - 4 * a * c;
                if (discriminant >= 0) {
                    discriminant = Math.sqrt(discriminant);
                    double t1 = (-b - discriminant) / (2 * a);
                    double t2 = (-b + discriminant) / (2 * a);

                    if ((t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1)) {
                        return true;  // 원과 변이 겹침
                    }
                }
            }

            // 2. 원의 중심이 다각형 내부에 있는지 확인 (Ray Casting 알고리즘)
            int intersections = 0;
            double x = center.getX();
            double y = center.getY();
            for (int i = 0; i < n; i++) {
                Point v1 = vertices.get(i);
                Point v2 = vertices.get((i + 1) % n);

                if (((v1.getY() > y) != (v2.getY() > y)) &&
                        (x < (v2.getX() - v1.getX()) * (y - v1.getY()) / (v2.getY() - v1.getY()) + v1.getX())) {
                    intersections++;
                }
            }

            if (intersections % 2 == 1) {
                return true; // 원 중심이 다각형 내부
            }

            return false; // 겹치지 않음
        }
    }


    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "circle");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);
        return json;
    }

    @Override
    public String getShapeType() {
        return "circle";
    }

    @Override
    public List<Point> getVertices() {
        // 원의 경계를 근사하는 점들 생성
        List<Point> vertices = new ArrayList<>();
        int numPoints = 32;
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            vertices.add(new Point(x, y));
        }
        return vertices;
    }
}