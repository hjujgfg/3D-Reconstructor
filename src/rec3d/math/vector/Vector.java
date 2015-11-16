package rec3d.math.vector;

/**
 * Created by Егор on 16.11.2015.
 */
public interface Vector<T> {
    public T[] getVec();
    public void setVec(T[] init);
    public T byIndex(int index);
}
