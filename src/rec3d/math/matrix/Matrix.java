package rec3d.math.matrix;

import rec3d.math.vector.DoubleVector;
import rec3d.math.vector.Vector;

import java.util.ArrayList;

/**
 * Created by Егор on 16.11.2015.
 */
public abstract class Matrix<T> {
    T[][] internal;
    String name;
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T[] t : internal) {
            for (T tt : t) {
                sb.append(t.toString()+ " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public T[] getRow(int id) throws IllegalArgumentException{
        return internal[id].clone();
    }

    public T[] getColumn(int id) throws IllegalArgumentException{
        ArrayList<T> tmp = new ArrayList<T>();
        for (T[] t : internal) {
            tmp.add(t[id]);
        }
        return (T[])tmp.toArray();
    }

    public void print() {
        System.out.println("Matrix: " + name + "\n" + toString() + "----------------------------------\n");
    }

    public abstract Vector<T> preMultiply();

    public abstract Vector<T> postMultiply();
}
