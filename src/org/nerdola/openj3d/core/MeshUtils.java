package org.nerdola.openj3d.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Funções geométricas auxiliares.
 */
public class MeshUtils {

    /**
     * Subdivide triângulos e normaliza vértices para formar uma esfera.
     */
    public static List<Triangle> inflate(List<Triangle> tris, double radius) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            Vertex m1 = midpoint(t.v1, t.v2);
            Vertex m2 = midpoint(t.v2, t.v3);
            Vertex m3 = midpoint(t.v1, t.v3);

            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }

        // Normalizar para o raio desejado
        for (Triangle t : result) {
            for (Vertex v : new Vertex[]{t.v1, t.v2, t.v3}) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
                v.x = (v.x / l) * radius;
                v.y = (v.y / l) * radius;
                v.z = (v.z / l) * radius;
            }
        }
        return result;
    }

    private static Vertex midpoint(Vertex a, Vertex b) {
        return new Vertex(
            (a.x + b.x) / 2,
            (a.y + b.y) / 2,
            (a.z + b.z) / 2
        );
    }
}