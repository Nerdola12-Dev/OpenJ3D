package org.nerdola.openj3d.io;

import java.awt.*;

/**
 * Material básico com suporte a cor difusa e textura.
 */
public class Material {
    public String name;
    public Color diffuseColor = Color.WHITE;
    public Texture texture;

    public Material(String name) {
        this.name = name;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public boolean hasTexture() {
        return texture != null;
    }
}