package org.nerdola.openj3d.core;

import org.nerdola.openj3d.io.TexturedTriangle;
import org.nerdola.openj3d.io.UV;

import java.util.ArrayList;
import java.util.List;

public class Mesh {
    private final List<Triangle> triangles;

    public Mesh() {
        this.triangles = new ArrayList<>();
    }

    public Mesh(List<Triangle> tris) {
        this.triangles = new ArrayList<>(tris);
    }

    public List<Triangle> getTriangles() { return triangles; }

    public void addTriangle(Triangle t) { triangles.add(t); }

    // ----------------------------
    // Transformação usando Matrix3 (existente)
    public Mesh transformed(Matrix3 matrix) {
        List<Triangle> transformed = new ArrayList<>();
        for (Triangle t : triangles) {
            if (t instanceof TexturedTriangle) {
                TexturedTriangle tt = (TexturedTriangle) t;
                Vertex tv1 = matrix.transform(tt.v1);
                Vertex tv2 = matrix.transform(tt.v2);
                Vertex tv3 = matrix.transform(tt.v3);
                transformed.add(new TexturedTriangle(
                        tv1, tv2, tv3,
                        new UV(tt.uv1.u, tt.uv1.v),
                        new UV(tt.uv2.u, tt.uv2.v),
                        new UV(tt.uv3.u, tt.uv3.v),
                        tt.material
                ));
            } else {
                Vertex v1 = matrix.transform(t.v1);
                Vertex v2 = matrix.transform(t.v2);
                Vertex v3 = matrix.transform(t.v3);
                transformed.add(new Triangle(v1, v2, v3, t.color));
            }
        }
        return new Mesh(transformed);
    }

    // ----------------------------
    // NOVO: Transformação usando Matrix4
    public Mesh transformed(Matrix4 matrix) {
        List<Triangle> transformed = new ArrayList<>();
        for (Triangle t : triangles) {
            if (t instanceof TexturedTriangle) {
                TexturedTriangle tt = (TexturedTriangle) t;
                Vertex tv1 = matrix.transform(tt.v1);
                Vertex tv2 = matrix.transform(tt.v2);
                Vertex tv3 = matrix.transform(tt.v3);
                transformed.add(new TexturedTriangle(
                        tv1, tv2, tv3,
                        new UV(tt.uv1.u, tt.uv1.v),
                        new UV(tt.uv2.u, tt.uv2.v),
                        new UV(tt.uv3.u, tt.uv3.v),
                        tt.material
                ));
            } else {
                Vertex v1 = matrix.transform(t.v1);
                Vertex v2 = matrix.transform(t.v2);
                Vertex v3 = matrix.transform(t.v3);
                transformed.add(new Triangle(v1, v2, v3, t.color));
            }
        }
        return new Mesh(transformed);
    }
}
