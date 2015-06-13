package rec3d.visualization;

import com.threed.jpct.*;
import org.lwjgl.Sys;
import rec3d.depth.MainTemp;
import sun.java2d.pipe.SpanShapeRenderer;

import java.awt.*;
import java.util.ArrayList;



/**
 * Created by Егор on 12.06.2015.
 */
public class JPCTVisualizer {
    World world;
    Camera camera;
    ArrayList<Object3D> boxes;
    FrameBuffer frameBuffer;
    ArrayList<double[]> points;
    SimpleVector cameraPosition;
    Object3D myObject;
    int dpwidth;
    int dpheight;
    public static void main(String[] args) {
        JPCTVisualizer visualizer = new JPCTVisualizer();
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
        double [] p = points.get(points.size()/2);
        SimpleVector lookat = new SimpleVector(p[0], p[1], p[2]);
        cameraPosition = new SimpleVector(50, p[1], p[2]);
        setupCamera(cameraPosition, lookat);
    }

    public Object3D createBox(float x, float y, float z) {
        Object3D box = Primitives.getBox(1.f, 1.f);
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
    }

    private void runCycle() {
        while (!org.lwjgl.opengl.Display.isCloseRequested()) {
            cameraPosition.rotateY(1.f);
            camera.moveCamera(cameraPosition, 1);
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
            for (int j = 0; j < dpwidth - 1; j ++) {
                SimpleVector [] v = new SimpleVector[4];
                /*v[0] = new SimpleVector(pointsArr[i][j]);
                myObject.addTriangle();*/
            }
        }
        return myObject;
    }
    
    private void fillBoxes(ArrayList<double[]> points) {
        System.out.println("Points size: " + points.size());
        int maxNumberOfBoxes = 10000;
        for (int i = 0; i < points.size() && i < maxNumberOfBoxes; i ++) {
            double[] arr = points.get(i * 211);
            Object3D tmp = createBox((float)arr[0], (float)arr[1], (float)arr[2]);
            boxes.add(tmp);
            world.addObject(tmp);
            System.out.println("box added" + arr[0] + " " + arr[1] + " " + arr[2]);
        }
    }
}
