package org.nerdola.openj3d.core;

/**
 * Representa um vértice (ou ponto) 3D com operações vetoriais.
 */
public class Vertex {
    public double x, y, z;

    public Vertex(double x, double y, double z) {
        this.x = x; 
        this.y = y; 
        this.z = z;
    }

    public Vertex copy() {
        return new Vertex(x, y, z);
    }

    // -----------------------------
    // Operações vetoriais

    public Vertex add(Vertex other) {
        return new Vertex(x + other.x, y + other.y, z + other.z);
    }

    public Vertex subtract(Vertex other) {
        return new Vertex(x - other.x, y - other.y, z - other.z);
    }

    public Vertex multiply(double scalar) {
        return new Vertex(x * scalar, y * scalar, z * scalar);
    }

    public double dot(Vertex other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vertex cross(Vertex other) {
        return new Vertex(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }

    public Vertex normalize() {
        double len = Math.sqrt(x*x + y*y + z*z);
        return len == 0 ? new Vertex(0,0,0) : new Vertex(x/len, y/len, z/len);
    }

    @Override
    public String toString() {
        return "Vertex(" + x + ", " + y + ", " + z + ")";
    }
}
