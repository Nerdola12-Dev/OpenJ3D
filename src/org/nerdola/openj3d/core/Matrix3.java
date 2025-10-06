package org.nerdola.openj3d.core;

/**
 * Matriz 3x3 para transformações 3D.
 */
public class Matrix3 {
    private final double[] m; // 9 valores (row-major)

    public Matrix3(double[] values) {
        if (values.length != 9) throw new IllegalArgumentException("Matrix3 precisa de 9 elementos.");
        this.m = values.clone();
    }
    
    public static Matrix3 scale(double sx, double sy, double sz) {
        return new Matrix3(new double[]{
            sx, 0, 0,
            0, sy, 0,
            0, 0, sz
        });
    }

    
    public static Matrix3 identity() {
        return new Matrix3(new double[]{
            1, 0, 0,
            0, 1, 0,
            0, 0, 1
        });
    }

    public static Matrix3 rotationX(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix3(new double[]{
            1, 0, 0,
            0, c, s,
            0, -s, c
        });
    }

    public static Matrix3 rotationY(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix3(new double[]{
            c, 0, -s,
            0, 1, 0,
            s, 0, c
        });
    }

    public static Matrix3 rotationZ(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix3(new double[]{
            c, s, 0,
            -s, c, 0,
            0, 0, 1
        });
    }

    public Matrix3 multiply(Matrix3 other) {
        double[] r = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    r[row * 3 + col] += m[row * 3 + i] * other.m[i * 3 + col];
                }
            }
        }
        return new Matrix3(r);
    }

    public Vertex transform(Vertex v) {
        return new Vertex(
            v.x * m[0] + v.y * m[3] + v.z * m[6],
            v.x * m[1] + v.y * m[4] + v.z * m[7],
            v.x * m[2] + v.y * m[5] + v.z * m[8]
        );
    }
}