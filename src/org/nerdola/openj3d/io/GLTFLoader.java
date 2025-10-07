package org.nerdola.openj3d.io;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.nerdola.openj3d.core.Mesh;
import org.nerdola.openj3d.core.Triangle;
import org.nerdola.openj3d.core.Vertex;

public class GLTFLoader implements ModelLoader {

    private final Gson gson = new Gson();

    @Override
    public Mesh load(File gltfFile) throws IOException {
        try (FileReader reader = new FileReader(gltfFile)) {
            JsonObject gltf = gson.fromJson(reader, JsonObject.class);

            JsonArray meshes = gltf.getAsJsonArray("meshes");
            System.out.println("Meshes encontradas: " + (meshes != null ? meshes.size() : 0));
            if (meshes == null || meshes.size() == 0)
                throw new IOException("GLTF sem meshes");

            List<Triangle> allTriangles = new ArrayList<>();
            Material defaultMaterial = new Material("default");
            defaultMaterial.diffuseColor = Color.RED;

            // Itera por todas as meshes
            for (int m = 0; m < meshes.size(); m++) {
                JsonObject meshObj = meshes.get(m).getAsJsonObject();
                JsonArray primitives = meshObj.getAsJsonArray("primitives");
                System.out.println("Mesh " + m + " - Primitivas: " + (primitives != null ? primitives.size() : 0));
                if (primitives == null || primitives.size() == 0) continue;

                for (int p = 0; p < primitives.size(); p++) {
                    JsonObject primitive = primitives.get(p).getAsJsonObject();
                    JsonObject attributes = primitive.getAsJsonObject("attributes");

                    float[] positions = extractFloatArray(gltf, attributes.get("POSITION").getAsInt(), gltfFile);
                    System.out.println("Mesh " + m + " Primitiva " + p + " - Posições: " + positions.length);

                    float[] texcoords = attributes.has("TEXCOORD_0") ? extractFloatArray(gltf, attributes.get("TEXCOORD_0").getAsInt(), gltfFile) : null;
                    if (texcoords != null) System.out.println("UVs carregadas: " + texcoords.length / 2);

                    int[] indices = primitive.has("indices") ? extractIntArray(gltf, primitive.get("indices").getAsInt(), gltfFile) : null;
                    if (indices != null) System.out.println("Indices carregados: " + indices.length);

                    List<Vertex> vertices = new ArrayList<>();
                    for (int i = 0; i < positions.length; i += 3) {
                        Vertex v = new Vertex(positions[i], positions[i + 1], positions[i + 2]);
                        vertices.add(v);
                        if (i < 9) System.out.println("Vértice " + (i / 3) + ": " + v.x + ", " + v.y + ", " + v.z);
                    }

                    // Cria triângulos
                    if (indices != null) {
                        for (int i = 0; i < indices.length; i += 3) {
                            Vertex v1 = vertices.get(indices[i]);
                            Vertex v2 = vertices.get(indices[i + 1]);
                            Vertex v3 = vertices.get(indices[i + 2]);

                            UV uv1 = texcoords != null ? new UV(texcoords[2 * indices[i]], texcoords[2 * indices[i] + 1]) : new UV(0, 0);
                            UV uv2 = texcoords != null ? new UV(texcoords[2 * indices[i + 1]], texcoords[2 * indices[i + 1] + 1]) : new UV(0, 0);
                            UV uv3 = texcoords != null ? new UV(texcoords[2 * indices[i + 2]], texcoords[2 * indices[i + 2] + 1]) : new UV(0, 0);

                            allTriangles.add(new TexturedTriangle(v1, v2, v3, uv1, uv2, uv3, defaultMaterial));
                        }
                    } else {
                        for (int i = 0; i < vertices.size(); i += 3) {
                            allTriangles.add(new TexturedTriangle(vertices.get(i), vertices.get(i + 1), vertices.get(i + 2),
                                    new UV(0, 0), new UV(0, 0), new UV(0, 0), defaultMaterial));
                        }
                    }

                    System.out.println("Mesh " + m + " Primitiva " + p + " - Triângulos criados: " + (indices != null ? indices.length / 3 : vertices.size() / 3));
                }
            }

            System.out.println("Total de triângulos carregados: " + allTriangles.size());
            return new Mesh(allTriangles);

        } catch (Exception e) {
            throw new IOException("Falha ao carregar GLTF: " + e.getMessage(), e);
        }
    }


    private float[] extractFloatArray(JsonObject gltf, int accessorIndex, File gltfFile) throws IOException {
        JsonArray accessors = gltf.getAsJsonArray("accessors");
        JsonArray bufferViews = gltf.getAsJsonArray("bufferViews");
        JsonArray buffers = gltf.getAsJsonArray("buffers");

        JsonObject accessor = accessors.get(accessorIndex).getAsJsonObject();
        int bufferViewIndex = accessor.get("bufferView").getAsInt();
        int count = accessor.get("count").getAsInt();
        String type = accessor.get("type").getAsString();

        JsonObject bufferView = bufferViews.get(bufferViewIndex).getAsJsonObject();
        int byteOffset = bufferView.has("byteOffset") ? bufferView.get("byteOffset").getAsInt() : 0;
        int bufferIndex = bufferView.get("buffer").getAsInt();

        JsonObject buffer = buffers.get(bufferIndex).getAsJsonObject();
        String uri = buffer.get("uri").getAsString();

        byte[] data;
        if (uri.startsWith("data:")) {
            String base64 = uri.substring(uri.indexOf(",") + 1);
            data = Base64.getDecoder().decode(base64);
        } else {
            File bufferFile = new File(gltfFile.getParentFile(), uri);
            data = java.nio.file.Files.readAllBytes(bufferFile.toPath());
        }

        int componentCount = getTypeSize(type);
        float[] array = new float[count * componentCount];

        for (int i = 0; i < array.length; i++) {
            int idx = byteOffset + i * 4;
            int bits = ((data[idx + 3] & 0xFF) << 24) | ((data[idx + 2] & 0xFF) << 16) |
                       ((data[idx + 1] & 0xFF) << 8) | (data[idx] & 0xFF);
            array[i] = Float.intBitsToFloat(bits);
        }

        return array;
    }

    private int[] extractIntArray(JsonObject gltf, int accessorIndex, File gltfFile) throws IOException {
        float[] floats = extractFloatArray(gltf, accessorIndex, gltfFile);
        int[] ints = new int[floats.length];
        for (int i = 0; i < floats.length; i++) ints[i] = (int) floats[i];
        return ints;
    }

    private int getTypeSize(String type) {
        switch (type) {
            case "SCALAR": return 1;
            case "VEC2": return 2;
            case "VEC3": return 3;
            case "VEC4": return 4;
            default: return 1;
        }
    }
}
