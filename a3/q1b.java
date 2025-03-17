package a3;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class q1b {
    // resizable array, atomic reference for atomic r/w operations
    public volatile AtomicReference<Object[]> array;
    private final AtomicBoolean resizing = new AtomicBoolean(false);

    public q1b() {
        // initialize array of size 20
        array = new AtomicReference<>(new Object[20]);
    }
    
    public Object get(int i) {
        // check if i is within the array
        if (i >= array.get().length) {
            resize();
        }

        // check if resizing is in progress
        while (resizing.get()) {
            // spin
        }
        return array.get()[i];
    }

    public void set(int i, Object o){
        // check if i is within the array
        if (i >= array.get().length) {
            resize();
        }

        // check if resizing is in progress
        while (resizing.get()) {
            // spin
        }

        array.get()[i] = o;
    }

    public void resize() {
        // set resizing to true if it is false, blocking other threads from acting on the array
        if (resizing.compareAndSet(false, true)){
            try {
                // create new array with 10 more elements and copy over old elements
                Object[] newArray = new Object[array.get().length + 10];
                for (int i = 0; i < array.get().length; i++) {
                    newArray[i] = array.get()[i];
                }
                array.set(newArray);
            } finally {
                // resizing done, set resizing to false
                resizing.set(false);
            }
        // currently being resized, spin until resizing is done
        } else {
            while (resizing.get()) {
                // spin
            }
        }   
    }
}
