package a3;
import java.util.concurrent.atomic.AtomicBoolean;

public class q1b {
    // resizable array
    public volatile Object[] array;
    private final AtomicBoolean resizing = new AtomicBoolean(false);

    public q1b() {
        // initialize array of size 20
        array = new Object[20];
    }
    
    public Object get(int i) {
        // check if resizing is in progress
        while (resizing.get()) {
            // spin
        }
        // check if i is within the array
        if (i >= array.length) {
            resize();
        }
        return array[i];
    }

    public void set(int i, Object o){
        // check if resizing is in progress
        while (resizing.get()) {
            // spin
        }
        // check if i is within the array
        if (i > array.length) {
            resize();
        }
        array[i] = o;
    }

    public void resize() {
        // set resizing to true if it is false, blocking other threads from acting on the array
        if (resizing.compareAndSet(false, true)){
            try {
                // create new array with 10 more elements and copy over old elements
                Object[] newArray = new Object[array.length + 10];
                for (int i = 0; i < array.length; i++) {
                    newArray[i] = array[i];
                }
                array = newArray;
            } finally {
                // resizing done, set resizing to false
                resizing.set(false);
            }
        } else {
            while (resizing.get()) {
                // spin
            }
        }   
    }
}
