package org.nerdola.openj3d.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Janela Profissional OpenJ3D
 * @author Nerdola
 */
public class Window extends Canvas {

    // === FRAME ===
    private JFrame frame;
    private String title = "OpenJ3D Window";
    private int width = 800, height = 600;
    private boolean resizable = true;
    private boolean fullscreen = false;
    private float opacity = 1.0f;
    private Color backgroundColor = Color.BLACK;
    private boolean showFPS = true;
    private int limitFPS = 60;

    // === CALLBACKS ===
    private Runnable updateCallback;
    private Consumer<Graphics2D> renderCallback;
    private Runnable onClose, onShow, onResize;

    // === INPUT ===
    private boolean[] keys = new boolean[256];
    private boolean[] mouseButtons = new boolean[5];
    private int mouseX, mouseY, scrollDelta;
    private Cursor customCursor;

    // === FPS ===
    private int fps;
    private int fpsCounter;

    // === MULTI-JANELAS ===
    private static final List<Window> windows = new ArrayList<>();

    // === COMPONENTES INTERNOS ===
    private final List<UIComponent> components = new ArrayList<>();
    private final List<Sprite> sprites = new ArrayList<>();

    // === ANIMAÇÕES / TIMERS ===
    private final List<Tween> tweens = new ArrayList<>();

    public Window() {
        frame = new JFrame();
        this.setFocusable(true);
        windows.add(this);

        // INPUT
        this.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if(e.getKeyCode()<keys.length) keys[e.getKeyCode()] = true; }
            @Override public void keyReleased(KeyEvent e) { if(e.getKeyCode()<keys.length) keys[e.getKeyCode()] = false; }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if(e.getButton()<mouseButtons.length) mouseButtons[e.getButton()] = true; }
            @Override public void mouseReleased(MouseEvent e) { if(e.getButton()<mouseButtons.length) mouseButtons[e.getButton()] = false; }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
            @Override public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });

        this.addMouseWheelListener(e -> scrollDelta = e.getWheelRotation());
    }

    // ====================== API FLUENTE ======================
    public Window setTitle(String t) { this.title = t; return this; }
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.width = width;
        this.height = height;
    }
    
    public Window setWindowSize(int width, int height) {
        // chama o setSize acima (que é void)
        setSize(width, height);
        return this;
    }
    
    public int getFPS() {
		return fps;
	}

    public Window setResizable(boolean r) { this.resizable = r; return this; }
    public Window setFullscreen(boolean f) { this.fullscreen = f; return this; }
    public Window setOpacity(float o) { this.opacity = o; return this; }
    public Window setBackgroundColor(Color c) { this.backgroundColor = c; return this; }
    public Window showFPS(boolean b) { this.showFPS = b; return this; }
    public Window setLimitFPS(int f) { this.limitFPS = f; return this; }
    public Window setWindowCursor(Cursor c) {
        super.setCursor(c); // chama o setCursor original
        this.customCursor = c;
        return this;
    }


    public Window onUpdate(Runnable cb) { this.updateCallback = cb; return this; }
    public Window onRender(Consumer<Graphics2D> cb) { this.renderCallback = cb; return this; }
    public Window onClose(Runnable cb) { this.onClose = cb; return this; }
    public Window onShow(Runnable cb) { this.onShow = cb; return this; }
    public Window onResize(Runnable cb) { this.onResize = cb; return this; }

    // ====================== INPUT ======================
    public boolean isKeyPressed(int key) { return key<keys.length && keys[key]; }
    public boolean isMouseButtonPressed(int button) { return button<mouseButtons.length && mouseButtons[button]; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public int getScrollDelta() { int d = scrollDelta; scrollDelta=0; return d; }

    // ====================== SPRITES / COMPONENTES ======================
    public Window addSprite(Sprite s) { sprites.add(s); return this; }
    public Window addComponent(UIComponent c) { components.add(c); return this; }

    // ====================== START ======================
    public void start() {
        frame.setTitle(title);
        frame.setSize(width, height);
        frame.setResizable(resizable);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if(customCursor!=null) frame.setCursor(customCursor);
        frame.add(this);
        frame.setVisible(true);
        this.createBufferStrategy(3); // Triple buffering
        if(onShow!=null) onShow.run();
        runLoop();
    }

    // ====================== GAME LOOP PROFISSIONAL ======================
    private void runLoop() {
        BufferStrategy bs = this.getBufferStrategy();
        long lastTime = System.nanoTime();
        double nsPerUpdate = 1_000_000_000.0 / limitFPS;
        long lastTimer = System.currentTimeMillis();
        double delta = 0;

        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;
            boolean shouldRender = false;

            while (delta >= 1) {
                if(updateCallback!=null) updateCallback.run();
                // atualizar tweens
                tweens.forEach(Tween::update);
                delta--;
                shouldRender = true;
            }

            if (shouldRender) {
                BufferStrategy bs1 = getBufferStrategy();
                if (bs1 == null) {
                    createBufferStrategy(3);
                    continue; // espera o próximo ciclo
                }

                Graphics2D g = (Graphics2D) bs1.getDrawGraphics();
                try {
                    g.setColor(backgroundColor);
                    g.fillRect(0, 0, getWidth(), getHeight());

                    // renderizar sprites e componentes
                    sprites.forEach(s -> s.render(g));
                    components.forEach(c -> c.render(g));

                    if (renderCallback != null) renderCallback.accept(g);

                    if (showFPS) {
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 20));
                        g.drawString("FPS: " + fps, 10, 25);
                    }
                } finally {
                    g.dispose();
                }

                // tenta exibir o buffer — se falhar, recria
                try {
                    bs1.show();
                } catch (Exception ex) {
                    createBufferStrategy(3);
                    continue;
                }

                Toolkit.getDefaultToolkit().sync();
                fpsCounter++;
            }


            if(System.currentTimeMillis()-lastTimer>=1000){
                fps=fpsCounter;
                fpsCounter=0;
                lastTimer+=1000;
            }
        }
    }

    // ====================== CLASSES INTERNAS EXEMPLO ======================
    public static class UIComponent {
        protected int x, y, width, height;
        public void render(Graphics2D g){ /* desenhar botão, painel, etc */ }
    }

    public static class Sprite {
        protected int x, y;
        protected BufferedImage image;
        public void render(Graphics2D g){ if(image!=null) g.drawImage(image,x,y,null); }
    }

    public static class Tween {
        private Runnable update;
        public Tween(Runnable update){ this.update=update; }
        public void update(){ if(update!=null) update.run(); }
    }
}
