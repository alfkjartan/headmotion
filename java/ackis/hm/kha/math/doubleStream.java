package kha.math;


public interface doubleStream {
    public void reset();

    public boolean hasNext();

    public double next() throws NoSuchElementException;

    public int size();
}

