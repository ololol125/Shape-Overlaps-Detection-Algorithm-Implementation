package knu.pyh.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class RegularPolygon extends Shape {
    private int sides;
    private double rotationAngle;
    private List<Point> vertices;

    public RegularPolygon(Point center, double radius, int sides, double rotationAngle) {
        super(center, radius);
        this.sides = sides;
        this.rotationAngle = rotationAngle;
        this.vertices = generateVertices();
    }

    private List<Point> generateVertices() {
        List<Point> points = new ArrayList<>();
        double angleStep = 2 * Math.PI / sides;

        for (int i = 0; i < sides; i++) {
            double angle = angleStep * i + rotationAngle;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            points.add(new Point(x, y));
        }

        return points;
    }

    // TODO: 학생 과제 - 정다각형의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        // SAT 알고리즘 구현 (다각형 vs 다각형, 다각형 vs 원 모두 대응)

        List<Point> verticesA = this.getVertices();
        List<Point> verticesB = other.getVertices();

        // 1. SAT 축으로 사용할 법선 벡터 구하기 (this 도형의 모든 변에 대해)
        List<Point> axes = new ArrayList<>();

        int nA = verticesA.size();
        for (int i = 0; i < nA; i++) {
            Point p1 = verticesA.get(i);
            Point p2 = verticesA.get((i + 1) % nA);

            // 변 벡터
            double edgeX = p2.getX() - p1.getX();
            double edgeY = p2.getY() - p1.getY();

            // 법선 벡터 (직교벡터): (edgeY, -edgeX)
            Point axis = new Point(edgeY, -edgeX);

            // 정규화
            double length = Math.sqrt(axis.getX() * axis.getX() + axis.getY() * axis.getY());
            axis = new Point(axis.getX() / length, axis.getY() / length);

            axes.add(axis);
        }

        // 2. 상대 도형이 RegularPolygon이면 해당 법선도 추가
        if (other instanceof RegularPolygon || other.getShapeType().contains("Polygon")) {
            int nB = verticesB.size();
            for (int i = 0; i < nB; i++) {
                Point p1 = verticesB.get(i);
                Point p2 = verticesB.get((i + 1) % nB);

                double edgeX = p2.getX() - p1.getX();
                double edgeY = p2.getY() - p1.getY();

                Point axis = new Point(edgeY, -edgeX);

                double length = Math.sqrt(axis.getX() * axis.getX() + axis.getY() * axis.getY());
                axis = new Point(axis.getX() / length, axis.getY() / length);

                axes.add(axis);
            }
        } else if (other instanceof Circle) {
            // Circle 일 경우 특수 처리:
            // SAT 축에 원의 중심과 정다각형의 각 정점 사이 벡터 추가 (최대 1개 축 필요)

            Circle circle = (Circle) other;
            Point centerC = circle.center;

            // 원 중심과 정다각형 각 정점 중 가장 가까운 점 찾기
            double minDist = Double.MAX_VALUE;
            Point closestPoint = null;
            for (Point v : verticesA) {
                double dx = v.getX() - centerC.getX();
                double dy = v.getY() - centerC.getY();
                double dist = dx*dx + dy*dy;
                if (dist < minDist) {
                    minDist = dist;
                    closestPoint = v;
                }
            }

            if (closestPoint != null) {
                double axisX = closestPoint.getX() - centerC.getX();
                double axisY = closestPoint.getY() - centerC.getY();
                double length = Math.sqrt(axisX * axisX + axisY * axisY);
                if (length != 0) {
                    axisX /= length;
                    axisY /= length;
                    axes.add(new Point(axisX, axisY));
                }
            }
        }

        // 3. 각 축에 대해 두 도형 투영(projection) 검사
        for (Point axis : axes) {
            // 도형 A 투영
            double minA = Double.MAX_VALUE;
            double maxA = -Double.MAX_VALUE;
            for (Point v : verticesA) {
                double proj = v.getX() * axis.getX() + v.getY() * axis.getY();
                if (proj < minA) minA = proj;
                if (proj > maxA) maxA = proj;
            }

            // 도형 B 투영
            double minB = Double.MAX_VALUE;
            double maxB = -Double.MAX_VALUE;

            if (other instanceof Circle) {
                Circle circle = (Circle) other;
                // 원은 중심을 축에 투영하고 반지름만큼 확장
                double centerProj = circle.center.getX() * axis.getX() + circle.center.getY() * axis.getY();
                minB = centerProj - circle.radius;
                maxB = centerProj + circle.radius;
            } else {
                for (Point v : verticesB) {
                    double proj = v.getX() * axis.getX() + v.getY() * axis.getY();
                    if (proj < minB) minB = proj;
                    if (proj > maxB) maxB = proj;
                }
            }

            // 겹침 여부 판단: 투영 구간이 겹치지 않으면 두 도형은 겹치지 않음
            if (maxA < minB || maxB < minA) {
                return false;  // 분리 축 발견
            }
        }

        // 모든 축에서 투영 구간이 겹침 → 두 도형 겹침
        return true;
    }


    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "regularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("sides", sides);
        json.put("rotationAngle", rotationAngle);
        json.put("color", color);

        JSONArray verticesArray = new JSONArray();
        for (Point vertex : vertices) {
            verticesArray.put(vertex.toJSON());
        }
        json.put("vertices", verticesArray);

        return json;
    }

    @Override
    public String getShapeType() {
        return "regularPolygon";
    }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}
