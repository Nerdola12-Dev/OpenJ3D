package org.nerdola.openj3d.core;

/**
 * Matriz 4x4 homogênea para transformações 3D.
 */
public class Matrix4 {

    private final double[] m; // 16 valores, row-major

    public Matrix4(double[] values) {
        if (values.length != 16) throw new IllegalArgumentException("Matrix4 precisa de 16 elementos.");
        this.m = values.clone();
    }

    public static Matrix4 identity() {
        return new Matrix4(new double[]{
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
        });
    }

    public static Matrix4 translation(double tx, double ty, double tz) {
        return new Matrix4(new double[]{
            1,0,0,tx,
            0,1,0,ty,
            0,0,1,tz,
            0,0,0,1
        });
    }

    public static Matrix4 rotationX(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix4(new double[]{
            1,0,0,0,
            0,c,-s,0,
            0,s,c,0,
            0,0,0,1
        });
    }

    public static Matrix4 rotationY(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix4(new double[]{
            c,0,s,0,
            0,1,0,0,
            -s,0,c,0,
            0,0,0,1
        });
    }

    public static Matrix4 rotationZ(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix4(new double[]{
            c,-s,0,0,
            s,c,0,0,
            0,0,1,0,
            0,0,0,1
        });
    }

    public static Matrix4 scale(double sx, double sy, double sz) {
        return new Matrix4(new double[]{
            sx,0,0,0,
            0,sy,0,0,
            0,0,sz,0,
            0,0,0,1
        });
    }

    public Matrix4 multiply(Matrix4 other) {
        double[] r = new double[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                for (int i = 0; i < 4; i++) {
                    r[row*4 + col] += this.m[row*4 + i] * other.m[i*4 + col];
                }
            }
        }
        return new Matrix4(r);
    }

    public Vertex transform(Vertex v) {
        double x = v.x*m[0] + v.y*m[1] + v.z*m[2] + m[3];
        double y = v.x*m[4] + v.y*m[5] + v.z*m[6] + m[7];
        double z = v.x*m[8] + v.y*m[9] + v.z*m[10] + m[11];
        double w = v.x*m[12] + v.y*m[13] + v.z*m[14] + m[15];
        if (w != 0 && w != 1) {
            x /= w; y /= w; z /= w;
        }
        return new Vertex(x, y, z);
    }
}
