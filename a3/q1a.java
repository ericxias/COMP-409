package a3;
import java.util.concurrent.locks.ReentrantLock;

public class q1a {
    public volatile Object[] array;
    private final ReentrantLock lock = new ReentrantLock();

    public q1a() {
        array = new Object[20];
    }

    public Object get(int i) {

    if (i >= array.length) {
            resize();
        }

        return array[i];


    }

    public void set(int i, Object o){

        if (i > array.length) {
            resize();
        }
        array[i] = o;

    }

    public void resize(){
        lock.lock();
        try {
            Object[] newArray = new Object[array.length + 10];
            for (int i = 0; i < array.length; i++) {
                newArray[i] = array[i];
            }
            array = newArray;
        } finally {
            lock.unlock();
        }
    }
    
}
