package org.nerdola.openj3d.io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.nerdola.openj3d.core.Mesh;
import org.nerdola.openj3d.core.Triangle;
import org.nerdola.openj3d.core.Vertex;

/**
 * Carrega modelos 3D no formato OBJ com suporte a materiais e texturas.
 * Agora com:
 * - Triangulação automática de faces com mais de 3 vértices
 * - Fallback de UV
 * - Logs de avisos sobre faces sem textura
 */
public class ObjLoader implements ModelLoader {

	private double SCALE = 400.0;
	
    @Override
    public Mesh load(File objFile) throws IOException {
        List<Vertex> vertices = new ArrayList<>();
        List<UV> uvs = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        Map<String, Material> materials = new HashMap<>();
        Material currentMaterial = new Material("default");

        BufferedReader reader = new BufferedReader(new FileReader(objFile));
        File baseDir = objFile.getParentFile();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("v ")) {
                String[] parts = line.split("\\s+");
                vertices.add(new Vertex(
                        Double.parseDouble(parts[1]) * SCALE,
                        -Double.parseDouble(parts[2]) * SCALE,
                        Double.parseDouble(parts[3]) * SCALE
                ));
            } else if (line.startsWith("vt ")) {
                String[] parts = line.split("\\s+");
                uvs.add(new UV(
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2])
                ));
            } else if (line.startsWith("usemtl ")) {
                String name = line.substring(7).trim();
                currentMaterial = materials.getOrDefault(name, new Material(name));
            } else if (line.startsWith("mtllib ")) {
                String mtlName = line.substring(7).trim();
                File mtlFile = new File(baseDir, mtlName);
                materials.putAll(loadMtl(mtlFile, baseDir));
            } else if (line.startsWith("f ")) {
                String[] parts = line.split("\\s+");
                if (parts.length < 4) continue; // ignora linhas inválidas

                int[] vIndices = new int[parts.length - 1];
                int[] tIndices = new int[parts.length - 1];

                for (int i = 1; i < parts.length; i++) {
                    String[] vt = parts[i].split("/");
                    vIndices[i - 1] = Integer.parseInt(vt[0]) - 1;
                    if (vt.length > 1 && !vt[1].isEmpty())
                        tIndices[i - 1] = Integer.parseInt(vt[1]) - 1;
                    else {
                        tIndices[i - 1] = 0; // fallback UV
                        System.out.println("⚠ Face sem UV detectada, usando (0,0).");
                    }
                }

                // Triangula faces com mais de 3 vértices
                for (int i = 1; i < vIndices.length - 1; i++) {
                    Vertex v1 = vertices.get(vIndices[0]);
                    Vertex v2 = vertices.get(vIndices[i]);
                    Vertex v3 = vertices.get(vIndices[i + 1]);

                    UV uv1 = tIndices[0] < uvs.size() ? uvs.get(tIndices[0]) : new UV(0, 0);
                    UV uv2 = tIndices[i] < uvs.size() ? uvs.get(tIndices[i]) : new UV(0, 0);
                    UV uv3 = tIndices[i + 1] < uvs.size() ? uvs.get(tIndices[i + 1]) : new UV(0, 0);

                    triangles.add(new TexturedTriangle(v1, v2, v3, uv1, uv2, uv3, currentMaterial));
                }
            }
        }
        reader.close();

        System.out.println("✅ OBJ carregado: " + vertices.size() + " vértices, " +
                triangles.size() + " triângulos, " + uvs.size() + " UVs.");

        return new Mesh(triangles);
    }

    private Map<String, Material> loadMtl(File mtlFile, File baseDir) {
        Map<String, Material> mats = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(mtlFile))) {
            String line;
            Material current = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("newmtl ")) {
                    current = new Material(line.substring(7).trim());
                    mats.put(current.name, current);
                } else if (line.startsWith("Kd ")) {
                    String[] parts = line.split("\\s+");
                    float r = Float.parseFloat(parts[1]);
                    float g = Float.parseFloat(parts[2]);
                    float b = Float.parseFloat(parts[3]);
                    if (current != null)
                        current.diffuseColor = new Color(r, g, b);
                } else if (line.startsWith("map_Kd ")) {
                    String texName = line.substring(7).trim();
                    File texFile = new File(baseDir, texName);
                    if (texFile.exists()) {
                        BufferedImage texImg = ImageIO.read(texFile);
                        if (current != null)
                            current.setTexture(new Texture(texImg));
                    } else {
                        System.out.println("⚠ Textura não encontrada: " + texName);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Falha ao ler MTL: " + e.getMessage());
        }
        return mats;
    }
    
    public void setScale(double scale)
    {
    	
    }
}
