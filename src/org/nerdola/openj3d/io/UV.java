package org.nerdola.openj3d.io;

import org.nerdola.openj3d.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Coordenadas UV (textura).
 */
public class UV {
    public double u, v;
    public UV(double u, double v) { this.u = u; this.v = v; }
}
