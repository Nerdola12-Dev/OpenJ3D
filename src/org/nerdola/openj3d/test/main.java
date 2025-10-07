package org.nerdola.openj3d.test;

import org.nerdola.openj3d.core.Matrix3;
import org.nerdola.openj3d.core.Mesh;
import org.nerdola.openj3d.core.Renderer3D;
import org.nerdola.openj3d.core.Triangle;
import org.nerdola.openj3d.core.Vertex;
import org.nerdola.openj3d.frame.Window;
import org.nerdola.openj3d.io.GLTFLoader;
import org.nerdola.openj3d.io.ObjLoader;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class main {

    private static double angle = 0.0;

    public static void main(String[] args) throws IOException {
    	
    	Mesh cube = new GLTFLoader().load(new File("C:\\Users\\Bernardes\\Desktop\\model\\glft\\exemplo.gltf"));

        Renderer3D renderer = new Renderer3D();

        Window window = new Window()
                .setWindowSize(800, 600)
                .setBackgroundColor(java.awt.Color.BLACK)
                .setLimitFPS(144)
                .showFPS(true)
                .onUpdate(() -> angle += 0.01) // rotaciona a mesh 
                .setResizable(false);
        
        window.onRender(g -> {
            int w = window.getWidth();
            int h = window.getHeight();

            // aplica rotações
            Matrix3 state = Matrix3.rotationY(angle).multiply(Matrix3.rotationX(angle*0.5));
            Mesh transformed = cube.transformed(state);

            // centraliza
            for (Triangle t : transformed.getTriangles()) {
                for (Vertex v : new Vertex[]{t.v1, t.v2, t.v3}) {
                    v.x += w / 2.0;
                    v.y += h / 2.0;
                }
            }

            // renderiza mesh para BufferedImage
            g.drawImage(renderer.render(transformed, w, h), 0, 0, null);
        });

        window.start();
    }
    
    public static Mesh createCube(double size) {
        double hs = size / 2.0; // half size
        List<Triangle> triangles = new ArrayList<>();

        Vertex v000 = new Vertex(-hs, -hs, -hs);
        Vertex v001 = new Vertex(-hs, -hs,  hs);
        Vertex v010 = new Vertex(-hs,  hs, -hs);
        Vertex v011 = new Vertex(-hs,  hs,  hs);
        Vertex v100 = new Vertex( hs, -hs, -hs);
        Vertex v101 = new Vertex( hs, -hs,  hs);
        Vertex v110 = new Vertex( hs,  hs, -hs);
        Vertex v111 = new Vertex( hs,  hs,  hs);

        Color color = Color.CYAN;

        // Frente
        triangles.add(new Triangle(v101, v001, v011, color));
        triangles.add(new Triangle(v101, v011, v111, color));

        // Trás
        triangles.add(new Triangle(v100, v110, v010, color));
        triangles.add(new Triangle(v100, v010, v000, color));

        // Esquerda
        triangles.add(new Triangle(v000, v010, v011, color));
        triangles.add(new Triangle(v000, v011, v001, color));

        // Direita
        triangles.add(new Triangle(v100, v101, v111, color));
        triangles.add(new Triangle(v100, v111, v110, color));

        // Topo
        triangles.add(new Triangle(v010, v110, v111, color));
        triangles.add(new Triangle(v010, v111, v011, color));

        // Base
        triangles.add(new Triangle(v000, v001, v101, color));
        triangles.add(new Triangle(v000, v101, v100, color));

        return new Mesh(triangles);
    }
}
