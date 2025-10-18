package org.nerdola.openj3d.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

import org.nerdola.openj3d.core.Mesh;
import org.nerdola.openj3d.core.Triangle;
import org.nerdola.openj3d.core.Vertex;

import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.InputStreamReader;


public class OpenJ3DModelLoader implements ModelLoader {

    private static final double SCALE = 400.0; // escala padrão

    @Override
    public Mesh load(File oj3dFile) throws IOException {
        if (!oj3dFile.exists()) throw new IOException(".oj3d não encontrado: " + oj3dFile.getAbsolutePath());

        try (ZipFile zip = new ZipFile(oj3dFile)) {
            ZipEntry modelEntry = zip.getEntry("Model.json");
            if (modelEntry == null) throw new IOException("Model.json não encontrado dentro do .oj3d");

            InputStream modelStream = zip.getInputStream(modelEntry);
            Gson gson = new Gson();
            ModelData modelData = gson.fromJson(new InputStreamReader(modelStream), ModelData.class);

            List<Triangle> triangles = new ArrayList<>();

            // Carrega meshes
            for (ModelData.MeshData mesh : modelData.meshes) {
                List<Vertex> vertices = new ArrayList<>();
                List<UV> uvs = new ArrayList<>();

                // vertices com escala aplicada
                if (mesh.vertices != null) {
                    for (ModelData.VertexData v : mesh.vertices) {
                        vertices.add(new Vertex(
                            v.x * SCALE,
                            v.y * SCALE,
                            v.z * SCALE
                        ));
                    }
                }

                // UVs
                if (mesh.uvs != null) {
                    for (ModelData.UVData uv : mesh.uvs) {
                        uvs.add(new UV(uv.u, uv.v));
                    }
                }

                // Material
                Material mat = new Material(mesh.material != null ? mesh.material : "default");
                if (mesh.materialTexture != null) {
                    ZipEntry texEntry = zip.getEntry(mesh.materialTexture);
                    if (texEntry != null) {
                        BufferedImage img = ImageIO.read(zip.getInputStream(texEntry));
                        mat.setTexture(new Texture(img));
                    } else {
                        System.out.println("⚠ Textura não encontrada no .oj3d: " + mesh.materialTexture);
                    }
                }

                // Triângulos
                if (mesh.triangles != null) {
                    for (int[] t : mesh.triangles) {
                        Vertex v1 = vertices.get(t[0]);
                        Vertex v2 = vertices.get(t[1]);
                        Vertex v3 = vertices.get(t[2]);

                        UV uv1 = (uvs.size() > t[0]) ? uvs.get(t[0]) : new UV(0, 0);
                        UV uv2 = (uvs.size() > t[1]) ? uvs.get(t[1]) : new UV(0, 0);
                        UV uv3 = (uvs.size() > t[2]) ? uvs.get(t[2]) : new UV(0, 0);

                        if (uvs.size() <= Math.max(t[0], Math.max(t[1], t[2]))) {
                            System.out.println("⚠ Fallback UV aplicado para triângulo: " + t[0] + "," + t[1] + "," + t[2]);
                        }

                        triangles.add(new TexturedTriangle(v1, v2, v3, uv1, uv2, uv3, mat));
                    }
                }
            }

            System.out.println("✅ OJ3D carregado do zip: " + triangles.size() + " triângulos.");
            return new Mesh(triangles);
        }
    }

    // Estrutura interna para mapear Model.json
    private static class ModelData {
        List<MeshData> meshes;

        static class MeshData {
            String name;
            List<VertexData> vertices;
            List<UVData> uvs;
            List<int[]> triangles;
            String material;
            String materialTexture; // caminho relativo da textura
        }

        static class VertexData {
            double x, y, z;
        }

        static class UVData {
            double u, v;
        }
    }
}

