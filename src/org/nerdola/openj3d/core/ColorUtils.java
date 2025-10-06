package org.nerdola.openj3d.core;

import java.awt.*;

/**
 * Utilitário para sombreamento e cálculo de cor.
 */
public class ColorUtils {
    public static Color shade(Color color, double shade) {
        shade = Math.max(0, Math.min(1, shade));
        int r = (int) (color.getRed() * shade);
        int g = (int) (color.getGreen() * shade);
        int b = (int) (color.getBlue() * shade);
        return new Color(r, g, b);
    }
}
