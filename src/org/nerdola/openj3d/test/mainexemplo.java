package org.nerdola.openj3d.test;

import org.nerdola.openj3d.core.Matrix3;
import org.nerdola.openj3d.core.Mesh;
import org.nerdola.openj3d.core.Renderer3D;
import org.nerdola.openj3d.core.Triangle;
import org.nerdola.openj3d.core.Vertex;
import org.nerdola.openj3d.io.ObjLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.File;

/**
 * Exemplo que abre um FileChooser para carregar um .obj, e faz render loop com BufferStrategy.
 */
public class mainexemplo extends Canvas {

    private final Renderer3D renderer = new Renderer3D();
    private Mesh mesh;
    private volatile boolean running = true;
    private double angle = 0.0;

    public mainexemplo(Mesh mesh) {
        this.mesh = mesh;
        setSize(800, 600);
        setBackground(Color.BLACK);
    }

    public void start() {
        createBufferStrategy(2);
        BufferStrategy bs = getBufferStrategy();

        long lastTime = System.nanoTime();
        final double nsPerFrame = 1_000_000_000.0 / 60.0;

        while (running) {
            long now = System.nanoTime();
            if (now - lastTime >= nsPerFrame) {
                // update
                angle += 0.01; // controle de rotação
                render(bs);
                lastTime = now;
            } else {
                // sleep pequeno para aliviar CPU
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void render(BufferStrategy bs) {
        Graphics g = bs.getDrawGraphics();
        try {
            int w = getWidth(), h = getHeight();
            // limpa
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);

            // aplica rotações na mesh (não altera a mesh original)
            Matrix3 rotation = Matrix3.rotationY(angle).multiply(Matrix3.rotationX(angle * 0.5));
            Mesh transformed = mesh.transformed(rotation);

            // centraliza (transformed já tem vértices transformados em espaço 3D; aqui assumimos projeção ortográfica simples)
            for (Triangle t : transformed.getTriangles()) {
                for (Vertex v : new Vertex[]{t.v1, t.v2, t.v3}) {
                    v.x += w / 2.0;
                    v.y += h / 2.0;
                }
            }

            // render
            java.awt.image.BufferedImage img = renderer.render(transformed, w, h);
            g.drawImage(img, 0, 0, null);

        } finally {
            g.dispose();
        }
        bs.show();
        Toolkit.getDefaultToolkit().sync();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("OpenJ3D - OBJ Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainexemplo canvas = null;

            // pedir ao usuário selecionar um arquivo .obj
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecione um arquivo .obj (com .mtl e textura)");
            int res = chooser.showOpenDialog(null);
            if (res != JFileChooser.APPROVE_OPTION) {
                System.out.println("Nenhum arquivo selecionado. Saindo.");
                System.exit(0);
            }

            File objFile = chooser.getSelectedFile();
            Mesh mesh = null;
            try {
                ObjLoader loader = new ObjLoader();
                mesh = loader.load(objFile);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Falha ao carregar OBJ: " + e.getMessage());
                System.exit(1);
            }

            canvas = new mainexemplo(mesh);
            canvas.setPreferredSize(new Dimension(800, 600));
            frame.add(canvas);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // iniciar thread de render
            final mainexemplo finalCanvas = canvas;
            new Thread(finalCanvas::start, "RenderThread").start();
        });
    }
}
