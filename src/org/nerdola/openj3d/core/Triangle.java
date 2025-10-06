package org.nerdola.openj3d.core;

import java.awt.*;

/**
 * Triângulo 3D com cor.
 */
public class Triangle {
    public Vertex v1, v2, v3;
    public Color color;

    public Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1; this.v2 = v2; this.v3 = v3; this.color = color;
    }

    public Vertex normal() {
        Vertex ab = v2.subtract(v1);
        Vertex ac = v3.subtract(v1);
        return ab.cross(ac).normalize();
    }
}