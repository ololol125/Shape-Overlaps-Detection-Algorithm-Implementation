package knu.pyh.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class IrregularPolygon extends Shape {
    private List<Point> vertices;

    public IrregularPolygon(Point center, double radius, int numVertices) {
        super(center, radius);
        this.vertices = generateIrregularVertices(numVertices);
    }

    private List<Point> generateIrregularVertices(int numVertices) {
        List<Point> points = new ArrayList<>();

        // 1. 무작위 각도로 점들 생성
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            angles.add(Math.random() * 2 * Math.PI);
        }
        Collections.sort(angles); // 각도 순으로 정렬

        // 2. 각 점에 대해 무작위 반경 적용
        for (int i = 0; i < numVertices; i++) {
            double angle = angles.get(i);
            double r = radius * (0.5 + Math.random() * 0.5);
            double x = center.getX() + r * Math.cos(angle);
            double y = center.getY() + r * Math.sin(angle);
            points.add(new Point(x, y));
        }

        // 간단한 컨벡스 헐 생성 (선분 교차 방지)
        return createSimpleConvexHull(points);
    }

    private List<Point> createSimpleConvexHull(List<Point> points) {
        // 간단한 컨벡스 헐 구현
        if (points.size() <= 3) return points;

        // x 좌표로 정렬
        points.sort(Comparator.comparingDouble(Point::getX));

        List<Point> hull = new ArrayList<>();

        // 하부 헐
        for (Point p : points) {
            while (hull.size() >= 2 && orientation(hull.get(hull.size() - 2),
                    hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        // 상부 헐
        int lowerSize = hull.size();
        for (int i = points.size() - 2; i >= 0; i--) {
            Point p = points.get(i);
            while (hull.size() > lowerSize && orientation(hull.get(hull.size() - 2),
                    hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        // 마지막 점 제거 (중복)
        if (hull.size() > 1) hull.remove(hull.size() - 1);

        return hull;
    }

    private double orientation(Point p, Point q, Point r) {
        return (q.getX() - p.getX()) * (r.getY() - p.getY()) -
                (q.getY() - p.getY()) * (r.getX() - p.getX());
    }

    // TODO: 학생 과제 - 일반 다각형의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        List<Point> verticesA = this.getVertices();
        List<Point> verticesB = other.getVertices();

        List<Point> axes = new ArrayList<>();

        // 1. this(불규칙 다각형)의 변 법선 구하기
        int nA = verticesA.size();
        for (int i = 0; i < nA; i++) {
            Point p1 = verticesA.get(i);
            Point p2 = verticesA.get((i + 1) % nA);

            double edgeX = p2.getX() - p1.getX();
            double edgeY = p2.getY() - p1.getY();

            Point axis = new Point(edgeY, -edgeX);

            double length = Math.sqrt(axis.getX() * axis.getX() + axis.getY() * axis.getY());
            axis = new Point(axis.getX() / length, axis.getY() / length);

            axes.add(axis);
        }

        // 2. other 도형이 다각형인 경우 other의 법선도 추가
        if (other instanceof IrregularPolygon || other instanceof RegularPolygon || other.getShapeType().contains("Polygon")) {
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
            Circle circle = (Circle) other;
            Point centerC = circle.center;

            // 원 중심과 this 도형에서 가장 가까운 정점 사이 축 추가
            double minDist = Double.MAX_VALUE;
            Point closestPoint = null;
            for (Point v : verticesA) {
                double dx = v.getX() - centerC.getX();
                double dy = v.getY() - centerC.getY();
                double dist = dx * dx + dy * dy;
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

        // 3. 각 축에 대해 투영 검사
        for (Point axis : axes) {
            double minA = Double.MAX_VALUE;
            double maxA = -Double.MAX_VALUE;
            for (Point v : verticesA) {
                double proj = v.getX() * axis.getX() + v.getY() * axis.getY();
                if (proj < minA) minA = proj;
                if (proj > maxA) maxA = proj;
            }

            double minB = Double.MAX_VALUE;
            double maxB = -Double.MAX_VALUE;

            if (other instanceof Circle) {
                Circle circle = (Circle) other;
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

            if (maxA < minB || maxB < minA) {
                return false; // 분리 축 발견 → 겹치지 않음
            }
        }

        return true; // 모든 축에서 겹침 → 겹침
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "irregularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
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
        return "irregularPolygon";
    }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}
