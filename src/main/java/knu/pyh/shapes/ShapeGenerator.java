package knu.pyh.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class ShapeGenerator {
    private Random random;

    public ShapeGenerator() {
        this.random = new Random();
    }

    public JSONObject generateShapes(int width, int height, int radiusMax, int howMany, int maxEdges) {
        List<Shape> shapes = new ArrayList<>();

        // 도형 생성 (원: 20%, 정다각형: 25%, 일반다각형: 55%)
        for (int i = 0; i < howMany; i++) {
            double probability = random.nextDouble();

            // 무작위 중심점 생성
            double centerX = radiusMax + random.nextDouble() * (width - 2 * radiusMax);
            double centerY = radiusMax + random.nextDouble() * (height - 2 * radiusMax);
            Point center = new Point(centerX, centerY);

            // 무작위 반경
            double radius = 10 + random.nextDouble() * (radiusMax - 10);

            Shape shape;
            if (probability < 0.20) {
                shape = new Circle(center, radius);
            } else if (probability < 0.45) {
                int sides = 3 + random.nextInt(maxEdges - 2);
                double rotation = random.nextDouble() * 2 * Math.PI;
                shape = new RegularPolygon(center, radius, sides, rotation);
            } else {
                int vertices = 3 + random.nextInt(maxEdges - 2);
                shape = new IrregularPolygon(center, radius, vertices);
            }

            shapes.add(shape);
        }

        // 연쇄적 그룹화 처리
        List<Set<String>> overlapGroups = findConnectedComponents(shapes);
        assignGroupColors(shapes, overlapGroups);

        // JSON 응답 생성
        JSONObject response = new JSONObject();
        JSONArray shapesArray = new JSONArray();

        for (Shape shape : shapes) {
            shapesArray.put(shape.toJSON());
        }

        response.put("shapes", shapesArray);
        response.put("totalCount", shapes.size());
        response.put("overlapGroups", convertGroupsToJSON(overlapGroups));

        return response;
    }

    // Union-Find를 사용한 연결 요소 찾기 (연쇄적 그룹화 해결)
    private List<Set<String>> findConnectedComponents(List<Shape> shapes) {
        // Union-Find 자료구조 구현
        Map<String, String> parent = new HashMap<>();
        Map<String, Integer> rank = new HashMap<>();

        // 초기화
        for (Shape shape : shapes) {
            parent.put(shape.getId(), shape.getId());
            rank.put(shape.getId(), 0);
        }

        // 모든 쌍에 대해 겹침 검사
        for (int i = 0; i < shapes.size(); i++) {
            for (int j = i + 1; j < shapes.size(); j++) {
                if (shapes.get(i).overlaps(shapes.get(j))) {
                    union(parent, rank, shapes.get(i).getId(), shapes.get(j).getId());
                }
            }
        }

        // 연결 요소 그룹화
        Map<String, Set<String>> groupMap = new HashMap<>();
        for (Shape shape : shapes) {
            String root = find(parent, shape.getId());
            groupMap.computeIfAbsent(root, k -> new HashSet<>()).add(shape.getId());
        }

        return new ArrayList<>(groupMap.values());
    }

    private String find(Map<String, String> parent, String x) {
        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent, parent.get(x))); // 경로 압축
        }
        return parent.get(x);
    }

    private void union(Map<String, String> parent, Map<String, Integer> rank,
                       String x, String y) {
        String rootX = find(parent, x);
        String rootY = find(parent, y);

        if (!rootX.equals(rootY)) {
            // 랭크 기반 합치기
            if (rank.get(rootX) < rank.get(rootY)) {
                parent.put(rootX, rootY);
            } else if (rank.get(rootX) > rank.get(rootY)) {
                parent.put(rootY, rootX);
            } else {
                parent.put(rootY, rootX);
                rank.put(rootX, rank.get(rootX) + 1);
            }
        }
    }

    private void assignGroupColors(List<Shape> shapes, List<Set<String>> groups) {
        Map<String, Shape> shapeMap = new HashMap<>();
        for (Shape shape : shapes) {
            shapeMap.put(shape.getId(), shape);
        }

        String[] COLORS = {
                "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF",
                "#00FFFF", "#FFA500", "#800080", "#008000", "#000080"
        };

        for (int i = 0; i < groups.size(); i++) {
            Set<String> group = groups.get(i);
            if (group.size() > 1) {
                String color = COLORS[i % COLORS.length];
                for (String shapeId : group) {
                    Shape shape = shapeMap.get(shapeId);
                    if (shape != null) {
                        shape.setColor(color);
                    }
                }
            }
        }
    }

    private JSONArray convertGroupsToJSON(List<Set<String>> groups) {
        JSONArray groupsArray = new JSONArray();
        String[] COLORS = {
                "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF",
                "#00FFFF", "#FFA500", "#800080", "#008000", "#000080"
        };

        for (int i = 0; i < groups.size(); i++) {
            Set<String> group = groups.get(i);
            if (group.size() > 1) {
                JSONObject groupJson = new JSONObject();
                JSONArray shapeIds = new JSONArray();

                for (String shapeId : group) {
                    shapeIds.put(shapeId);
                }

                groupJson.put("shapeIds", shapeIds);
                groupJson.put("color", COLORS[i % COLORS.length]);
                groupJson.put("size", group.size());

                groupsArray.put(groupJson);
            }
        }

        return groupsArray;
    }
}
