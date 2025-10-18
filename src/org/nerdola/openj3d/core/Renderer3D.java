package org.nerdola.openj3d.core;

import org.nerdola.openj3d.io.TexturedTriangle;
import org.nerdola.openj3d.io.Texture;
import org.nerdola.openj3d.io.Material;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renderizador 3D básico com z-buffer e suporte a texturas (UV).
 */
public class Renderer3D {

    public BufferedImage render(Mesh mesh, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double[] zBuffer = new double[width * height];

        for (int i = 0; i < zBuffer.length; i++) {
            zBuffer[i] = Double.NEGATIVE_INFINITY;
        }

        for (Triangle t : mesh.getTriangles()) {
            Vertex norm = t.normal();
            double shade = Math.abs(norm.z);

            if (t instanceof TexturedTriangle) {
                TexturedTriangle tt = (TexturedTriangle) t;
                Material mat = tt.material;
                if (mat != null && mat.hasTexture()) {
                    drawTexturedTriangle(img, zBuffer, tt);
                    continue;
                }
            }

            Color shaded = ColorUtils.shade(t.color, shade);
            drawTriangle(img, zBuffer, t, shaded);
        }

        return img;
    }

    private void drawTriangle(BufferedImage img, double[] zBuffer, Triangle t, Color color) {
        int width = img.getWidth(), height = img.getHeight();

        int minX = (int) Math.max(0, Math.ceil(Math.min(t.v1.x, Math.min(t.v2.x, t.v3.x))));
        int maxX = (int) Math.min(width - 1, Math.floor(Math.max(t.v1.x, Math.max(t.v2.x, t.v3.x))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(t.v1.y, Math.min(t.v2.y, t.v3.y))));
        int maxY = (int) Math.min(height - 1, Math.floor(Math.max(t.v1.y, Math.max(t.v2.y, t.v3.y))));

        double area = (t.v1.y - t.v3.y) * (t.v2.x - t.v3.x) + (t.v2.y - t.v3.y) * (t.v3.x - t.v1.x);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double b1 = ((y - t.v3.y) * (t.v2.x - t.v3.x) + (t.v2.y - t.v3.y) * (t.v3.x - x)) / area;
                double b2 = ((y - t.v1.y) * (t.v3.x - t.v1.x) + (t.v3.y - t.v1.y) * (t.v1.x - x)) / area;
                double b3 = ((y - t.v2.y) * (t.v1.x - t.v2.x) + (t.v1.y - t.v2.y) * (t.v2.x - x)) / area;

                if (b1 >= 0 && b2 >= 0 && b3 >= 0 && b1 <= 1 && b2 <= 1 && b3 <= 1) {
                    double depth = b1 * t.v1.z + b2 * t.v2.z + b3 * t.v3.z;
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < depth) {
                        img.setRGB(x, y, color.getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }
    }

    /**
     * Renderiza um TexturedTriangle (interpolando UVs por baricentro e amostrando a textura).
     */
    private void drawTexturedTriangle(BufferedImage img, double[] zBuffer, TexturedTriangle t) {
        int width = img.getWidth(), height = img.getHeight();

        int minX = (int) Math.max(0, Math.ceil(Math.min(t.v1.x, Math.min(t.v2.x, t.v3.x))));
        int maxX = (int) Math.min(width - 1, Math.floor(Math.max(t.v1.x, Math.max(t.v2.x, t.v3.x))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(t.v1.y, Math.min(t.v2.y, t.v3.y))));
        int maxY = (int) Math.min(height - 1, Math.floor(Math.max(t.v1.y, Math.max(t.v2.y, t.v3.y))));

        double area = (t.v1.y - t.v3.y) * (t.v2.x - t.v3.x) + (t.v2.y - t.v3.y) * (t.v3.x - t.v1.x);

        Texture texture = t.material != null ? t.material.texture : null;
        if (texture == null) {
            drawTriangle(img, zBuffer, t, t.color);
            return;
        }

        BufferedImage texImg = texture.getLayer(0);
        int tw = texImg.getWidth();
        int th = texImg.getHeight();
        int[] texPixels = texImg.getRGB(0, 0, tw, th, null, 0, tw);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double b1 = ((y - t.v3.y) * (t.v2.x - t.v3.x) + (t.v2.y - t.v3.y) * (t.v3.x - x)) / area;
                double b2 = ((y - t.v1.y) * (t.v3.x - t.v1.x) + (t.v3.y - t.v1.y) * (t.v1.x - x)) / area;
                double b3 = ((y - t.v2.y) * (t.v1.x - t.v2.x) + (t.v1.y - t.v2.y) * (t.v2.x - x)) / area;

                if (b1 >= 0 && b2 >= 0 && b3 >= 0 && b1 <= 1 && b2 <= 1 && b3 <= 1) {
                    double depth = b1 * t.v1.z + b2 * t.v2.z + b3 * t.v3.z;
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < depth) {
                        // UV interpolado
                        double u = b1 * t.uv1.u + b2 * t.uv2.u + b3 * t.uv3.u;
                        double v = b1 * t.uv1.v + b2 * t.uv2.v + b3 * t.uv3.v;

                        // Wrap/clamp UV
                        u = u - Math.floor(u);
                        v = v - Math.floor(v);
                        int px = (int)(u * (tw - 1));
                        int py = (int)((1.0 - v) * (th - 1));
                        px = Math.max(0, Math.min(tw - 1, px));
                        py = Math.max(0, Math.min(th - 1, py));

                        int rgb = texPixels[py * tw + px];
                        img.setRGB(x, y, rgb);
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }
    }


    /**
     * Amostragem por vizinho mais próximo. Converte UV (0..1) para pixel da textura.
     * Inverte V (1 - v) para coordenadas de imagem (top->down).
     */
    private int sampleTextureNearest(BufferedImage tex, double u, double v) {
        if (tex == null) return 0xFFFFFFFF; // branco se ausente
        int tw = tex.getWidth();
        int th = tex.getHeight();
        // Wrap/clamp UV
        u = u - Math.floor(u);
        v = v - Math.floor(v);
        if (Double.isNaN(u) || Double.isNaN(v)) return 0xFFFFFFFF;
        int px = (int) (u * (tw - 1));
        int py = (int) ((1.0 - v) * (th - 1)); // inverte v
        px = Math.max(0, Math.min(tw - 1, px));
        py = Math.max(0, Math.min(th - 1, py));
        return tex.getRGB(px, py);
    }
}
