package org.nerdola.openj3d.core;

public class Vector3 {
    public double x, y, z;

    public Vector3() { this(0,0,0); }

    public Vector3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vector3 set(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
        return this;
    }

    public Vector3 copy() { return new Vector3(x, y, z); }

    // -----------------------------
    // Operações vetoriais

    public Vector3 add(Vector3 v) { return new Vector3(x + v.x, y + v.y, z + v.z); }
    public Vector3 sub(Vector3 v) { return new Vector3(x - v.x, y - v.y, z - v.z); }
    public Vector3 mul(double s) { return new Vector3(x*s, y*s, z*s); }

    public double dot(Vector3 v) { return x*v.x + y*v.y + z*v.z; }

    public Vector3 cross(Vector3 v) {
        return new Vector3(
            y * v.z - z * v.y,
            z * v.x - x * v.z,
            x * v.y - y * v.x
        );
    }

    public Vector3 normalize() {
        double len = Math.sqrt(x*x + y*y + z*z);
        return len > 0 ? new Vector3(x/len, y/len, z/len) : new Vector3(0,0,0);
    }
}
