package org.nerdola.openj3d.io;

import org.nerdola.openj3d.core.Triangle;
import org.nerdola.openj3d.core.Vertex;

/**
 * Triângulo com coordenadas UV e material.
 */
public class TexturedTriangle extends Triangle {
    public UV uv1, uv2, uv3;
    public Material material;

    public TexturedTriangle(Vertex v1, Vertex v2, Vertex v3,
                            UV uv1, UV uv2, UV uv3,
                            Material material) {
        super(v1, v2, v3, material.diffuseColor);
        this.uv1 = uv1;
        this.uv2 = uv2;
        this.uv3 = uv3;
        this.material = material;
    }
}