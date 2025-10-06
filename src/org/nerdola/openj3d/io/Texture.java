
package org.nerdola.openj3d.io;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma textura (ou múltiplas camadas).
 */
public class Texture {
    private final List<BufferedImage> layers = new ArrayList<>();

    public Texture(BufferedImage base) {
        layers.add(base);
    }

    public void addLayer(BufferedImage layer) {
        layers.add(layer);
    }

    public BufferedImage getLayer(int index) {
        return layers.get(index);
    }

    public int getLayerCount() {
        return layers.size();
    }
}
