package rec3d.math.vector;



/**
 * Created by Егор on 16.11.2015.
 */
public class DoubleVector implements Vector<Double> {
    private double[] vec;

    public double[] getVec() {
        //return vec;

        return vec;
    }

    @Override
    public void setVec(Double[] init) {

    }

    public void setVec(double[] vec) {
        this.vec = vec;
    }

    public Double byIndex(int index) {
        return vec[index];
    }

    public DoubleVector(Double... args) {
        vec = new double[args.length];
        int i = 0;
        for (Double d : args) {
            vec[i] = d;
            i ++;
        }
    }
}
