package org.nerdola.openj3d.io;

import java.io.File;
import java.io.IOException;

import org.nerdola.openj3d.core.Mesh;

/**
 * Interface genérica para carregadores de modelos 3D.
 */
public interface ModelLoader {
    Mesh load(File file) throws IOException;
}