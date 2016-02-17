package edu.lapidus.rec3d.depth.threaded;

/**
 * Created by Егор on 17.02.2016.
 */
public class Lock {
    int id;

    public Lock(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lock lock = (Lock) o;

        return id == lock.id;

    }

    @Override
    public int hashCode() {
        int result = 17;
        return 31 * result + id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
