package rec3d.math.vector;

/**
 * Created by Егор on 20.11.2015.
 */
public class Vector {
    private double[] vec;

    public double[] getVec() {
        return vec;
    }

    public void setVec(double[] vec) {
        this.vec = vec;
    }

    public Double byIndex(int index) {
        return vec[index];
    }

    public Vector(Double... args) {
        vec = new double[args.length];
        int i = 0;
        for (Double d : args) {
            vec[i] = d;
            i ++;
        }
    }

    public Vector(double[] vec) {
        this.vec = vec;
    }
}
