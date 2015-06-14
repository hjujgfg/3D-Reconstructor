package rec3d.visualization;

import com.threed.jpct.*;
import org.lwjgl.Sys;
import rec3d.depth.MainTemp;
import sun.applet.Main;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;



/**
 * Created by Егор on 12.06.2015.
 */
public class JPCTVisualizer extends JPanel implements MouseMotionListener {
    World world;
    Camera camera;
    ArrayList<Object3D> boxes;
    FrameBuffer frameBuffer;
    ArrayList<int[]> points;
    SimpleVector cameraPosition;
    Object3D myObject;
    int dpwidth;
    int dpheight;
    Object3D protoBox;
    SimpleVector lookatVec;
    int camX, camy;
    public static void main(String[] args) {
        JPCTVisualizer visualizer = new JPCTVisualizer();
        //visualizer.pack();
        Config.maxPolysVisible = 12000;
        MainTemp calc = new MainTemp();
        visualizer.points = calc.run();
        visualizer.dpwidth = calc.dpwidth;
        visualizer.dpheight = calc.dpheight;
        visualizer.initWorld();
        visualizer.runCycle();
    }

    private void initWorld() {
        world = new World();
        world.setAmbientLight(0, 255, 0);

        frameBuffer = new FrameBuffer(800, 600, FrameBuffer.SAMPLINGMODE_NORMAL);
        frameBuffer.disableRenderer(IRenderer.RENDERER_SOFTWARE);
        frameBuffer.enableRenderer(IRenderer.RENDERER_OPENGL);

        TextureManager.getInstance().addTexture("tinyTexture", new Texture(1, 1, Color.GRAY));

        boxes = new ArrayList<Object3D>();
        fillBoxes(points);
        int [] p = points.get(points.size()/2);
        lookatVec = new SimpleVector(p[0], p[1], p[2]);
        cameraPosition = new SimpleVector(1300, 900, -500);
        setupCamera(cameraPosition, lookatVec);
        //this.addMouseMotionListener(this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                camX = e.getX();
                camy = e.getY();
                SimpleVector ray = Interact2D.reproject2D3D(world.getCamera(), frameBuffer, camX, camy);
                lookatVec = ray.normalize();
            }
        });
        /*this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                camX = e.getX();
                camy = e.getY();
                SimpleVector ray = Interact2D.reproject2D3D(world.getCamera(), frameBuffer, camX, camy);
                lookatVec = ray.normalize();
            }
        });*/
        /*this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                frameBuffer.disableRenderer(IRenderer.RENDERER_OPENGL);
                frameBuffer.dispose();
                System.exit(0);
            }
        });*/
    }

    public Object3D createBox(float x, float y, float z) {
        Object3D box;
        if (protoBox == null) {
            protoBox = Primitives.getBox(1.f, 1.f);
        }
        box = protoBox.cloneObject();
        box.translate(x, y, z);
        box.setTexture("tinyTexture");
        box.setEnvmapped(Object3D.ENVMAP_ENABLED);
        box.build();
        return box;
    }



    public void setupCamera(SimpleVector position, SimpleVector lookat) {
        camera = world.getCamera();
        camera.setPosition(position);
        camera.lookAt(lookat);
        camera.setOrientation(lookat, new SimpleVector(0, -1, 0));
    }

    private void runCycle() {
        while (!org.lwjgl.opengl.Display.isCloseRequested()) {
            //cameraPosition.rotateY(1.f);
            //camera.moveCamera(cameraPosition, 1);

            //camera.lookAt(new SimpleVector(camX, camy, ));
            camera.lookAt(lookatVec);
            frameBuffer.clear(java.awt.Color.BLUE);
            world.renderScene(frameBuffer);
            world.draw(frameBuffer);
            frameBuffer.update();
            frameBuffer.displayGLOnly();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Object3D createPolygonal(ArrayList<double[]> points) {
        double[][] pointsArr = (double[][]) points.toArray();

        myObject = new Object3D(pointsArr.length);
        for (int i = 0; i < dpheight - 1; i ++) {
            if (MainTemp.rowLengths[i] > 0 && MainTemp.rowLengths[i + 1] > 0) {
                for (int j = 0, k = 0; j < MainTemp.rowLengths[i] - 1 && k < MainTemp.rowLengths[i+1] - 1; j++, k ++) {
                    SimpleVector[] v = new SimpleVector[4];
                /*v[0] = new SimpleVector(pointsArr[i][j]);
                myObject.addTriangle();*/
                }
            }
        }
        return myObject;
    }


    
    private void fillBoxes(ArrayList<int[]> points) {
        System.out.println("Points size: " + points.size());
        int maxNumberOfBoxes = 10000;
        for (int i = 0; i < points.size() / 211 && i < maxNumberOfBoxes; i ++) {
            int[] arr = points.get(i * 211);
            Object3D tmp = createBox((float)arr[0], (float)arr[1], (float)arr[2]);
            boxes.add(tmp);
            world.addObject(tmp);
            System.out.println("box added" + arr[0] + " " + arr[1] + " " + arr[2]);
        }
    }

    private void triangulate(ArrayList<double[]> points) {

    }


    public void mouseDragged(MouseEvent e) {
        camX = e.getX();
        camy = e.getY();
        SimpleVector ray = Interact2D.reproject2D3D(world.getCamera(), frameBuffer, camX, camy);
        lookatVec = ray.normalize();
    }


    public void mouseMoved(MouseEvent e) {
        camX = e.getX();
        camy = e.getY();
        SimpleVector ray = Interact2D.reproject2D3D(world.getCamera(), frameBuffer, camX, camy);
        lookatVec = ray.normalize();
    }


}
