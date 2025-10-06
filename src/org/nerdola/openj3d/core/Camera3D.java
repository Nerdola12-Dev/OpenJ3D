package org.nerdola.openj3d.core;

/**
 * Câmera simples sem controles.
 * Apenas posição e ângulos ajustáveis manualmente.
 */
public class Camera3D {

    private Vector3 position = new Vector3();
    private double yaw = 0;   // rotação horizontal
    private double pitch = 0; // rotação vertical

    public Camera3D() {}

    // ----------------------------
    // Posicionamento
    public void setPos(Vector3 pos) { this.position = pos.copy(); }
    public void setPos(double x, double y, double z) { this.position.set(x, y, z); }
    public void setPosX(double x) { this.position.x = x; }
    public void setPosY(double y) { this.position.y = y; }
    public void setPosZ(double z) { this.position.z = z; }

    public Vector3 getPos() { return position.copy(); }
    public double getX() { return position.x; }
    public double getY() { return position.y; }
    public double getZ() { return position.z; }

    // ----------------------------
    // Rotação
    public void setYaw(double yaw) { this.yaw = yaw; }
    public void setPitch(double pitch) { this.pitch = pitch; }

    public double getYaw() { return yaw; }
    public double getPitch() { return pitch; }

    // ----------------------------
    // Matriz de visualização para render
    public Matrix4 getViewMatrix() {
        // rotaciona primeiro, depois aplica translação inversa
        Matrix4 rotation = Matrix4.rotationY(-yaw).multiply(Matrix4.rotationX(-pitch));
        Matrix4 translation = Matrix4.translation(-position.x, -position.y, -position.z);
        return rotation.multiply(translation);
    }
}
